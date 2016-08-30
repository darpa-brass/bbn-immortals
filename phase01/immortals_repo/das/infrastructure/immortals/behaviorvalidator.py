# """
# Used to validate the behavior of the environment. It is currently very simple,
# and simply ensures all connected clients send their location at least once to
# all other connected clients.
# """
#
# from threading import Lock
#
# class BehaviorValidator:
#
#     def __init__(self):
#         self.client_dict = {}
#         self.do_validation = False
#         self.scenario_is_finished = False


    # """
    # Adds the instance_identifier to the list of clients expected to interact with
    # the Marti server
    # """
    # def add_client(self, instance_identifier, application_identifier):
    #     self.client_dict[instance_identifier] = ValidationApplication(instance_identifier, application_identifier)


    # """
    # Receives an event from the client indicated by instance_identifier to be
    # sorted out internally
    # """
    # def receive_event(self, instance_identifier, event):
    #     if self.do_validation:
    #         self.client_dict[instance_identifier].log_event(event)


    # """
    # Stops paying attention to received events and validates behavior
    # """
    # def stop_and_validate(self):
    #     self.do_validation = False
    #     is_valid = True
    #
    #     for client in self.client_dict.keys():
    #         if self.client_dict[client].application_identifier == 'marti':
    #             pass
    #
    #         elif self.client_dict[client].send_count <= 0:
    #             is_valid = False
    #             return is_valid
    #         else:
    #             for client2 in self.client_dict.keys():
    #                 if self.client_dict[client2].application_identifier == 'marti':
    #                     pass
    #                 else:
    #                     if client != client2 and client not in self.client_dict[client2].clients_received_from:
    #                         is_valid = False
    #
    #     return is_valid


    # """
    # Indicates that it should start validating received events
    # """
    # def start_validation(self,):
    #     self.do_validation = True
    #
    # def is_finished(self):
    #     return self.scenario_is_finished



# """
# A class used to represent and manage client states
# """
# class ValidationApplication:
#
#     def __init__(self, instance_identifier, application_identifier):
#         self.modify_lock = Lock()
#         self.instance_identifier = instance_identifier
#         self.application_identifier = application_identifier
#         self.send_count = 0
#         self.clients_received_from = set()
#
#     def log_event(self, event):
#         self.modify_lock.acquire()
#
#         event_type = event['type']
#
#         if event_type == 'ClientStart':
#             print 'Client "' + self.instance_identifier + '" has started.'
#             self.do_monitor = True
#
#         elif event_type == 'ClientShutdown':
#             print 'Client "' + self.instance_identifier + '" has shut down.'
#             self.do_monitor = False
#
#         elif event_type == 'MyLocationUpdated':
#             print 'Client "' + self.instance_identifier + '" has updated its location.'
#             self.send_count = self.send_count + 1
#
#         elif event_type == 'FieldLocationUpdated':
#             print 'Client "' + self.instance_identifier + '" has received a location update from "' + event['source'] + '".'
#             self.clients_received_from.add(event['source'])
#
#         elif event_type == 'ScenarioFinished':
#             self.scenario_is_finished = True
#
#         self.modify_lock.release()
