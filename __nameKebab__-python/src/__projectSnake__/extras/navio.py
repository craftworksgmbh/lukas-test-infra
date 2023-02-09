import __projectSnake__
from typing import Any,Dict, Optional, Union
import os
import logging
import mlflow
from kedro.framework.hooks import hook_impl
from kedro.pipeline import Pipeline
from kedro.io import DataCatalog
from kedro.runner import AbstractRunner
from kedro_mlflow.mlflow import KedroPipelineModel
from kedro_mlflow.pipeline.pipeline_ml import PipelineML
from kedro_mlflow.framework.hooks import MlflowHook
from kedro_mlflow.io.catalog.switch_catalog_logging import switch_catalog_logging
import pynavio
from pathlib import Path
from .credentials import get_credentials
from pynavio.client import Client


class KedroNavioPipelineModel(KedroPipelineModel):
    def __init__(self, pipeline: Pipeline,
                 catalog: DataCatalog,
                 input_name: str,
                 runner: Optional[AbstractRunner] = None,
                 copy_mode: Optional[Union[Dict[str, str], str]] = None):
        super().__init__(pipeline=pipeline, catalog=catalog, input_name=input_name, runner=runner, copy_mode=copy_mode)

    def load_context(self, context):

        # a consistency check is made when loading the model
        # it would be better to check when saving the model
        # but we rely on a mlflow function for saving, and it is unaware of kedro
        # pipeline structure
        mlflow_artifacts_keys = set(context.artifacts.keys())
        kedro_artifacts_keys = set(self.pipeline.inputs() - {self.input_name})
        in_inference_but_not_artifacts = kedro_artifacts_keys.difference(mlflow_artifacts_keys)
        if len(in_inference_but_not_artifacts) > 0:
            raise ValueError(
                (
                    "Provided artifacts do not match catalog entries:"
                    f"\n    - 'inference.inputs() - artifacts' = : {in_inference_but_not_artifacts}"
                )
            )

        updated_catalog = self.initial_catalog.shallow_copy()
        for name, uri in context.artifacts.items():
            if name in kedro_artifacts_keys:
                updated_catalog._data_sets[name]._filepath = Path(uri)
                self.loaded_catalog.save(name=name, data=updated_catalog.load(name))
        logging.info("Kedro Mlflow context loaded")

    @pynavio.prediction_call
    def predict(self, context, model_input):
        predictions = super().predict(context, model_input)
        return predictions


class NavioHooks(MlflowHook):
    @hook_impl
    def after_pipeline_run(
            self,
            run_params: Dict[str, Any],
            pipeline: Pipeline,
            catalog: DataCatalog,
    ) -> None:
        """Hook to be invoked after a pipeline runs.
        Args:
            run_params: The params needed for the given run.
                Should be identical to the data logged by Journal.
                # @fixme: this needs to be modelled explicitly as code, instead of comment
                Schema: {
                    "project_path": str,
                    "env": str,
                    "kedro_version": str,
                    "tags": Optional[List[str]],
                    "from_nodes": Optional[List[str]],
                    "to_nodes": Optional[List[str]],
                    "node_names": Optional[List[str]],
                    "from_inputs": Optional[List[str]],
                    "load_versions": Optional[List[str]],
                    "pipeline_name": str,
                    "extra_params": Optional[Dict[str, Any]],
                }
            pipeline: The ``Pipeline`` that was run.
            catalog: The ``DataCatalog`` used during the run.
        """
        def format_path(path):
            """Hot fix to remove file:// prefix from mlflow paths"""
            ### Todo: handle in pynavio
            return Path(path.replace('file://', ''))

        if self._is_mlflow_enabled:
            if isinstance(pipeline, PipelineML):
                artifacts_path = format_path(mlflow.get_artifact_uri())
                module_path = pynavio.get_module_path(__projectSnake__)

                kedro_model = KedroNavioPipelineModel(
                    pipeline=pipeline.inference,
                    catalog=catalog,
                    input_name=pipeline.input_name,
                    **pipeline.kpm_kwargs,
                )
                artifacts = kedro_model.extract_pipeline_artifacts(artifacts_path)
                navio_params = catalog.load("params:navio")
                # Add navio example request
                try:
                    artifacts['example_request'] = getattr(catalog.datasets,
                                                           navio_params.get("example_request"))._filepath
                except AttributeError as e:
                    logging.error(f"Navio model requires an example request.\n"
                                  f"Ensure that {navio_params.get('example_request')} is a valid data catalog entry\n"
                                  f"or call pynavio.make_example_request in a node and add it to catalog.")
                    raise e

                # Preapare navio datasets
                navio_datasets = navio_params.get("datasets")
                if isinstance(navio_datasets, list):
                    navio_dataset_dict = {name: getattr(catalog.datasets, name)._filepath.as_uri() for name in
                                          navio_datasets}
                elif navio_datasets is not None:
                    navio_dataset_dict = {navio_datasets: getattr(catalog.datasets, navio_datasets)._filepath.as_uri()}
                else:
                    navio_dataset_dict = None

                # Prepare dependencies
                pip_packages = navio_params.get("pip_packages")
                if pip_packages is None:
                    pip_packages = pynavio.infer_external_dependencies(module_path)

                code_path = [pynavio.get_module_path(__projectSnake__)]
                outpath = os.path.join(artifacts_path, 'navio_model')
                pynavio.mlflow.to_navio(kedro_model,
                                        path=outpath,
                                        pip_packages=pip_packages,
                                        conda_packages=navio_params.get("conda_packages"),
                                        code_path=code_path,
                                        artifacts=artifacts,
                                        explanations=navio_params.get("explanations"),
                                        oodd=navio_params.get("oodd"),
                                        dataset=navio_dataset_dict,
                                        num_gpus=navio_params.get("num_gpus", 0)
                                        )

                # use a regular print to avoid the line breaks of the logger to enable copying the uri
                print(f"Navio model saved to {artifacts_path}/navio_model.zip")

                # Upload model zip to navio
                if navio_params.get("upload_name") is not None:
                    upload_model(navio_params, artifacts_path)

            # Close the mlflow active run at the end of the pipeline to avoid interactions with further runs
            mlflow.end_run()

        else:
            switch_catalog_logging(catalog, True)


def upload_model(navio_params, artifacts_path):
    """Uploads the model zip to navio using the pynavio Client.
    Args:
        navio_params: The navio parameter group from the parameters.yml file.
        artifacts_path: The path to the model zip inferred by mlflow.get_artifact_uri().
    """
    credentials = get_credentials()
    upload_params = credentials.get(navio_params.get("credentials"))

    required_keys = {"navio_url", "api_token", "workspace_id", "use_case_id"}
    actual_keys = set(upload_params.keys())
    missing_keys = required_keys.difference(actual_keys)
    if len(missing_keys) > 0:
        raise KeyError(f"The following keys are missing in credentials: {missing_keys}")

    zip_path = os.path.join(artifacts_path, 'navio_model.zip')
    client = Client(upload_params.get("navio_url"),
                    upload_params.get("api_token")
                    )
    client.upload_model_zip(zip_path,
                            upload_params.get("workspace_id"),
                            upload_params.get("use_case_id"),
                            navio_params.get("upload_name")
                            )
    logging.info("Navio model uploaded successfully")
