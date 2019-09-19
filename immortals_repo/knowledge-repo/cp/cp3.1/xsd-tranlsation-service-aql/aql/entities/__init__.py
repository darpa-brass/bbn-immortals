from copy import copy
from difflib import SequenceMatcher
from lxml import etree as et
import random

from constants import XSL_NS


class ElementType(object):
    # TODO add slots

    DEFAULTS = {
        'xsd:nonNegativeInteger': "1",

    }

    # An weighted arithmetic mean over these fields
    SIMILARITY_WEIGHT = {
        'annotation': 1.2,
        'children_names': 1,
        'name': 1.3
    }

    TOTAL_SIMILARITY_WEIGHT = sum(SIMILARITY_WEIGHT.values())

    def __init__(self, name, children=None):
        self.name = name
        self._set_children(children)
        self.restriction_enum = []
        self.attributes = []
        # self._children = []
        # self.children_original_order = []

        # Annotation with documentation about type (useful for similarity comparisson)
        self.annotation = None

    # TODO add tests
    def fill_with(self, other):
        '''
        Fill this element type with other
        '''

        self._children = list(other._children)
        self.children_original_order = list(other.children_original_order)
        self.name = other.name
        self.restriction_enum = other.restriction_enum
        self.annotation = other.annotation
        self.attributes = list(other.attributes)

        self.changed()

    def copy_from(self, other):
        self._children = list(other._children)
        self.children_original_order = list(other.children_original_order)
        self.name = other.name
        self.restriction_enum = other.restriction_enum
        self.annotation = other.annotation
        self.attributes = list(other.attributes)

        self.changed()

    def similarity(self, other):
        # Saving some process here when elements are equal in all attributes (considered in hash calculation)
        if self == other:
            return 1.0

        final_ratio = 0

        # Annotations
        final_ratio += self.SIMILARITY_WEIGHT['annotation'] * SequenceMatcher(
            None, self.annotation or '', other.annotation or '').ratio()

        # Children
        children_name = '-'.join([i.name for i in self.children])
        other_children_name = '-'.join([i.name for i in other.children])

        final_ratio += self.SIMILARITY_WEIGHT['children_names'] * SequenceMatcher(
            None, children_name, other_children_name).ratio()

        # Name
        final_ratio += self.SIMILARITY_WEIGHT['name'] * SequenceMatcher(
            None, self.name or '', other.name or '').ratio()

        return final_ratio / self.TOTAL_SIMILARITY_WEIGHT

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
        self.children_original_order.remove(elem)
        self.changed()

    def add_child(self, elem):
        self.children = self.children_original_order + [elem]

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
        self.children_original_order = copy(val) or []
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
    # An weighted arithmetic mean over these fields
    SIMILARITY_WEIGHT = {
        'element_type': 1,
        'name': 1
    }

    TOTAL_SIMILARITY_WEIGHT = sum(SIMILARITY_WEIGHT.values())

    # A threshold to determinate if elements are similar
    SIMILARITY_THRESHOLD = 0.7

    # TODO Add slots

    def __init__(self, name, element_type, attrs=None):
        self.name = name
        self.copy_of = None
        self.attrs = attrs or {}

        self.element_type = element_type
        self.calc_hash()

        # Url for namespace of this element type
        self.namespace = None

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
        if self == other:
            return 1.0

        final_ratio = 0

        # Element type similarity
        final_ratio += self.SIMILARITY_WEIGHT['element_type'] * \
                       self.element_type.similarity(other.element_type)

        final_ratio += self.SIMILARITY_WEIGHT['name'] * \
                       SequenceMatcher(None, self.name, other.name).ratio()

        return final_ratio / self.TOTAL_SIMILARITY_WEIGHT

    def is_similar(self, other):
        return self.similarity(other) >= self.SIMILARITY_THRESHOLD

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

    def lxml_name(self, namespaces=None):
        if namespaces and self.namespace in namespaces:
            return f'{namespaces[self.namespace]}:{self.name}'

        return self.name

    def to_lxml_element(self, parent):
        if not self.copy_of:
            return self._create_lxml_element(parent)

        # If we'll make a copy, we need to test if it exists in origin
        if_exists = et.SubElement(
            parent,
            et.QName(XSL_NS, 'if'),
            test=self.copy_of)

        et.SubElement(
            if_exists,
            et.QName(XSL_NS, 'copy-of'),
            select=self.copy_of)

        if_not_exists = et.SubElement(
            parent,
            et.QName(XSL_NS, 'if'),
            test=f'not({self.copy_of})')

        self._create_lxml_element(if_not_exists)

        return (if_exists, if_not_exists)

    def _create_lxml_element(self, parent):
        params = dict(name=self.name)

        if self.namespace:
            params['namespace'] = self.namespace

        element = et.SubElement(parent, et.QName(XSL_NS, 'element'),
                                **params)

        for attr in self.element_type.attributes:
            if attr['use'] == 'required':
                attr_params = {'name': attr['name']}
                attr_element = et.SubElement(element, et.QName(XSL_NS, 'attribute'),
                                             **attr_params)
                # TODO clean this up
                if attr['type'] == "xsd:ID":
                    et.SubElement(attr_element, et.QName(XSL_NS, "value-of"),
                                  select="my:gen-id()")
                elif attr['type'] == "xsd:IDREF":
                    attr_element.text = "INSERT_REF"
                elif attr['type'] == "xsd:positiveInteger":
                    attr_element.text = str(random.randint(1, 100000000000))
                elif attr['type'] == "xsd:token":
                    attr_element.text = "TOKEN_DEFAULT"
                else:
                    print(f'Not supported attribute type {attr["type"]}')

        for child in self.element_type.children_original_order:
            if 'minoccurs' not in child.attrs or child.attrs['minoccurs'] != '0':
                child.to_lxml_element(element)

        if self.attrs.get('default'):
            element.text = self.attrs.get('default')
            element.addprevious(et.Comment('Created with default value'))
        elif self.element_type.restriction_enum:
            element.text = self.element_type.restriction_enum[0]
            element.addprevious(et.Comment('CHECK! Created with first element of restriction from type spec'))
        elif self.element_type.name in ElementType.DEFAULTS:
            element.text = ElementType.DEFAULTS[self.element_type.name]

        return (element,)
