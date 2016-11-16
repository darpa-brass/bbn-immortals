# noinspection PyPep8Naming
class ServerConfig:
    """
    :type url: str
    :type port: int
    """

    @classmethod
    def from_dict(cls, d):
        return cls(
                d['url'],
                d['port']
        )

    def __init__(self,
                 url,
                 port):
        self.url = url
        self.port = port


# noinspection PyPep8Naming
class AnalyticsConfig:
    """
    :type target: str
    :type verbosity: str
    :type url: str
    :type port: int
    """

    @classmethod
    def from_dict(cls, d):
        return cls(
                d['target'],
                d['verbosity'],
                d['url'],
                d['port']
        )

    def __init__(self,
                 target,
                 verbosity,
                 url,
                 port):
        self.target = target
        self.verbosity = verbosity
        self.url = url
        self.port = port


# noinspection PyPep8Naming
class TestSettings:
    """
    :type imageBroadcastIntervalMS: int
    :type imageBroadcastDelayMS: int
    """

    @classmethod
    def from_dict(cls, d):
        return cls(
                d['imageBroadcastIntervalMS'],
                d['imageBroadcastDelayMS']
        )

    def __init__(self,
                 imageBroadcastIntervalMS,
                 imageBroadcastDelayMS):
        self.imageBroadcastIntervalMS = imageBroadcastIntervalMS
        self.imageBroadcastDelayMS = imageBroadcastDelayMS


# noinspection PyPep8Naming
class ATAKLiteConfig:
    """
    :type callsign: str
    :type broadcastSA: bool
    :type latestSABroadcastIntervalMS: int
    :type latestSABroadcastDelayMS: int
    :type userInterface: str
    :type logReceivedLocationUpdates: bool
    :type logOwnLocationUpdates: bool
    :type loadReceivedLocationUpdatesFromLog: true
    :type locationLogExternalStoragePath: str
    :type serverConfig ServerConfig
    :type analyticsConfig: AnalyticsConfig
    :type testSettings: TestSettings
    """

    @classmethod
    def from_dict(cls, d):
        return cls(
                d['callsign'],
                d['broadcastSA'],
                d['latestSABroadcastIntervalMS'],
                d['latestSABroadcastDelayMS'],
                d['userInterface'],
                d['logReceivedLocationUpdates'],
                d['logOwnLocationUpdates'],
                d['loadReceivedLocationUpdatesFromLog'],
                d['loadOwnLocationUpdatesFromLog'],
                d['locationLogExternalStoragePath'],
                ServerConfig.from_dict(d['serverConfig']),
                AnalyticsConfig.from_dict(d['analyticsConfig']),
                TestSettings.from_dict(d['testSettings'])
        )

    def __init__(self,
                 callsign,
                 broadcastSA,
                 latestSABroadcastIntervalMS,
                 latestSABroadcastDelayMS,
                 userInterface,
                 logReceivedLocationUpdates,
                 logOwnLocationUpdates,
                 loadReceivedLocationUpdatesFromLog,
                 locationLogExternalStoragePath,
                 serverConfig,
                 analyticsConfig,
                 testSettings):
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
class LocationProvider:
    """
    :type country: str
    :type direction: str
    :type degreeChangePerSecond: float
    """

    @classmethod
    def from_dict(cls, d):
        return cls(
                d['country'],
                d['direction'],
                d['degreeChangePerSecond']
        )

    def __init__(self,
                 country,
                 direction,
                 degreeChangePerSecond):
        self.country = country
        self.direction = direction
        self.degreeChangePerSecond = degreeChangePerSecond
