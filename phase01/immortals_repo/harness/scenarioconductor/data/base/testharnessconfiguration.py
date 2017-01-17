class TestHarnessConfiguration:
    """
    :type enabled: bool
    :type protocol: str
    :type url: str
    :type port: int
    """

    @classmethod
    def from_dict(cls, d):
        return cls(
            enabled=d['enabled'],
            protocol=d['protocol'],
            url=d['url'],
            port=d['port']
        )

    def __init__(self,
                 enabled,
                 protocol,
                 url,
                 port
                 ):
        self.enabled = enabled
        self.protocol = protocol
        self.url = url
        self.port = port
