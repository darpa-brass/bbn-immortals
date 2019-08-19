from entities import ElementType, Element


def test_create_element_type():
    f = ElementType(name='xsd:boolean')
    assert f.name == 'xsd:boolean'
    assert f.children == []
    assert str(f) == 'xsd:boolean'


def test_element_type_must_have_same_hash_of_same_type_name():
    f = ElementType(name='xsd:boolean')
    f2 = ElementType(name='xsd:boolean')
    assert hash(f) == hash(f2)


def test_element_type_with_in_operator():
    elements = [ElementType(name='xsd:string')]

    f = ElementType(name='xsd:string')
    f2 = ElementType(name='xsd:token')

    assert f in elements
    assert f2 not in elements


def test_element_type_with_same_elements_must_be_equal():
    element_type = ElementType('xsd:string')
    children = [Element(name='Name', element_type=element_type)]

    f = ElementType(name='ModelType', children=children)
    f2 = ElementType(name='ModelType', children=children)

    assert f == f2
    assert hash(f) == hash(f2)


def test_element_type_same_children_in_different_order_must_have_same_hash():
    element_type = ElementType('xsd:string')
    child1 = Element(name='Name', element_type=element_type)
    child2 = Element(name='Owner', element_type=element_type)

    f = ElementType(name='ModelType', children=[child1, child2])
    f2 = ElementType(name='ModelType', children=[child2, child1])

    assert f == f2
    assert hash(f) == hash(f2)


def test_element_type_change_children_must_recalculate_hash():
    element_type = ElementType('xsd:string')
    children = [Element(name='Name', element_type=element_type)]

    f = ElementType(name='ModelType', children=children)
    current_hash = hash(f)

    owner = Element(name='Owner', element_type=element_type)
    f.add_child(owner)

    assert hash(f) != current_hash


def test_element_type_add_child_must_add_item_properly():
    element_type = ElementType('xsd:string')
    children = [Element(name='Name', element_type=element_type)]

    f = ElementType(name='ModelType', children=children)
    current_hash = hash(f)

    code = Element(name='Code', element_type=element_type)
    f.add_child(code)

    assert hash(f) != current_hash

    assert f.children[0] == code


def test_element_remove_child_must_remove_properly():
    element_type = ElementType('xsd:string')
    name_elem = Element(name='Name', element_type=element_type)
    surname_elem = Element(name='Surname', element_type=element_type)
    f = ElementType(name='ModelType', children=[name_elem, surname_elem])

    # Removing surname
    f.remove_child(surname_elem)

    assert f.children == [name_elem]

    # It's hash must be changed
    f_removed = ElementType(name='ModelType', children=[name_elem])
    assert f_removed == f


def test_element_get_child_by_name_must_returns_expected_child():
    element_type = ElementType('xsd:string')
    name_elem = Element(name='Name', element_type=element_type)
    surname_elem = Element(name='Surname', element_type=element_type)
    f = ElementType(name='ModelType', children=[name_elem, surname_elem])

    assert f.get_child_by_name('Surname') == surname_elem
    assert f.get_child_by_name('SurnameNotFound') is None


def test_element_fill_with_must_copy_all_data_from_other_element():
    element_type = ElementType('xsd:string')

    name_elem = Element(name='Name', element_type=element_type)
    surname_elem = Element(name='Surname', element_type=element_type)

    f = ElementType(name='ModelType', children=[name_elem, surname_elem])

    f2 = ElementType('')

    f2.fill_with(f)

    assert f2.name == f.name
    assert f2.children == f.children
