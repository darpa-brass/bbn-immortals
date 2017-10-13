import copy
import typing
from threading import Thread, RLock

from bokeh.client import show_session
from bokeh.document import Document
from bokeh.layouts import column
from bokeh.layouts import row
from bokeh.models import HTMLTemplateFormatter

from pymmortals import triples_helper
from pymmortals.auxillary_functions import calculate_validator_identifiers
from pymmortals.datatypes.routing import EventTag, EventTags, EventType
from pymmortals.datatypes.visualization import ChartLineData
from pymmortals.generated.com.securboration.immortals.ontology.cp.gmeinterchangeformat import GmeInterchangeFormat
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.analyticsevent import AnalyticsEvent
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.status import Status
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.testdetails import TestDetails
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.testresult import TestResult
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.validationstate import ValidationState
from pymmortals.generated.mil.darpa.immortals.core.api.validation.results.validationresults import ValidationResults
from pymmortals.generated.mil.darpa.immortals.core.api.validation.validators import Validators
from pymmortals.routing.eventrouter import EventRouter, AbstractEventReceiver
from pymmortals.threadprocessrouter import sleep
from .abstractbokehdashboard import AbstractBokehDashboard
from .visualizationobjects import DataTable, CartesianChart


def _get_chart_data_from_scenario_configuration(gif: GmeInterchangeFormat) -> typing.Dict[str, typing.List[str]]:
    all_validator_identifiers = [k.identifier for k in list(Validators)]
    desired_validator_stats: typing.Dict[str, bool] = calculate_validator_identifiers(gif)

    data = {
        'Test Identifier': [],
        'Required Status': [],
        'Actual Status': [],
        'Result': []
    }

    for v in all_validator_identifiers:
        data['Test Identifier'].append(v)

        if v in desired_validator_stats:
            if desired_validator_stats[v]:
                data['Required Status'].append(Status.SUCCESS.name)
            else:
                data['Required Status'].append(Status.FAILURE.name)

        else:
            # data['Required Status'].append(Status.NOT_APPLICABLE)
            data['Required Status'].append('N/A')
            data['Actual Status'].append(Status.PENDING.name)

        data['Actual Status'].append(Status.PENDING.name)
        data['Result'].append('gray')

    return data


def _get_chart_data_from_validation_state(gif: GmeInterchangeFormat, validation_state: ValidationState) \
        -> typing.Dict[str, typing.List[str]]:
    data = _get_chart_data_from_scenario_configuration(gif)

    for i in range(len(data['Test Identifier'])):
        assert validation_state.executedTests is not None
        for test in validation_state.executedTests:  # type: TestDetails
            if test.testIdentifier == data['Test Identifier'][i]:
                data['Actual Status'][i] = test.actualStatus.name

            if data['Required Status'][i] == 'NOT_APPLICABLE' or data['Required Status'][i] == 'N/A':
                data['Result'][i] = 'green'

            elif data['Required Status'][i] == data['Actual Status'][i]:
                data['Result'][i] = 'green'

            else:
                data['Result'][i] = 'red'

    return data


def _get_chart_data_from_validation_results(gif: GmeInterchangeFormat, results: ValidationResults) \
        -> typing.Dict[str, typing.List[str]]:
    data = _get_chart_data_from_scenario_configuration(gif)

    for i in range(len(data['Test Identifier'])):
        for r in results.results:  # type: TestResult
            if r.validatorIdentifier == data['Test Identifier'][i]:
                data['Actual Status'][i] = r.currentState

            if data['Required Status'][i] == 'NOT_APPLICABLE' or data['Required Status'][i] == 'N/A':
                data['Result'][i] = 'green'

            elif data['Required Status'][i] == data['Actual Status'][i]:
                data['Result'][i] = 'green'

            else:
                data['Result'][i] = 'red'

    return data


class ImmortalsDashboardManager(AbstractBokehDashboard, AbstractEventReceiver):
    def __init__(self, event_router: EventRouter):
        AbstractEventReceiver.__init__(self, event_router=event_router)
        AbstractBokehDashboard.__init__(self)
        self._current_dashboard_instance: ImmortalsDashboard = None
        self._event_router: EventRouter = event_router
        self.subscribe(EventTags.DeploymentModelLoaded)

    def _receive_event(self, event_tag, data):
        assert isinstance(data, GmeInterchangeFormat)
        self._current_dashboard_instance = ImmortalsDashboard(event_router=self._event_router, gif=data)
        show_session(session_id=data.sessionIdentifier, app_path='/immortals')

    def modify_doc(self, doc):
        self._current_dashboard_instance.modify_doc(doc=doc)


class ImmortalsDashboard(AbstractBokehDashboard):
    def modify_doc(self, doc):
        self._doc: Document = doc
        self._das_status_visualization: DasStatusVisualization = DasStatusVisualization(doc=doc,
                                                                                        event_router=self._event_router)
        self._bandwidth_visualization: BandwidthVisualization = \
            BandwidthVisualization(doc=doc,
                                   event_router=self._event_router,
                                   gif=self._gif)
        self._validation_status: ValidationStatus = ValidationStatus(doc=doc,
                                                                     event_router=self._event_router,
                                                                     gif=self._gif)

        doc.add_root(column(
            self._das_status_visualization.get_bokeh_diagram(),
            row(
                self._validation_status.get_bokeh_diagram(),
                self._bandwidth_visualization.get_bokeh_diagram()
            )
        ))

    def __init__(self, event_router: EventRouter, gif: GmeInterchangeFormat):
        AbstractBokehDashboard.__init__(self)
        self._gif: GmeInterchangeFormat = gif
        self._doc: Document = None
        self._das_status_visualization: DasStatusVisualization = None
        self._event_router: EventRouter = event_router
        self._bandwidth_visualization: BandwidthVisualization = None
        self._validation_status: ValidationStatus = None


class ValidationStatus(DataTable, AbstractEventReceiver):
    _types = DataTable._types

    _pass_fail_template = '<span style="background-color:<%= Result %>;color:<%= Result %>">XXXXX</span>'

    def __init__(self, doc: Document, event_router: EventRouter, gif: GmeInterchangeFormat):
        AbstractEventReceiver.__init__(self, event_router=event_router)
        DataTable.__init__(self,
                           doc=doc,
                           title="Validation Status",
                           column_identifiers=['Test Identifier', 'Required Status', 'Actual Status', 'Result'],
                           # bokeh_kwargs={'width': 432, 'height': 300},
                           bokeh_kwargs={'width': 560, 'height': 400},
                           column_kwargs=[
                               {
                                   # 'width': 166
                                   'width': 214
                               },
                               {
                                   # 'width': 80
                                   'width': 102
                               },
                               {
                                   # 'width': 70
                                   'width': 90
                               },
                               {
                                   # 'width': 50,
                                   'width': 64,
                                   'formatter': HTMLTemplateFormatter(template=ValidationStatus._pass_fail_template)
                               }
                           ]
                           )

        self._event_router: EventRouter = event_router
        self._gif: GmeInterchangeFormat = gif
        self.initialize_validation_status(self._gif)

        self.subscribe(EventTags.ValidationTestsFinished)

    def _receive_event(self, event_tag: EventTag, data: typing.Union[GmeInterchangeFormat, ValidationState]):
        if event_tag == EventTags.ValidationTestsFinished:
            self.update_validation_state(data)
            self.disconnect()

    def initialize_validation_status(self, gif: GmeInterchangeFormat):
        self._gif: GmeInterchangeFormat = gif
        data = _get_chart_data_from_scenario_configuration(gif=gif)
        self.replace_data(data)

    def update_validation_state(self, results: ValidationState):
        data = _get_chart_data_from_validation_state(self._gif, results)
        self.replace_data(data)


class BandwidthVisualization(CartesianChart, AbstractEventReceiver):
    def __init__(self, doc: Document, event_router: EventRouter, gif: GmeInterchangeFormat):
        AbstractEventReceiver.__init__(self, event_router=event_router)
        y_upper_viewable_multiplier = 1.5
        max_bandwidth_value = triples_helper.get_bandwidth_constraint_kbit_per_second(gif) / 8
        CartesianChart.__init__(
            self,
            doc=doc,
            x_axis_label='Time (s)',
            y_axis_label='KB/s transferred',
            title='Total utilized client/server bandwidth',
            x_range=(0, 60),
            y_range=(0, max_bandwidth_value * y_upper_viewable_multiplier),
            y_upper_viewable_multiplier=y_upper_viewable_multiplier,
            chart_height=400,
            chart_width=560
        )

        self._event_router: EventRouter = event_router

        self._bandwidth_line_data: ChartLineData = ChartLineData(
            xValues=[],
            yValues=[],
            label="Utilized Bandwidth",
            color="blue"
        )
        self.add_line(self._bandwidth_line_data, refit_x=False, refit_y=False)

        cld: ChartLineData = ChartLineData(
            xValues=[0, 600],
            yValues=[max_bandwidth_value, max_bandwidth_value],
            label='Maximum allowed bandwidth usage',
            color='red'
        )
        self.add_line(chart_line_data=cld, refit_x=False, refit_y=False)

        self.subscribe(EventTags.AnalyticsEventServerNetworkTrafficCalculatedBytesPerSec)
        self.subscribe(EventTags.ValidationTestsFinished)

        self.start_time = -1

    def update_bandwidth(self, time_delta: int, total_kbytes_transferred: int):
        self.append_line(x=time_delta, y=total_kbytes_transferred, identifier=self._bandwidth_line_data.label,
                         refit_x=False, refit_y=True)

    def _receive_event(self, event_tag: EventTag, data: object):
        if event_tag == EventTags.AnalyticsEventServerNetworkTrafficCalculatedBytesPerSec:
            data = data  # type: AnalyticsEvent

            if self.start_time < 0:
                self.start_time = data.eventTime

            t_d = int((data.eventTime - self.start_time) / 1000)

            self.update_bandwidth(t_d, int(float(data.data) / 1000))

        elif event_tag == EventTags.ValidationTestsFinished:
            self.disconnect()


class DasStatusVisualization(DataTable, AbstractEventReceiver):
    """
    :type event_router: EventRouter
    :type _raw_data_chart: dict[str, list[str]]
    """

    _types = DataTable._types
    # TODO: Clean up phases!
    phase_perturbation_detected = 'PERTURBATION_DETECTED'
    phase_adapting = 'ADAPTING'
    phase_adaptation_completed = 'ADAPTATION_COMPLETED'
    phase_done = '/done'
    phase_scenario_execution = 'Scenario Execution'

    def __init__(self, event_router, doc):
        self._update_lock = RLock()
        AbstractEventReceiver.__init__(self, event_router=event_router)
        DataTable.__init__(self,
                           title="DAS Status",
                           column_identifiers=['Phase', 'Details'],
                           doc=doc,
                           # bokeh_kwargs={'width': 756, 'height': 160},
                           bokeh_kwargs={'width': 1120, 'height': 208},
                           column_kwargs=[
                               {
                                   # 'width': 60
                                   'width': 80
                               },
                               {
                               }
                           ]
                           )

        self.event_router = event_router
        self.get_bokeh_diagram()
        self._raw_data_chart = {'Phase': [], 'Details': []}
        self._updating_thread = Thread(target=self._update_poller)
        self._updating_thread.setDaemon(True)
        self._updating_thread.start()

        self._phase_perturbation_detected_started = False
        self._phase_adapting_started = False
        self._phase_adaptation_completed_started = False
        self._phase_done_started = False
        self._phase_scenario_execution_started = False

        self.subscribe(EventType.STATUS)
        self.subscribe(EventTags.ValidationTestsFinished)

    def _update_poller(self):
        while True:
            with self._update_lock:
                current_data = copy.deepcopy(self._raw_data_chart)

                previous_line_phase = None
                for i in range(len(self._raw_data_chart['Phase'])):
                    value = self._raw_data_chart['Phase'][i]
                    if value == previous_line_phase:
                        current_data['Phase'][i] = ''

                    previous_line_phase = value

            DataTable.replace_data(self, current_data)

            sleep(0.1)

    def _receive_event(self, event_tag, data):
        if event_tag in DasStatusVisualization._event_mapping:
            self._event_mapping[event_tag](self, data)

        elif event_tag == EventTags.ValidationTestsFinished:
            self.disconnect()

    # noinspection PyUnusedLocal
    def perturbation_detected(self, message):
        """
        :type message: str
        """
        with self._update_lock:
            self._raw_data_chart['Phase'].insert(0, DasStatusVisualization.phase_perturbation_detected)
            self._raw_data_chart['Details'].insert(0, 'A perturbation in the environment has been detected.')

    def adapting(self, message):
        """
        :type message: str
        """
        with self._update_lock:
            self._raw_data_chart['Phase'].insert(0, DasStatusVisualization.phase_adapting)
            if isinstance(message, str):
                self._raw_data_chart['Details'].insert(0, message)
            else:
                self._raw_data_chart['Details'].insert(0, 'Adaptation has started.')

    # noinspection PyUnusedLocal
    def adaption_completed(self, message):
        """
        :type message: str
        """
        with self._update_lock:
            self._raw_data_chart['Phase'].insert(0, DasStatusVisualization.phase_adaptation_completed)
            self._raw_data_chart['Details'].insert(0,
                                                   'Adaptation has completed. Now setting up validation environment...')

    def executing_scenario(self, message):
        """
        :type message: ValidationStartReturnData
        """
        with self._update_lock:
            self._raw_data_chart['Phase'].insert(0, DasStatusVisualization.phase_scenario_execution)
            self._raw_data_chart['Details'].insert(0, 'Executing Validation.  Approximate Duration: '
                                                   + str(message.expectedDurationSeconds / 1000) + ' seconds')

    # noinspection PyUnusedLocal
    def done(self, message):
        """
        :type message: str
        """
        with self._update_lock:
            self._raw_data_chart['Phase'].insert(0, DasStatusVisualization.phase_done)
            self._raw_data_chart['Details'].insert(0, 'The scenario has finished validation.')

    _event_mapping = {
        EventTags.THStatusPerturbationDetected: perturbation_detected,
        EventTags.THStatusAdapting: adapting,
        EventTags.THStatusAdaptationCompleted: adaption_completed,
        EventTags.THSubmitDone: done,
        EventTags.ValidationStarted: executing_scenario,
        EventTags.DASStatusMessage: adapting
    }
