# noinspection PyClassHasNoInit
class ReportingInterface:
    def submit_action(self, action, arguments, event_time_s=None):
        raise NotImplementedError

    def error_test_data_file(self, message, event_time_s=None):
        raise NotImplementedError

    def error_test_data_format(self, message, event_time_s=None):
        raise NotImplementedError

    def error_das_log_file(self, message, event_time_s=None):
        raise NotImplementedError

    def error_das(self, message, event_time_s=None):
        raise NotImplementedError

    def das_ready(self, event_time_s=None):
        raise NotImplementedError

    def perturbation_detected(self, message, event_time_s=None):
        raise NotImplementedError

    def mission_halted(self, message, event_time_s=None):
        raise NotImplementedError

    def mission_aborted(self, message, event_time_s=None):
        raise NotImplementedError

    def mission_test_error(self, message, event_time_s=None):
        raise NotImplementedError

    def artifact_file(self, source_filepath, target_subpath=None):
        raise NotImplementedError

    def log_das_info(self, message, event_time_s=None):
        raise NotImplementedError

    def log_das_error(self, message, event_time_s=None):
        raise NotImplementedError

    def write_artifact_to_file(self, str_to_write, target_subpath, clobber_existing=True):
        """
        :param str str_to_write:
        :param str target_subpath:
        :param bool clobber_existing:
        """
        raise NotImplementedError
