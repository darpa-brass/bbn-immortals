from pytest import fixture

from mappers.diff import compare
from generator import generate_xslt


@fixture
def simple_versions(shared_datadir, get_main_element_single):
    with open(shared_datadir / 'SIMPLE_MDL_v0_8_17.xsd', 'r') as xsd_file:
        first = get_main_element_single(xsd_file.read())

    with open(shared_datadir / 'SIMPLE_MDL_v0_8_19.xsd', 'r') as xsd_file:
        second = get_main_element_single(xsd_file.read())

    return first, second


@fixture
def full_versions(shared_datadir, get_main_element_single):
    with open(shared_datadir / 'MDL_v0_8_17.xsd', 'r') as xsd_file:
        first = get_main_element_single(xsd_file.read())

    with open(shared_datadir / 'MDL_v0_8_19.xsd', 'r') as xsd_file:
        second = get_main_element_single(xsd_file.read())

    return first, second


def test_generate_xslt_with_simple_example(simple_versions, shared_datadir):
    first_tree, second_tree = simple_versions

    compare_result = compare(first_tree, second_tree)

    xslt = generate_xslt(first_tree, second_tree, compare_result)

    with open(shared_datadir / 'SIMPLE_MDL_RESULT.xslt', 'r') as mdl_result:
        xslt_expected = mdl_result.read()

    xslt = xslt.strip()
    xslt_expected = xslt.strip()

    assert xslt == xslt_expected


def test_generate_xslt_with_full_example(full_versions, shared_datadir):
    first_tree, second_tree = full_versions

    compare_result = compare(first_tree, second_tree)

    xslt = generate_xslt(first_tree, second_tree, compare_result)

    with open(shared_datadir / 'MDL_17_x_19_result.xslt', 'r') as mdl_result:
        xslt_expected = mdl_result.read()

    xslt = xslt.strip()
    xslt_expected = xslt.strip()

    assert xslt == xslt_expected
