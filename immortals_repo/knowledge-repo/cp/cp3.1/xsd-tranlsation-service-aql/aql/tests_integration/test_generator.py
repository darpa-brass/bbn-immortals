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
    first_result, second_result = simple_versions

    first_tree = first_result['element']
    second_tree = second_result['element']

    compare_result = compare(first_tree, second_tree)

    xslt = generate_xslt(first_tree, second_tree, compare_result, first_result['namespaces'], 'MDL19.xsd')

    with open(shared_datadir / 'SIMPLE_MDL_RESULT.xslt', 'r') as mdl_result:
        xslt_expected = mdl_result.read()

    xslt = xslt.strip()
    xslt_expected = xslt_expected.strip()

    assert xslt == xslt_expected


def test_generate_xslt_with_simple_example_must_add_a_new_target_namespace(simple_versions, shared_datadir):
    first_result, second_result = simple_versions

    first_tree = first_result['element']
    second_tree = second_result['element']

    compare_result = compare(first_tree, second_tree)

    xslt = generate_xslt(first_tree, second_tree, compare_result, first_result['namespaces'], 'MDL19.xsd')

    assert '<xsl:template match="/mdl:MDLRoot/@xsi:schemaLocation">' in xslt

    relocation = ('<xsl:attribute name="xsi:schemaLocation">'
                  'http://inetprogram.org/projects/MDL MDL19.xsd</xsl:attribute>')
    assert relocation in xslt


def test_generate_xslt_with_full_example(full_versions, shared_datadir):
    first_result, second_result = full_versions

    first_tree = first_result['element']
    second_tree = second_result['element']

    compare_result = compare(first_tree, second_tree)

    xslt = generate_xslt(first_tree, second_tree, compare_result, first_result['namespaces'], 'MDL19.xsd')

    with open(shared_datadir / 'MDL_17_x_19_result.xslt', 'r') as mdl_result:
        xslt_expected = mdl_result.read()

    xslt = xslt.strip()
    xslt_expected = xslt_expected.strip()

    assert xslt == xslt_expected
