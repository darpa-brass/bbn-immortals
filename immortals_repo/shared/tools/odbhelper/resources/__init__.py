from pkg_resources import resource_string


def load_s6_basic():
    return resource_string('odbhelper.resources.dummy_data', 's6_basic.json').decode()


def load_s6_advanced():
    return resource_string('odbhelper.resources.dummy_data', 's6_advanced.json').decode()
