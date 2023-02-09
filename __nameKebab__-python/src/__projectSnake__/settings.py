"""Project settings. There is no need to edit this file unless you want to change values
from the Kedro defaults. For further information, including these default values, see
https://kedro.readthedocs.io/en/stable/kedro_project_setup/settings.html."""

# Instantiated project hooks.
from __projectSnake__.extras.navio import NavioHooks
HOOKS = (NavioHooks(),)

# Installed plugins for which to disable hook auto-registration.
DISABLE_HOOKS_FOR_PLUGINS = ("kedro-mlflow",)

# Class that manages storing KedroSession data.
# from kedro.framework.session.store import ShelveStore
# SESSION_STORE_CLASS = ShelveStore
# Keyword arguments to pass to the `SESSION_STORE_CLASS` constructor.
# SESSION_STORE_ARGS = {
#     "path": "./sessions"
# }

# Class that manages Kedro's library components.
# from kedro.framework.context import KedroContext
# CONTEXT_CLASS = KedroContext

# Directory that holds configuration.
# CONF_SOURCE = "conf"

# Class that manages how configuration is loaded.
from kedro.config import TemplatedConfigLoader
class RuntimeTemplatedConfigLoader(TemplatedConfigLoader):
    """ Adaptation of tempalted config loader to also include runtime parameters"""
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        if self.runtime_params is not None:
            self._config_mapping.update(self.runtime_params)

CONFIG_LOADER_CLASS = RuntimeTemplatedConfigLoader
CONFIG_LOADER_ARGS = {
    "globals_pattern": "*parameters.yml",
}

# Class that manages the Data Catalog.
# from kedro.io import DataCatalog
# DATA_CATALOG_CLASS = DataCatalog
