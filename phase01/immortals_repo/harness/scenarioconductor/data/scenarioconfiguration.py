#
# {
#     "scenarioConfiguration": {
#         "sessionIdentifier" : "B4F3692B0665",
#         "server": {
#             "bandwidth": 1000
#         },
#         "clients": [
#             {
#                 "imageBroadcastIntervalMS": "5000",
#                 "latestSABroadcastIntervalMS": "250",
#                 "count": 2
#             }
#         ]
#
#     },
#     "executionMode": "RUN_SCENARIO",
#     "scenarioIdentifiers" : [
#         "client-test-images"
#     ],
#     "validate": true,
#     "timeout": 60,
#     "keepEnvironmentRunning": false,
#     "wipeExistingEnvironment": true,
#     "displayEmulatorGui": false,
#
#     "wipeExistingEnvironment": false,
# }


class MartiServer:
    """
    :type bandwidth int
    """

    @classmethod
    def from_dict(cls, d):
        # type:(dict) -> MartiServer
        return cls(
                d['bandwidth']
        )

    def __init__(self,
                 bandwidth  # type: int
                 ):
        self.bandwidth = bandwidth  # type: int


class ATAKLiteClient:
    """
    :type image_broadcast_interval_ms: int
    :type latestsa_broadcast_interval_ms: int
    :type count: int
    :type present_resources: list[str]
    :type required_properties: list[str]
    """

    @classmethod
    def from_dict(cls, d):
        # type:(dict) -> ATAKLiteClient
        return cls(
                d['imageBroadcastIntervalMS'],
                d['latestSABroadcastIntervalMS'],
                d['count'],
                d['presentResources'],
                d['requiredProperties']
        )

    def __init__(self,
                 image_broadcast_interval_ms,
                 latestsa_broadcast_interval_ms,
                 count,
                 present_resources,
                 required_properties
                 ):
        self.image_broadcast_interval_ms = image_broadcast_interval_ms
        self.latestsa_broadcast_interval_ms = latestsa_broadcast_interval_ms
        self.count = count
        self.present_resources = present_resources
        self.required_properties = required_properties


class ScenarioConfiguration:
    """
    :type session_identifier: str
    :type server: MartiServer
    :type clients: list[ATAKLiteClient]
    """

    def __init__(self,
                 session_identifier,  # type: str
                 server,  # type: MartiServer
                 clients  # type:ATAKLiteClient
                 ):
        self.session_identifier = session_identifier  # type: str
        self.server = server  # type: MartiServer
        self.clients = clients  # type:ATAKLiteClient
        self.server.parent_config = self
        for c in self.clients:
            c.parent_config = self

    @classmethod
    def from_dict(cls, d):
        return cls(
                d['sessionIdentifier'],
                MartiServer.from_dict(d['server']),
                map(lambda c: ATAKLiteClient.from_dict(c), d['clients'])
        )

# def main():
#     with open('sample_configuration.json', 'r') as f:
#         sc_j = json.load(f)
#         sc = ScenarioConfiguration.from_dict(sc_j)
#
#     src = ScenarioRunnerConfiguration.from_scenario_configuration(sc, 'validation')
#     # print json.dumps(src, default=lambda o: o.__dict__, sort_keys=True, indent=4)
#     from scenariorunner import  ScenarioRunner
#     sr = ScenarioRunner(src)
#
#     # sr.execute_scenario()
#
#
# if __name__ == '__main__':
#     main()
