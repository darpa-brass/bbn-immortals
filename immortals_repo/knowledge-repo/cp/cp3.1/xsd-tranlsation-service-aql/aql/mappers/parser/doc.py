import logging
from functools import reduce
from collections import defaultdict
from itertools import chain
from xml.dom import minidom

from entities import ElementType, Element, ELEMENT_ANY_TYPE


# URL for XMLSchema specification
XML_SCHEMA_URL = 'http://www.w3.org/2001/XMLSchema'


class DocParser(object):
    '''
    A parser for string xsd documents, returns an array with parsed elements
    since xml root

    To ignore:
    - annotation
    - key
    - attribute
    - documentation
    - field
    - key
    - keyref
    - pattern
    - maxInclusive
    - minInclusive
    - selector

    # Process content:
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
            'complexType': self._parse_complex_type,
            'sequence': self._parse_sequence,
            'choice': self._parse_sequence,
            'complexContent': self._parse_complex_content,
            'simpleContent': self._parse_simple_content,
            'simpleType': self._parse_simple_type,
            'element': self._parse_element,
            'union': self._parse_union,
            'extension': self._parse_extension,
            'restriction': self._parse_restriction,
            'import': self._parse_import,
            'enumeration': self._parse_enumeration,
        }

        # Namespace for XML Schema tags (base of Schema definition)
        self.xmlsns = None

        self.namespaces = {}
        self.get_schema = get_schema

    def parse(self, file_contents):
        dom = minidom.parseString(file_contents)

        self._fill_namespaces(dom.documentElement)
        self._fill_types(dom)

        parse_result = self._parse(dom.documentElement)

        return parse_result, self.types

    def _fill_types(self, dom):
        elements = chain(dom.getElementsByTagName(f'{self.xmlsns}:complexType'),
                         dom.getElementsByTagName(f'{self.xmlsns}:simpleType'))

        for el in elements:
            if not el.getAttribute('name'):
                continue

            self.types.add(el.getAttribute('name'), el)

    def _fill_namespaces(self, el):
        for k, v in el.attributes.items():
            if ':' not in k:
                continue

            first, second = k.split(':')
            if first == 'xmlns':
                self.namespaces[v.strip()] = second.strip()

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

        _, types = schema_imported
        self.types.add_namespace(local_namespace, types)

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

    def _parse_sequence(self, el):
        result = self._parse(el)

        elements = result['element']

        if result.get('sequence'):
            # Flat sub elements in arrays
            elements = reduce(lambda x, y: x + y, result.get('sequence'), elements)

        # Here, we add all options of choice to handle it as a possible element
        # It's necessary to think about colateral effects of this
        if result.get('choice'):
            # Flat sub elements in arrays
            elements = reduce(lambda x, y: x + y, result.get('choice'), elements)

        return elements

    def _parse_union(self, el):
        member_types = [i.strip() for i in el.getAttribute('memberTypes').split(' ')]
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

        return el_type

    def _parse_complex_type(self, el):
        result = self._parse(el)

        complex_content = result.get('complexContent')
        if complex_content:
            return complex_content[0]

        el_name = el.getAttribute('name') or ''

        sequence = result.get('sequence', [None])
        children = sequence[0]
        if children:
            logging.warning('ELEMENT type with name {} children: {}'.format(
                el_name,
                ','.join([str(i) for i in children])))

        return ElementType(el_name, children)

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

        return Element(el.getAttribute('name'), el_type, attrs=el_attributes)

    def _parse_extension(self, el):
        return self.types.get_or_create(el.getAttribute('base'))

    def _parse_restriction(self, el):
        result = self._parse(el)

        return {
            'base': self.types.get_or_create(el.getAttribute('base')),
            'enumeration': result.get('enumeration')
        }

    def _parse_enumeration(self, el):
        return el.getAttribute('value')
