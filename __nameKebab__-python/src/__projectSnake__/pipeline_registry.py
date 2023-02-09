"""Project pipelines."""
from typing import Dict
from kedro.pipeline import Pipeline, node
# https://kedro.readthedocs.io/en/stable/nodes_and_pipelines/modular_pipelines.html#modular-pipelines
from kedro.pipeline.modular_pipeline import pipeline

""" 
    Hello world example pipeline.
    Delete this and create a new pipeline with:

        kedro pipeline create <pipeline_name>
        
    This will create the pipeline structure in src.pipelines
"""
hello_world_pipeline = Pipeline(
    [
        node(
            name="Hello_world",
            func=lambda env: f"hello world! You are running the environment {env} from conf/parameters.yml ",
            inputs="params:env",
            outputs="message"),
        node(
            name="print_message",
            func=lambda message: print(message),
            inputs="message",
            outputs=None)
    ])


def register_pipelines() -> Dict[str, Pipeline]:
    """Register the project's pipelines.

    Returns:
        A mapping from a pipeline name to a ``Pipeline`` object.
    """
    return {"__default__": hello_world_pipeline}
