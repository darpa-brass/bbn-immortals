from .applicationconfig import ApplicationConfig


# noinspection PyPep8Naming
class ScenarioConfiguration:
    """
    :type scenarioIdentifier: str
    :type durationMS: int
    :type deploymentApplications: list[data.applicationconfig.ApplicationConfig]
    :type validatorIdentifiers: list[str]
    """

    @classmethod
    def from_dict(cls, j, parent_config, value_pool=None):
        """
        :type j dict
        :rtype Scenario
        """
        r = ScenarioConfiguration(
            scenarioIdentifier=j['scenarioIdentifier'],
            durationMS=j['durationMS'],
            deploymentApplications=None,
            validatorIdentifiers=j['validatorIdentifiers'],
            parent_config=parent_config
        )
        # apps = map(lambda app: ApplicationConfig.from_json(app, parent_config),
        #            j['deploymentApplications'])
        r.deploymentApplications = map(lambda app: ApplicationConfig.from_json(app, r, value_pool=value_pool),
                                       j['deploymentApplications'])
        return r

    def __init__(self,
                 scenarioIdentifier,  # type: str
                 durationMS,  # type: int
                 deploymentApplications,  # type: list
                 validatorIdentifiers,  # type: list,
                 parent_config
                 ):
        self.scenarioIdentifier = scenarioIdentifier  # type:str
        self.durationMS = durationMS  # type:int
        self.deploymentApplications = deploymentApplications  # type: list
        self.validatorIdentifiers = validatorIdentifiers  # type:list
        self.parent_config = parent_config
