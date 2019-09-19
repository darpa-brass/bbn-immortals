# import pprint
from pytest import fixture, mark

from mappers import diff


@fixture
def simple_mdl_versions(shared_datadir, get_main_element_single):
    with open(shared_datadir / 'SIMPLE_MDL_v0_8_17.xsd', 'r') as xsd_file:
        first = get_main_element_single(xsd_file.read())['element']

    with open(shared_datadir / 'SIMPLE_MDL_v0_8_19.xsd', 'r') as xsd_file:
        second = get_main_element_single(xsd_file.read())['element']

    return (first, second)


@fixture
def full_mdl_versions(shared_datadir, get_main_element_single):
    with open(shared_datadir / 'MDL_v0_8_17.xsd', 'r') as xsd_file:
        first = get_main_element_single(xsd_file.read())['element']

    with open(shared_datadir / 'MDL_v0_8_19.xsd', 'r') as xsd_file:
        second = get_main_element_single(xsd_file.read())['element']

    return (first, second)


def test_simple_mdl_versions_must_return_expected_compare_result(simple_mdl_versions):
    v17_tree, v19_tree = simple_mdl_versions

    result = diff.compare(v17_tree, v19_tree)

    expected_result = {
        'additions': ['/MDLRoot/NetworkDomains/Network/NetworkNode/InternalStructure',
                      '/MDLRoot/NetworkDomains/Domain'],
        'relocations': [('/MDLRoot/NetworkDomains/Network/NetworkNode/Routes',
                         '/MDLRoot/NetworkDomains/Network/NetworkNode/InternalStructure/Module/Routes'),
                        ('/MDLRoot/DatabaseID', '/MDLRoot/NetworkDomains/DatabaseID')],
        'removals': sorted([
            '/MDLRoot/NetworkDomains/Network/NetworkNode/NetworkName',
            '/MDLRoot/NetworkDomains/Network/NetworkNode/Routes',
            '/MDLRoot/DatabaseID',
        ]),
        'renames': [('/MDLRoot/ConfigurationVersion', '/MDLRoot/ConfigVersion')]
    }

    result['removals'].sort()

    assert result == expected_result


def test_full_mdl_versions_must_return_expected_compare_result(full_mdl_versions):
    v17_tree, v19_tree = full_mdl_versions

    result = diff.compare(v17_tree, v19_tree)

    expected_keys = {'renames', 'additions', 'relocations', 'removals'}
    current_keys = set(result.keys())

    assert current_keys == expected_keys

    assert len(result['additions']) == 74
    assert len(result['removals']) == 70
    assert len(result['relocations']) == 61
    assert len(result['renames']) == 4


@mark.skip(reason='TODO: think about intersections between additions and removals, this must not happen')
def test_full_mdl_versions_must_has_no_intersections_between_additions_and_removals(full_mdl_versions):
    v17_tree, v19_tree = full_mdl_versions

    result = diff.compare(v17_tree, v19_tree)

    # Checking if we have some item in more than one operation
    additions_set = set(result['additions'])
    removals_set = set(result['removals'])

    intersect = additions_set & removals_set

    assert len(intersect) == 0
