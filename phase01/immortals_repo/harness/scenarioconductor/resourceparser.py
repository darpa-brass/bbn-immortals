# import os
#
# from  packages import commentjson
#
# class ResourceParser:
#
#     def refine_scenario(self, configuration_source_directory, resource_definition_filepath, scenario_definition):
#
#         android_file_map = {}
#
#         if resource_definition_filepath is None:
#             android_file_map[]
#
#             return
#
#
#         with open(resource_definition_filepath):
#             json_data = commentjson.load(resource_definition_filepath):
#
#             for functionality_point in json_data['functionalityPoints']:
#                 if functionality_point['missionFunctionalityUri'] == "<http://darpa.mil/immortals/ontology/r1.0.0/functionality/location#LocationProviderSaasm>":
#                     has_saasm = True;
#
#
#             for resource in json_data['resourceUris']:
#                 if resource == "<http://darpa.mil/immortals/ontology/r1.0.0/resources/BlueTooth>":
#                     source = os.path.join(configuration_source_directory, 'location', 'LocationProviderBluetoothGpsSimulated.json')
#                     target = '/sdcard/ataklite/LocationProviderBluetoothGpsSimulated.json'
#                     android_file_map[source] = target
#
#                 elif resource == "<http://darpa.mil/immortals/ontology/r1.0.0/resources/USB>":
#                     if has_saasm:
#                         source = os.path.join(configuration_source_directory, 'location', 'LocationProviderSaasmSimulated.json')
#                         target = '/sdcard/ataklite/LocationProviderSaasmSimulated.json'
#                         android_file_map[source] = target
#                     else:
#                         source = os.path.join(configuration_source_directory, 'location', 'LocationProviderUsbGpsSimulated.json')
#                         target = '/sdcard/ataklite/LocationProviderUsbGpsSimulated.json'
#                         android_file_map[source] = target
#
#                 elif resource == "<http://darpa.mil/immortals/ontology/r1.0.0/resources/UI>":
#                     source = os.path.join(configuration_source_directory, 'location', 'LocationProviderManualSimulated.json')
#                     target = '/sdcard/ataklite/LocationProviderManualSimulated.json'
#                     android_file_map[source] = target
#
#         for application in scenario_definition['deploymentApplications']:
#             if application['application'] == 'ataklite':
#                 for source_path in android_file_map.keys():
#                     application['files'][source_path] = android_file_map[source_path]
#
#         return scenario_definition
