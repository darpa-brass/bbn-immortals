from pytest import fixture
from lxml import etree as et

from entities import Element, ElementType
import constants


@fixture
def lxml_root():
    return et.Element(
        et.QName(constants.XSL_NS, 'stylesheet'),
        version='1.0')


@fixture
def string_type():
    return ElementType('xsd:string')


@fixture
def complex_type(string_type):
    return ElementType('EmployeeType',
                       children=[Element('Name', element_type=string_type)])


def test_create_new_element(string_type):
    f = Element(name='Name', element_type=string_type)

    assert f.name == 'Name'
    assert f.element_type == string_type
    assert f.children == []
    assert str(f) == 'Name-xsd:string'
    assert f.self_hash == hash(str(f))
    assert f.type_hash == hash(f.element_type)


def test_element_with_in_operator(string_type):
    fields = [Element(name='Name', element_type=string_type)]

    f = Element(name='Name', element_type=string_type)
    f2 = Element(name='Surname', element_type=string_type)

    assert f in fields
    assert f2 not in fields


def test_element_with_same_values_must_be_equal(string_type):
    f = Element(name='Model', element_type=string_type)
    f2 = Element(name='Model', element_type=string_type)

    assert f == f2
    assert hash(f) == hash(f2)


def test_element_of_complex_type_with_same_values_must_be_equal(complex_type):
    f = Element(name='Employee', element_type=complex_type)
    f2 = Element(name='Employee', element_type=complex_type)

    assert f == f2
    assert hash(f) == hash(f2)


def test_element_changed_method_must_recalculate_hash(complex_type, string_type):
    f = Element(name='Employee', element_type=complex_type)
    current_hash = hash(f)

    complex_type.children = complex_type.children + [Element('Surname', element_type=string_type)]
    assert hash(f) == current_hash

    f.changed()

    assert hash(f) != current_hash


def test_element_similarity_must_return_expected_ratio(string_type):
    e = Element(name='Name', element_type=string_type)
    new_e = Element(name='NewName', element_type=string_type)

    assert e.similarity(new_e) == 0.7272727272727273

    e = Element(name='Name', element_type=string_type)
    new_e = Element(name='NewOther', element_type=string_type)

    assert e.similarity(new_e) == 0.3333333333333333


def test_element_get_child_by_name(complex_type, string_type):
    surname = Element('Surname', element_type=string_type)

    f = Element(name='Employee', element_type=complex_type)
    f.add_child(surname)

    assert f.get_child_by_name('Surname') == surname
    assert f.get_child_by_name('SurnameNotFound') is None


def test_element_add_child_must_add_properly(complex_type, string_type):
    name_elem = complex_type.children[0]
    surname_elem = Element('Surname', element_type=string_type)

    f = Element(name='Employee', element_type=complex_type)
    f.add_child(surname_elem)

    assert f.children == [
        name_elem,
        surname_elem
    ]


def test_element_remove_child_must_remove_properly(complex_type, string_type):
    name_elem = complex_type.children[0]
    surname_elem = Element('Surname', element_type=string_type)

    complex_type.add_child(surname_elem)

    f = Element(name='Employee', element_type=complex_type)
    f.remove_child(surname_elem)

    assert f.children == [name_elem]


def test_element_to_lxml_element_must_return_expected_tag(string_type, lxml_root):
    el = Element('Surname', element_type=string_type)

    el.to_lxml_element(lxml_root)

    result = et.tostring(lxml_root).decode()

    expected = ('<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">'
                '<Surname/>'
                '</xsl:stylesheet>')

    assert result == expected


def test_element_to_lxml_element_with_children_must_return_expected_tags(complex_type, string_type, lxml_root):
    complex_type.add_child(Element('Surname', element_type=string_type))
    f = Element('Employee', element_type=complex_type)

    f.to_lxml_element(lxml_root)

    result = et.tostring(lxml_root).decode()

    expected = ('<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">'
                '<Employee><Name/><Surname/></Employee>'
                '</xsl:stylesheet>')

    assert result == expected


def test_element_to_lxml_element_with_default_attr_must_return_expected_tag_with_value(string_type, lxml_root):
    el = Element('Surname', element_type=string_type, attrs={'default': 'Silva'})
    el.to_lxml_element(lxml_root)

    result = et.tostring(lxml_root).decode()

    expected = ('<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">'
                '<!--Created with default value--><Surname>Silva</Surname>'
                '</xsl:stylesheet>')

    assert result == expected


def test_element_to_lxml_element_with_restriction_enum_filled_in_type_must_return_expected_tag_with_value(
        string_type, lxml_root):
    string_type.restriction_enum = ('Perez', 'Silva')
    el = Element('Surname', element_type=string_type)
    el.to_lxml_element(lxml_root)

    result = et.tostring(lxml_root).decode()

    expected = ('<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">'
                '<!--CHECK! Created with first element of restriction from type spec--><Surname>Perez</Surname>'
                '</xsl:stylesheet>')

    assert result == expected
