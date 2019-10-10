import logging
from functools import reduce
from collections import defaultdict
from itertools import chain
from xml.dom import minidom
from constants import PRIMITIVE_TYPES_DEFAULT, SIMPLE_TYPES_DEFAULT

from entities import ElementType, Element, ELEMENT_ANY_TYPE


# URL for XMLSchema specification
XML_SCHEMA_URL = 'http://www.w3.org/2001/XMLSchema'


class DocParser(object):
    '''
    A parser for string xsd documents, returns an array with parsed elements
    since xml root

    To ignore:
    - key

    - field
    - key
    - keyref
    - pattern
    - maxInclusive
    - minInclusive
    - selector

    # Process content:
    - attribute
    - documentation
    - annotation
    - any (A global element that matches any type = ELEMENT_ANY_TYPE)
    - complexType
    - sequence
    - choice
    - complexContent
    - simpleContent
    - simpleType
    - element
    - extension
    - restriction
    - union (for now first el only)
    - Don't know for now:
    - enumeration
    '''

    # Attributes to exclude from elements creation
    ELEMENT_ATTRS_EXCLUDE = {'name', 'type'}

    def __init__(self, types_class, get_schema):
        self.types = types_class(self._parse_child)

        # TODO Maybe change this keys to constants and handle tags
        # with a proper function instead of just split by ':' and get
        # second param
        self._map_parsers = {
            'attribute': self._parse_attribute,
            'complexType': self._parse_complex_type,
            'sequence': self._parse_sequence,
            'choice': self._parse_choice,
            'complexContent': self._parse_complex_content,
            'simpleContent': self._parse_simple_content,
            'simpleType': self._parse_simple_type,
            'element': self._parse_element,
            'union': self._parse_union,
            'extension': self._parse_extension,
            'restriction': self._parse_restriction,
            'import': self._parse_import,
            'enumeration': self._parse_enumeration,
            'annotation': self._parse_annotation,
            'documentation': self._parse_documentation,
        }

        # Namespace for XML Schema tags (base of Schema definition)
        self.xmlsns = None

        self.namespaces = {}
        self.target_namespace = None
        self.namespace = None
        self.get_schema = get_schema

    def parse(self, file_contents):
        dom = minidom.parseString(file_contents)

        self._fill_namespaces(dom.documentElement)
        self._fill_types(dom)

        parse_result = self._parse(dom.documentElement)

        return {
            'result': parse_result,
            'types': self.types,
            'namespaces': {
                'target': self.target_namespace,
                'namespaces': self.namespaces
            }
        }

    def _fill_types(self, dom):
        elements = chain(dom.getElementsByTagName(f'{self.xmlsns}:complexType'),
                         dom.getElementsByTagName(f'{self.xmlsns}:simpleType'))

        for el in elements:
            if not el.getAttribute('name'):
                continue

            self.types.add(el.getAttribute('name'), el)

    def _fill_namespaces(self, el):
        for k, v in el.attributes.items():
            value = v.strip()

            if k == 'xmlns':
                self.namespace = value
                continue

            if k == 'targetNamespace':
                self.target_namespace = value
                continue

            if ':' not in k:
                continue

            first, second = k.split(':')
            if first == 'xmlns':
                self.namespaces[value] = second.strip()

        logging.info(f'Target namespace: {self.target_namespace}')
        logging.info(f'Document namespace: {self.namespace}')

        # Defining xml schema definition namespace (like 'xs' or 'xsd')
        self.xmlsns = self.namespaces[XML_SCHEMA_URL]

    def _parse(self, el):
        results = defaultdict(list)

        for el in el.childNodes:
            result = self._parse_child(el)
            if not result:
                continue

            tag_name, parse_result = result
            results[tag_name].append(parse_result)

        return results

    def _parse_child(self, el):
        if not isinstance(el, minidom.Element):
            return None

        tag_name = el.tagName.split(':')[1]

        if tag_name in self._map_parsers:
            return tag_name, self._map_parsers[tag_name](el)

        return None

    def _parse_import(self, el):
        '''
        Parse import tags and merge it's types to our local types
        '''

        result = {
            'location': None,
            'namespace_url': None
        }

        for k, v in el.attributes.items():
            if k == 'schemaLocation':
                result['location'] = v.strip()

            if k == 'namespace':
                result['namespace_url'] = v.strip()

        logging.info('Handling import tag with location "{location}" and namespace url "{namespace_url}"'.format(
            **result))
        if not result['location'] or not result['namespace_url']:
            return result

        schema_imported = self.get_schema(result['location'])
        if not schema_imported:
            return result

        local_namespace = self.namespaces.get(result['namespace_url'])
        if not local_namespace:
            logging.error('Namespace url "{namespace_url}" not found in local namespaces'.format(
                **result))
            return result

        self.types.add_namespace(local_namespace, schema_imported['types'])

        return result

    def _parse_simple_content(self, el):
        result = self._parse(el)

        if result.get('restriction'):
            return result.get('restriction')[0]['base']

        return result.get('extension')[0]

    def _parse_complex_content(self, el):
        result = self._parse(el)

        if result.get('restriction'):
            return result.get('restriction')[0]['base']

        return result.get('extension')[0]

    def _parse_attribute(self, el):
        return {
            'name': el.getAttribute('name'),
            'type': el.getAttribute('type'),
            'use': el.getAttribute('use'),
        }

    def _parse_sequence(self, el):
        result = self._parse(el)

        elements = result['element']

        # Flat sub elements in arrays
        sequences = result.get('sequence', [{}])
        elements = reduce(lambda x, y: x + y.get('elements', []), sequences, elements)

        # Flat sub elements from choices in elements, we are doing this to handle it as a normal
        # child element
        choices = result.get('choice', [])
        elements = reduce(lambda x, y: x + y, choices, elements)

        return {
            'elements': elements,
            'choices': choices
        }

    def _parse_choice(self, el):
        result = self._parse(el)

        return result.get('element', [])

    def _parse_union(self, el):
        member_types = [i.strip() for i in el.getAttribute('memberTypes').split(' ')]

        primitive_types = [i for i in member_types if i.startswith(f'{self.xmlsns}:')]
        primitive_types = [i for i in primitive_types
                           if PRIMITIVE_TYPES_DEFAULT.get(i.split(':')[1]) is not None]

        # Primitive types are easy to generate default values
        if primitive_types:
            first_type, *_ = primitive_types
            return self.types.get_or_create(first_type)

        # Let's try to find types with an enumeration, it's also easy to generate values
        types = [self.types.get_or_create(i) for i in member_types]
        types_with_enum_restriction = [i for i in types if i.restriction_enum]
        if types_with_enum_restriction:
            first_type_el, *_ = types_with_enum_restriction
            return first_type_el

        # Now, let's try to find for simple types with default value..
        simple_types = [i for i in member_types if i.lower() in SIMPLE_TYPES_DEFAULT]
        if simple_types:
            first_simple_type, *_ = simple_types
            return self.types.get_or_create(first_simple_type)

        # Ok, give up! lets get the first one and GG
        first_type, *_ = member_types
        return self.types.get_or_create(first_type)

    def _parse_simple_type(self, el):
        result = self._parse(el)

        union = result.get('union', None)
        if union:
            return union[0]

        el_type = ElementType(el.getAttribute('name') or '')

        restriction = result.get('restriction')
        if restriction and restriction[0].get('enumeration'):
            # TODO for now, not handling 'base' from restriction
            el_type.restriction_enum = restriction[0]['enumeration']

        if result.get('annotation'):
            el_type.annotation = '\n'.join(result.get('annotation', []))

        return el_type

    def _parse_complex_type(self, el):
        result = self._parse(el)

        complex_content = result.get('complexContent')
        if complex_content:
            return complex_content[0]

        el_name = el.getAttribute('name') or ''

        sequence = result.get('sequence', [{}])[0]
        children = sequence.get('elements')
        if children:
            logging.debug('ELEMENT type with name {} children: {}'.format(
                el_name,
                ','.join([str(i) for i in children])))

        el_type = ElementType(el_name, children)

        # Choices groups
        el_type.choices_groups = sequence.get('choices', [])

        if result.get('simpleContent'):
            el_type.fill_with(result.get('simpleContent')[0])
            el_type.name = el_name
            el_type.changed()
        else:
            if result.get('annotation'):
                el_type.annotation = '\n'.join(result.get('annotation', []))

            if result.get('attribute'):
                el_type.attributes = result.get('attribute', [])

        return el_type

    def _parse_element(self, el):
        logging.debug('Checking element: {}'.format(el.getAttribute('name')))

        el_type = None

        type_name = el.getAttribute('type')
        if type_name:
            el_type = self.types.get_or_create(type_name)

        if not el_type:
            result = self._parse(el)

            el_types = result.get('complexType') or result.get('simpleType')

            if el_types:
                el_type = el_types[0]

        if not el_type:
            logging.warning('Element {} do not matches with any type, using ANY element type for it'.format(
                el.getAttribute('name')))
            el_type = ELEMENT_ANY_TYPE

        el_attributes = {}
        for attr_name, attr_value in el.attributes.items():
            attr_name = attr_name.lower().strip()  # Always lower case and strip to use in a second moment
            if attr_name not in self.ELEMENT_ATTRS_EXCLUDE:
                el_attributes[attr_name] = attr_value

        logging.debug('Element "{}", type defined with name "{}", attrs "{}" and children: {}'.format(
            el.getAttribute('name'),
            el_type.name,
            str(el_attributes),
            ','.join([str(i) for i in el_type.children])))

        el = Element(el.getAttribute('name'), el_type, attrs=el_attributes)

        if self.namespace:
            el.namespace = self.namespace

        return el

    def _find_ancestor_with_name(self, el):
        if el.parentNode is None:
            return None
        if el.parentNode.getAttribute("name"):
            return el.parentNode.getAttribute("name")
        else:
            return self._find_ancestor_with_name(el.parentNode)

    def _parse_extension(self, el):
        base = self.types.get_or_create(el.getAttribute('base'))
        extension = self._parse(el)
        extension_element = self.types.get_or_create(self._find_ancestor_with_name(el) + "_extension")
        extension_element.fill_with(base)
        extension_element.attributes.extend(extension.get('attribute', []))
        extension_element.children.extend(extension.get('children', []))
        return extension_element

    def _parse_restriction(self, el):
        result = self._parse(el)

        return {
            'base': self.types.get_or_create(el.getAttribute('base')),
            'enumeration': result.get('enumeration')
        }

    def _parse_enumeration(self, el):
        return el.getAttribute('value')

    def _parse_annotation(self, el):
        result = self._parse(el)
        return '\n'.join(result.get('documentation', []))

    def _parse_documentation(self, el):
        return '\n'.join([i.data.strip() for i in el.childNodes])
