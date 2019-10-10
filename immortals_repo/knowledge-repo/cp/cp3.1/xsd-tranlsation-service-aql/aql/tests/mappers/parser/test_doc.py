from pytest import fixture
# from mappers.utils import tree_print


def _check_children(expected_names, element):
    children_names = {i.name for i in element.children}
    assert expected_names.issubset(children_names)


@fixture
def simple_doc(shared_datadir, get_main_element_single):
    with open(shared_datadir / 'simple.xsd', 'r') as file_content:
        return get_main_element_single(file_content.read())['element']


def test_parser_must_return_expected_elements(simple_doc):
    assert simple_doc.name == 'Root'

    # Check if root as the following children
    _check_children({
        'Network',
        'Owner',
        'ReadOnly'
    }, simple_doc)

    # Check if network has expected children
    _check_children({
        'DeliveryClass',
        'Owner',
        'ReadOnly'
    }, simple_doc.get_child_by_name('Network'))


def test_parser_must_parse_expected_attributes(simple_doc):
    # Readonly attr inside root
    expected_attrs = {
        'default': 'true',
        'minoccurs': '1'
    }

    assert simple_doc.get_child_by_name('ReadOnly').attrs == expected_attrs

    # Delivery Class inside network
    delivery_class = simple_doc.get_child_by_name('Network').get_child_by_name('DeliveryClass')
    expected_attrs = {
        'default': 'BestEffort',
    }

    assert delivery_class.attrs == expected_attrs


def test_parser_must_add_restriction_enum_to_type(simple_doc):
    delivery_class = simple_doc.get_child_by_name('Network').get_child_by_name('DeliveryClass')

    expected_restriction_enum = ['Voice', 'FlightSafety', 'BestEffort']
    assert delivery_class.element_type.restriction_enum == expected_restriction_enum


def test_parser_must_parse_type_annotation(simple_doc):
    expected = '\n'.join([
        'Our root type with a simple documentation',
        'This is a second line for our documentation'
    ])

    assert simple_doc.element_type.annotation == expected


def test_parser_must_parse_choices(simple_doc):
    network = simple_doc.get_child_by_name('Network')

    expected_choices = [
        [network.get_child_by_name('IPV4'), network.get_child_by_name('IPV6')]
    ]

    assert network.element_type.choices_groups == expected_choices


def test_parser_must_parse_union(simple_doc):
    destination = simple_doc.get_child_by_name('Network').get_child_by_name('DestinationAddress')

    assert destination.element_type.name == 'IPv4Address'
