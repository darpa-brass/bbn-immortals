import sys
import copy
from difflib import SequenceMatcher

from mapping.xsdentity import find_entities
from mapping.xsdfield import field_intersect, field_diff, Field

from translation.aqltranslation import XSD_TYPES


class Mapper(object):
    """
    This class finds the mapping between two different versions of the XSD
    """

    class Expansion(object):

        def __init__(self):
            # expanded fields
            self.exp_fields = []

        def expand(self, field, entities):
            self._do_expand(field, entities, [])

        def _do_expand(self, field, entities, current_branch):
            if field.field_type in XSD_TYPES:
                k = [f.name for f in current_branch]
                k.append(field.field_type)
                self.exp_fields.append(k)
            else:
                if field.field_type in entities:
                    for ff in entities[field.field_type].fields:
                        current_branch.append(ff)
                        self._do_expand(ff, entities, current_branch)
                        current_branch.pop()

    def __init__(self):
        self.mapping_percent = None
        self.old_entities = []  # all entities of the old version
        # All entities of the new verion
        self.new_entities = []
        # All fields of the old version
        self.old_fields = []
        # All fields of the new version
        self.new_fields = []
        # Fields kept between versions
        self.unchanged = []
        # Fields with small name changes
        self.renamed = []
        # Fields whose type has an small name change
        self.typerenamed = []
        # Fields added in the new version
        self.added = []
        # Fields removed in the new version
        self.removed = []
        # Schema location
        self.new_schema_location = ""

        self.removed_graph_similar = []
        self.added_graph_similar = []

        self.similar_by_doc = {}

    def map_from_entities(self, old, new):
        self.old_entities = old
        self.new_entities = new
        self.map_internal()

    def map_internal(self):
        # Find all fields on the new and old XSD
        self.old_fields = []
        for e in self.old_entities.values():
            self.old_fields.extend(e.fields)
        self.new_fields = []
        for e in self.new_entities.values():
            self.new_fields.extend(e.fields)

        result = []
        # Find which fields have not changed at all
        self.unchanged = field_intersect(self.old_fields, self.new_fields)
        result = [(x, x) for x in self.unchanged]

        # Find removed fields
        self.removed = field_diff(self.old_fields, self.new_fields)
        # Find add fields
        self.added = field_diff(self.new_fields, self.old_fields)

        # Find similar fields by name and type
        self._find_similar_name()

        # Try to do a graph similarity
        self._find_graph_similar()

        # diff the fields again that were found to be similiar
        self._diff_similar()

        # Try to find fields with similar documentation
        self._find_documentation_similar()

        added_count = len(self.added)
        new_count = len(self.new_fields) + sys.float_info.epsilon
        removed_count = len(self.removed)
        old_count = len(self.old_fields) + sys.float_info.epsilon

        self.mapping_percent = added_count / new_count + removed_count / old_count
        self.mapping_percent = (1 - self.mapping_percent * 0.5) * 100

    def _diff_similar(self):
        for field, matches in self.removed_graph_similar:
            if len(matches) == 1:
                self.renamed.append((field, matches[0]))
                new_field = copy.copy(field)
                new_field.name = matches[0].name
                self.removed.extend(field_diff([new_field], matches))
                self.added.extend(field_diff(matches, [new_field]))

    def map(self, old_dom, new_dom):
        # Find entities in the XSD
        self.old_entities = find_entities(old_dom)
        self.new_entities = find_entities(new_dom)
        self.map_internal()

    def _graph_similarity(self, s):
        """
        Tries to find a graph similarity between the removed field 's'
        and the added fields in the same entity of the new version.
        """

        # Expand the field into a tree of simple type fields
        ex_s = Mapper.Expansion()
        ex_s.expand(s, self.old_entities)
        old_ex_fields = ex_s.exp_fields

        # These variables store the best match found so far
        # For removed fields
        removed_max_match = {}
        # For added fields
        added_max_match = {}

        # Non expanded field that will match
        matching_added_field = []

        # Look an added field in the entity of the removed field
        # That can match the expanded tree
        if not s.entity.name in self.new_entities:
            return False, False, None

        for a in self.new_entities[s.entity.name].fields:
            if a in self.added:
                # Expand the new added field
                ex_a = Mapper.Expansion()
                ex_a.expand(a, self.new_entities)
                new_ex_fields = ex_a.exp_fields

                # Map each element of the expanded tree pairwise
                # and keep the best match
                for xs in old_ex_fields:
                    s_xs = str(xs)
                    if s_xs not in removed_max_match:
                        removed_max_match[s_xs] = (0.0, None)
                    for xa in new_ex_fields:
                        s_xa = str(xa)
                        if s_xa not in added_max_match:
                            added_max_match[s_xa] = (0.0, None)
                        match = SequenceMatcher(None, xs, xa).ratio()
                        if match > removed_max_match[s_xs][0]:
                            removed_max_match[s_xs] = (match, xa)
                            if a not in matching_added_field:
                                matching_added_field.append(a)
                        if match > added_max_match[s_xa][0]:
                            added_max_match[s_xa] = (match, xs)

        # We now say that we find a match if the average of matching between expanded fields
        # is above 0.75 and no match is below 0.6
        removed_match = len(removed_max_match) > 0
        added_match = len(added_max_match) > 0

        rem_ave = 0.0
        for x in removed_max_match.values():
            if x[0] < 0.6:
                removed_match = False
                break
            rem_ave += x[0]
        if removed_match:
            rem_ave /= len(removed_max_match)
            removed_match = rem_ave > 0.75

        add_ave = 0.0
        for x in added_max_match.values():
            if x[0] < 0.6:
                added_match = False
                break
            add_ave = x[0]
        if added_match:
            add_ave /= len(added_max_match)
            added_match = add_ave > 0.75

        return removed_match, added_match, matching_added_field

    def _find_graph_similar(self):
        m = 0
        # iterate over the "removed" fields to see if there are any similar fields
        while m < len(self.removed):
            s = self.removed[m]
            try:
                # boolean, boolean, list(Field)
                removed_match, added_match, matching_field = self._graph_similarity(s)
                if removed_match:
                    self.removed_graph_similar.append((s, matching_field))
                    del self.removed[m]
                    if added_match:
                        self.added.remove(matching_field)
                        self.added_graph_similar.append(matching_field)
                else:
                    m += 1
            except Exception as ex:
                print(f'{ex}')
                m += 1

    def _find_similar_name(self):
        """
        Find fields similar by name and type name
        :return:
        """
        m, n = 0, 0
        while m < len(self.removed):
            inc_m = True
            n = 0
            while m < len(self.removed) and n < len(self.added):
                try:
                    s = self.removed[m]
                    d = self.added[n]
                    ns, ts = s.similar(d)
                    if 0.5 < ns < 1 and ts == 1:
                        inc_m = False
                        self.renamed.append((s, d))
                        del self.added[n]
                        del self.removed[m]
                    elif 0.5 < ts < 1 and ns == 1:
                        inc_m = False
                        self.typerenamed.append((s, d))
                        del self.added[n]
                        del self.removed[m]
                    else:
                        n += 1
                except IndexError as ex:
                    print('Woops!')
            if inc_m:
                m += 1

    def _find_documentation_similar(self):
        self.similar_by_doc = {}
        for r in self.removed:
            for a in self.added:
                if r.similar_doc(a):
                    self.similar_by_doc[str(r)] = (r, a)

        for k in self.similar_by_doc.values():
            if k[0] in self.removed:
                self.removed.remove(k[0])
            if k[1] in self.added:
                self.added.remove(k[1])


