"""
This class encompasses the configuration file to be loaded onto the actual ATAKLite client.

It's placement on the client device should be /sdcard/ataklite/ATAKLite-Config.json
"""

from .serializable import Serializable


# noinspection PyPep8Naming
class ServerConfig(Serializable):
    _validator_values = {}

    def __init__(self,
                 url: str,
                 port: int):
        super().__init__()
        self.url = url
        self.port = port


# noinspection PyPep8Naming
class AnalyticsConfig(Serializable):
    _validator_values = {}

    def __init__(self,
                 target: str,
                 verbosity: str,
                 url: str,
                 port: int):
        super().__init__()
        self.target = target
        self.verbosity = verbosity
        self.url = url
        self.port = port


# noinspection PyPep8Naming
class TestSettings(Serializable):
    _validator_values = {}

    def __init__(self,
                 imageBroadcastIntervalMS: int,
                 imageBroadcastDelayMS: int):
        super().__init__()
        self.imageBroadcastIntervalMS = imageBroadcastIntervalMS
        self.imageBroadcastDelayMS = imageBroadcastDelayMS


# noinspection PyPep8Naming
class ATAKLiteConfig(Serializable):
    _validator_values = {}

    def __init__(self,
                 callsign: str,
                 broadcastSA: bool,
                 latestSABroadcastIntervalMS: int,
                 latestSABroadcastDelayMS: int,
                 userInterface: str,
                 logReceivedLocationUpdates: bool,
                 logOwnLocationUpdates: bool,
                 loadReceivedLocationUpdatesFromLog: bool,
                 locationLogExternalStoragePath: str,
                 serverConfig: ServerConfig,
                 analyticsConfig: AnalyticsConfig,
                 testSettings: TestSettings):
        super().__init__()
        self.callsign = callsign
        self.broadcastSA = broadcastSA
        self.latestSABroadcastIntervalMS = latestSABroadcastIntervalMS
        self.latestSABroadcastDelayMS = latestSABroadcastDelayMS
        self.userInterface = userInterface
        self.logReceivedLocationUpdates = logReceivedLocationUpdates
        self.logOwnLocationUpdates = logOwnLocationUpdates
        self.loadReceivedLocationUpdatesFromLog = loadReceivedLocationUpdatesFromLog
        self.locationLogExternalStoragePath = locationLogExternalStoragePath
        self.serverConfig = serverConfig
        self.analyticsConfig = analyticsConfig
        self.testSettings = testSettings


# noinspection PyPep8Naming
class LocationProvider(Serializable):
    _validator_values = {}

    def __init__(self,
                 country: str,
                 direction: str,
                 degreeChangePerSecond: float):
        super().__init__()
        self.country = country
        self.direction = direction
        self.degreeChangePerSecond = degreeChangePerSecond
