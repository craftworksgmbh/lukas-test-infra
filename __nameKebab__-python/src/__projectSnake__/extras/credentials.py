from kedro.config import ConfigLoader, MissingConfigException
from kedro.framework.project import settings


def get_credentials():
    conf_loader = ConfigLoader(conf_source=settings.CONF_SOURCE, env="local")
    try:
        credentials = conf_loader.get("credentials*", "credentials*/**")
    except MissingConfigException:
        credentials = {}
    return credentials
