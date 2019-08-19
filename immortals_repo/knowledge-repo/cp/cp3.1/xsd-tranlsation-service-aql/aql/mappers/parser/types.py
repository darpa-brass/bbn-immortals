from entities import ElementType


class Types:
    '''
    Helper class to handle type reference parsed from elements
    '''

    def __init__(self, element_parser):
        self.types = {}
        self.namespace_types = {}
        self.element_parser = element_parser

    def add_namespace(self, namespace, types):
        self.namespace_types[namespace] = types

    def add(self, name, element):
        if name in self.types:
            return

        self.types[name] = [element, None]

    def get_or_create(self, name):
        parsed = self.get(name)

        if not parsed:
            parsed = ElementType(name)
            self.types[name] = [None, parsed]

        return parsed

    def get(self, name):
        from_namespace = self._get_from_namespace(name)
        if from_namespace:
            return from_namespace

        if name not in self.types:
            return None

        el, parsed = self.types[name]

        if not parsed:
            # Create an "empty" element to avoid circular dependency
            # If it exists in self.types, we'll return it in the subsequent
            # calls of 'element_parser', after we needs to fill this element with
            # proper data
            ref = ElementType(name)
            self.types[name][1] = ref

            parse_result = self.element_parser(el)
            if parse_result:
                _, parse_element = parse_result
                ref.fill_with(parse_element)

            parsed = ref

        return parsed

    def _get_from_namespace(self, name):
        if ':' not in name:
            return None

        namespace, single_name = name.split(':')
        if namespace in self.namespace_types:
            return self.namespace_types[namespace].get(single_name)

        return None
