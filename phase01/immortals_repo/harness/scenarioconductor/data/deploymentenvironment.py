class DeploymentEnvironment:
    """
    :type identifier: str
    :type: sdk_level: str
    :type deployment_platform: str
    """

    @classmethod
    def from_dict(cls, d):
        return cls(
                d['identifier'],
                d['sdkLevel'],
                d['deploymentPlatform']
        )

    def __init__(self, identifier, config):
        self.identifier = identifier
        self.sdk_level = config['sdkLevel']
        self.deployment_platform = config['deploymentPlatform']