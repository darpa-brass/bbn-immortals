from difflib import SequenceMatcher


class Field(object):

    def __init__(self, entity, name, field_type):
        self.entity = entity
        self.name = name
        self.field_type = field_type
        self.documentation = self._get_documentation(entity)
        self.dom = None
        self.most_similar_by_documentation = None
        self._max_doc_similarity = 0.7  # This is the baseline to beat to accept a similar by doc

    def __eq__(self, other):
        return self.name == other.name and \
               self.field_type == other.field_type and \
               self.entity.name == other.entity.name

    def __ne__(self, other):
        return not self.__eq__(other)

    #
    # def __hash__(self):
    #    return hash((self.entity, self.name, self.field_type))
    #
    def __str__(self):
        return f'{self.field_type} {self.entity.name}::{self.name}'

    def __repr__(self):
        return self.__str__()

    def similar(self, other):
        """
        Find similar fields by name and type
        """
        nr = SequenceMatcher(None, other.name, self.name).ratio()
        tr = SequenceMatcher(None, other.field_type, self.field_type).ratio()
        return nr, tr

    def similar_doc(self, other):
        """
        Find similar fields by documentation
        """
        if len(self.documentation) < 1:
            return False
        nr = SequenceMatcher(None, other.documentation, self.documentation).ratio()
        if nr > self._max_doc_similarity:
            self._max_doc_similarity = nr
            self.most_similar_by_documentation = other
            return True
        return False

    def _get_documentation(self, entity):
        """
        Gets the documentation of the field
        """
        documentation = ""
        el = entity.dom_element
        min_pos = 1000000000000000
        for n in el.getElementsByTagName("xsd:documentation"):
            doctext = ""
            try:
                doctext = n.firstChild.data.strip()
            except:
                doctext = ""
            pos = doctext.find(self.name)
            if 0 <= pos < min_pos:
                min_pos = pos
                documentation = doctext
        return documentation


def field_diff(a, b):
    """
    Returns all fields in a that are not in b.
    This is a quick and dirt hack because IDK why the set diff did not worked.
    """
    result = []
    for s in a:
        found_equal = False
        for d in b:
            found_equal = s.name == d.name and s.field_type == d.field_type and s.entity.name == d.entity.name
            if found_equal:
                break  # We found what  we where looking for, STOP SEARCHING
        if not found_equal:
            result.append(s)
    return result


def field_intersect(a, b):
    """
    Finds the intersection between a and b
    This is a quick and dirt hack because IDK why the set diff did not worked.
    """
    result = []
    for s in a:
        found_equal = False
        for d in b:
            found_equal = s.name == d.name and s.field_type == d.field_type and s.entity.name == d.entity.name
            if found_equal:
                break  # We found what  we where looking for, STOP SEARCHING
        if found_equal:
            result.append(s)
    return result
