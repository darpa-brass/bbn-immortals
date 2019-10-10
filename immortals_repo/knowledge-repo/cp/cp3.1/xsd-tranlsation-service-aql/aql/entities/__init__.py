from copy import copy, deepcopy
from difflib import SequenceMatcher
from lxml import etree as et
from mappers.utils import gen_default_value

from constants import XSL_NS


class ElementType(object):
    # TODO add slots

    # TODO Handle choices_groups with children manipulation
    # we need to check if a child is in choices_groups and remove it, etc..

    # TODO children and children_original_order could be just one attr, now we are using
    # children_original_order to calculate hashing, so children attr could be with original order and
    # we just remove children_original_order attr.

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
        self.choices_groups = []

        # Annotation with documentation about type (useful for similarity comparisson)
        self.annotation = None

    # TODO add tests
    def default(self):
        return gen_default_value(self.name)

    # TODO add tests
    def fill_with(self, other):
        '''
        Fill this element type with other
        '''

        self._children = list(other._children)
        self.children_original_order = list(other.children_original_order)
        self.name = other.name
        self.restriction_enum = deepcopy(other.restriction_enum)
        self.annotation = other.annotation
        self.attributes = deepcopy(other.attributes)
        self.choices_groups = deepcopy(other.choices_groups)

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
            [str(hash(str(self)))] + [str(hash(i)) for i in self.children_original_order]
        ))

    def _set_children(self, val):
        # Always sort our children to calculate hash properly
        self.children_original_order = copy(val) or []
        self._children = sorted(val or [], key=str)
        self.changed()

    children = property(_get_children, _set_children)

    # TODO add tests
    def skip_from_output_by_choice(self, child):
        found_in_groups = False
        for group in self.choices_groups:
            try:
                if group.index(child) == 0:
                    return False

                found_in_groups = True
            except ValueError:
                pass

        return found_in_groups

    def in_choices_group(self, child):
        for group in self.choices_groups:
            if child in group:
                return True

        return False

    def in_same_choices_group(self, first_child, second_child):
        for group in self.choices_groups:
            if first_child in group and second_child in group:
                return True

        return False

    # TODO add tests
    def output_children(self):
        for child in self.children_original_order:
            if not child.lxml_must_create():
                continue

            if self.skip_from_output_by_choice(child):
                continue

            yield child


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
    def children_original_order(self):
        return self.element_type.children_original_order

    @property
    def self_hash(self):
        return hash(str(self))

    @property
    def self_hash_unordered(self):
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
        # Calculate my hash, considering my name, type, and sequence index
        self._hash = hash(str(self.self_hash) + '-' + str(self.type_hash))

    def __str__(self):
        return f'{self.name}-{self.element_type}'

    def __eq__(self, other):
        return hash(self) == hash(other)

    def __hash__(self):
        return self._hash

    # TODO add tests
    def lxml_must_create(self):
        return self.attrs.get('minoccurs', '1') != '0' or self.has_copy_of()

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

    # TODO add tests
    def has_copy_of(self):
        '''
        Search recursivelly for a copy-of element and returns true if found
        '''

        if self.copy_of:
            return True

        for child in self.children:
            if child.has_copy_of():
                return True

        return False

    def lxml_choices_comment(self, element_type):
        if element_type.in_choices_group(self):
            comment = et.Element(et.QName(XSL_NS, 'comment'))
            comment.text = 'Element "{}" choosed from choices group'.format(self.name)

            return comment

        return None

    def _create_lxml_element(self, parent):
        params = dict(name=self.name)

        if self.namespace:
            params['namespace'] = self.namespace

        element = et.SubElement(parent, et.QName(XSL_NS, 'element'),
                                **params)

        for attr in self.element_type.attributes:
            # TODO add tests
            if attr['use'] == 'required':
                attr_params = {'name': attr['name']}
                attr_element = et.SubElement(element, et.QName(XSL_NS, 'attribute'),
                                             **attr_params)
                if attr['type'].endswith("IDREF"):
                    et.SubElement(attr_element, et.QName(XSL_NS, 'value-of'), select='ancestor::mdl:' + self.element_type.name.replace('Ref', '') + "/@ID")
                else:
                    attr_value = gen_default_value(attr['type'])
                    if attr_value is not None:
                        attr_element.text = attr_value
                        continue

                if attr['type'].endswith(':ID'):
                    et.SubElement(attr_element,
                                  et.QName(XSL_NS, 'value-of'),
                                  select='my:gen-id()')

        for child in self.element_type.output_children():
            # Adding a choices comment
            choices_comment = child.lxml_choices_comment(self.element_type)
            if choices_comment is not None:
                element.append(choices_comment)

            child.to_lxml_element(element)

        if self.attrs.get('default'):
            element.text = self.attrs.get('default')
            element.addprevious(et.Comment('Created with default value'))
        elif self.element_type.restriction_enum:
            element.text = self.element_type.restriction_enum[0]
            element.addprevious(et.Comment('CHECK! Created with first element of restriction from type spec'))
        else:
            type_default_value = self.element_type.default()
            if type_default_value:
                element.text = type_default_value

        return (element,)
