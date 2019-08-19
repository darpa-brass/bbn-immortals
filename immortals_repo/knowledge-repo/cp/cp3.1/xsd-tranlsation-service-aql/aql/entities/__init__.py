from difflib import SequenceMatcher
from lxml import etree as et

from constants import XSL_NS


class ElementType(object):

    def __init__(self, name, children=None):
        self.name = name
        self._set_children(children)
        self.restriction_enum = []

    # TODO add tests
    def fill_with(self, other):
        '''
        Fill this element type with other
        '''

        self._children = other._children
        self.name = other.name
        self.restriction_enum = other.restriction_enum

        self.changed()

    def _get_children(self):
        return self._children

    def __str__(self):
        return self.name

    def __eq__(self, other):
        return hash(self) == hash(other)

    def __hash__(self):
        return self._hash

    def changed(self):
        self.calc_hash()

    def get_child_by_name(self, name):
        for i in self.children:
            if i.name == name:
                return i

        return None

    def remove_child(self, elem):
        self._children.remove(elem)
        self.changed()

    def add_child(self, elem):
        self.children = self.children + [elem]

    def calc_hash(self):
        # Calculate my hash (relative with my children) to be used
        # to compare with other values
        # As it'll not change, it will be very fast to detect witch part of our
        # tree is different from other

        self._hash = hash('-'.join(
            [str(hash(str(self)))] + [str(hash(i)) for i in self._children]
        ))

    def _set_children(self, val):
        # Always sort our children to calculate hash properly
        self._children = sorted(val or [], key=str)
        self.changed()

    children = property(_get_children, _set_children)


class ElementAnyType(ElementType):
    '''
    Defines an element that matches any type
    In a practical way, it's common to ignore this element type in tree comparissons
    '''
    pass


ELEMENT_ANY_TYPE = ElementAnyType('ANY')


class Element(object):

    # TODO Add slots

    def __init__(self, name, element_type, attrs=None):
        self.name = name
        self.copy_of = None
        self.attrs = attrs or {}

        self.element_type = element_type
        self.calc_hash()

    @property
    def children(self):
        return self.element_type.children

    @property
    def self_hash(self):
        return hash(str(self))

    @property
    def type_hash(self):
        return hash(self.element_type)

    def similarity(self, other):
        return SequenceMatcher(None, self.name, other.name).ratio()

    def get_child_by_name(self, name):
        return self.element_type.get_child_by_name(name)

    def remove_child(self, elem):
        self.element_type.remove_child(elem)
        self.changed()

    def add_child(self, elem):
        self.element_type.add_child(elem)
        self.changed()

    def changed(self):
        # First, notify field type about changes to recalculate its hash
        self.element_type.changed()
        self.calc_hash()

    def calc_hash(self):
        # Calculate my hash, considering my name and type
        self._hash = hash(str(self.self_hash) + '-' + str(self.type_hash))

    def __str__(self):
        return f'{self.name}-{self.element_type}'

    def __eq__(self, other):
        return hash(self) == hash(other)

    def __hash__(self):
        return self._hash

    # TODO: Add unit tests
    def to_lxml_element(self, parent):
        if self.copy_of:
            return et.SubElement(
                parent,
                et.QName(XSL_NS, 'copy-of'),
                select=self.copy_of)

        element = et.SubElement(parent, self.name)

        for child in self.children:
            child.to_lxml_element(element)

        if self.attrs.get('default'):
            element.text = self.attrs.get('default')
            element.addprevious(et.Comment('Created with default value'))
        elif self.element_type.restriction_enum:
            element.text = self.element_type.restriction_enum[0]
            element.addprevious(et.Comment('CHECK! Created with first element of restriction from type spec'))

        return element
