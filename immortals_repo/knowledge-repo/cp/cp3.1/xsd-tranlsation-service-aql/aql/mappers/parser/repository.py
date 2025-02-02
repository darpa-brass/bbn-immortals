import logging
from collections import OrderedDict

from .doc import DocParser
from .types import Types


# TODO Tests required
class SchemaRepository(object):

    def __init__(self):
        self.schemas = OrderedDict()
        self.parsed = {}

    def add(self, location, content, primary):
        self.schemas[location] = {
            'content': content,
            'primary': primary
        }

    def get_schema(self, location):
        if location not in self.parsed:
            self.parsed[location] = self._parse_schema(location)

        return self.parsed[location]

    def get_main_element(self):
        '''
        Returns main element for given documents after parsing then.
        '''

        self.parse()

        # Just search for primary element in documents
        for location, schema_data in self.schemas.items():
            # Filter only primary docs
            if not schema_data['primary']:
                continue

            result = self.get_schema(location)
            if len(result['result']['element']) == 0:
                continue

            element = result['result']['element'][0]

            logging.info('Getting element {} from {}'.format(str(element), location))

            return {
                'element': element,
                'location': location,
                'namespaces': result['namespaces']['namespaces']
            }

        logging.error('No main element found')
        return None

    def parse(self):
        for location in self.schemas.keys():
            if location in self.parsed:
                continue

            self.parsed[location] = self._parse_schema(location)

    def _parse_schema(self, location):
        if location not in self.schemas:
            logging.info(f'Location {location} not found in availables XML schemas')
            return None

        logging.info(f'Parsing XSD file in location "{location}"')
        doc_parser = DocParser(Types, self.get_schema)
        return doc_parser.parse(self.schemas[location]['content'])
