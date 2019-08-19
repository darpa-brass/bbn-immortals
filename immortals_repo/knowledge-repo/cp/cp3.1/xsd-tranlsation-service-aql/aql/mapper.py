"""
This program maps an XSD file into another
"""
import csv

from xml.dom import minidom

from mapping.xsddiff import get_all_fields, find_fields_diff, field_diff

from os import listdir
from os.path import isfile, join

from mapping.xsdmapper import Mapper


def name_sort(x):
    x = x.split('MDL_v')[1]
    x = x.split('.')[0]
    x = x.split('_')
    return int(x[0]) * 1000000 + int(x[1]) * 1000 + int(x[2])



#versions = [f for f in listdir(path) if isfile(join(path, f))]
#versions.sort(key=lambda x: name_sort(x))

versions = [
  './data/MDL_v0_8_17.xsd',
  './data/MDL_v0_8_19.xsd'
]

with open('results.csv', 'w') as csvFile:
    writer = csv.writer(csvFile)
    writer.writerow(['Old_Version',
                     'New_Version',
                     'OldCount',
                     'NewCount',
                     'Unchanged_Count',
                     'Small_Name_Change',
                     'Small_Type_Change',
                     'Added',
                     'Removed',
                     'Removed_Sim_Graph',
                     'Added_Sim_Graph',
                     'Similar_Doc'
                     'UnMapPercent'])

    for i in range(1, len(versions)):
        print(f"Mapping: {versions[i - 1]} -> {versions[i]}")

        with open(versions[i - 1], 'r') as old_version:
            old_version = old_version.read()
        with open(versions[i], 'r') as new_version:
            new_version = new_version.read()

        old_version, new_version = minidom.parseString(old_version), minidom.parseString(new_version)

        mapper = Mapper()
        mapper.map(old_version, new_version)
        old_field_count = len(mapper.old_fields)
        new_field_count = len(mapper.new_fields)
        unchanged_count = len(mapper.unchanged)
        renamed_count = len(mapper.renamed)
        typerenamed_count = len(mapper.typerenamed)
        removed_count = len(mapper.removed)
        added_count = len(mapper.added)
        graph_similar = len(mapper.removed_graph_similar)
        add_graph_similar = len(mapper.added_graph_similar)
        similar_doc = len(mapper.similar_by_doc)

        xslt_input = []
        xslt_input.extend(((x, x) for x in mapper.unchanged))
        xslt_input.extend(mapper.renamed)
        xslt_input.extend(mapper.typerenamed)
        xslt_input.extend(((x[0], x[1][0]) for x in mapper.removed_graph_similar))
        xslt_input.extend(mapper.added_graph_similar)
        xslt_input.extend(mapper.similar_by_doc)


        print(mapper.similar_by_doc)

        print(f" > Old field count: {old_field_count}")
        print(f" > New field count: {new_field_count}")
        print(f" > Unchanged field count: {unchanged_count}")
        print(f" > Removed fields (present in old and not in new): {removed_count}")
        print(f" > Added fields (present in new and not in old): {added_count}")
        print(f" > Fields with small diff in name: {renamed_count}")
        print(f" > Fields with small diff in type name: {typerenamed_count}")
        print(f" > Removed fields with similar graphs: {graph_similar}")
        print(f" > Added fields with similar graphs: {add_graph_similar}")
        print(f" > Similar documentation: {similar_doc}")
        print("")
        print(f" > Percent of fields mapped ((added/new_fields) + (removed/old_fields))/2: {mapper.mapping_percent}")

        row = [versions[i - 1].split('/')[-1],  # 'Old_Version',
               versions[i].split('/')[-1],  # 'New_Version',
               old_field_count,  # 'OldCount',
               new_field_count,  # 'NewCount',
               unchanged_count,  # Unchanged Count
               renamed_count,  # 'Small_Name_Change',
               typerenamed_count,  # 'Small_Type_Change',
               added_count,  # 'Added',
               removed_count,  # 'Removed',
               graph_similar,  # 'Removed',
               add_graph_similar,  # 'Removed',
               similar_doc,
               mapper.mapping_percent  # 'MapPercent'
               ]
        writer.writerow(row)
        csvFile.flush()


csvFile.close()
