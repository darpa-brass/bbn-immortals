"""
Core android platform. Currently contains emulator usage that can be disabled
by overriding the relevent methods. Assumes ADB usage for deployment
"""

class DeploymentPlatform(object):

    def platform_setup(self):
        pass


    def deploy_application(self, application_location):
        pass


    def upload_file(self, source_file_location, file_target):
        pass



    def start_application(self, event_listener):
        pass


    def stop_application(self):
        pass


    def platform_teardown(self):
        pass

    def call(self, call_list):
        pass


    def check_output(self, call_list):
        pass


    def Popen(self, args, bufsize=0, executable=None, stdin=None, stdout=None, stderr=None, preexec_fn=None, close_fds=False, shell=False, cwd=None, env=None, universal_newlines=False, startupinfo=None, creationflags=0):
        pass
