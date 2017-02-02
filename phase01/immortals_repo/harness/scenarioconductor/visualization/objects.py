import copy
import pprint
from threading import Lock, Thread, RLock
from time import sleep

import bokeh.models.widgets
from bokeh.layouts import column, row
from bokeh.models import ColumnDataSource, TableColumn, HTMLTemplateFormatter
from bokeh.plotting import figure

from ..data.base.scenarioapiconfiguration import ScenarioConductorConfiguration
from ..data.base.serializable import Serializable
from ..data.base.validation import get_validator_list
from ..ll_api.data import Status
from ..validation import calculate_validators

pp = pprint.PrettyPrinter(indent=4)


def _get_chart_data_from_scenario_configuration(scenario_configuration):
    all_validators = get_validator_list()
    desired_validator_stats = calculate_validators(scenario_configuration)

    data = {
        'Test Identifier': [],
        'Required Status': [],
        'Actual Status': [],
        'Result': []
    }

    for v in all_validators:
        data['Test Identifier'].append(v)

        if v in desired_validator_stats:
            if desired_validator_stats[v]:
                data['Required Status'].append(Status.SUCCESS)
            else:
                data['Required Status'].append(Status.FAILURE)

        else:
            # data['Required Status'].append(Status.NOT_APPLICABLE)
            data['Required Status'].append('N/A')
            data['Actual Status'].append(Status.PENDING)

        data['Actual Status'].append(Status.PENDING)
        data['Result'].append('gray')

    return data


def _get_chart_data_from_validation_results(scenario_configuration, results):
    """
    :type results: ValidationResults
    """

    data = _get_chart_data_from_scenario_configuration(scenario_configuration)

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


# noinspection PyClassHasNoInit
class AbstractBokehProvider(Serializable):
    def get_bokeh_diagram(self):
        raise NotImplementedError


# noinspection PyPep8Naming
class CartesianChart(AbstractBokehProvider):
    """
    :type chartMinX: long
    :type chartMaxX: long
    :type chartMinY: long
    :type chartMaxY: long
    :type xAxisLabel: str
    :type yAxisLabel: str
    :type title: str
    :type maxYValue: long
    :type minYValue: long
    :type maxYValueLabel: str
    :type minYValueLabel: str
    """

    _types = {}

    def __init__(self,
                 chartMinX,
                 chartMaxX,
                 chartMinY,
                 chartMaxY,
                 xAxisLabel,
                 yAxisLabel,
                 title,
                 maxYValue=None,
                 minYValue=None,
                 maxYValueLabel=None,
                 minYValueLabel=None
                 ):
        self.chartMinX = chartMinX
        self.chartMaxX = chartMaxX
        self.chartMinY = chartMinY
        self.chartMaxY = chartMaxY
        self.xAxisLabel = xAxisLabel
        self.yAxisLabel = yAxisLabel
        self.title = title
        self.maxYValue = maxYValue
        self.minYValue = minYValue
        self.maxYValueLabel = maxYValueLabel
        self.minYValueLabel = minYValueLabel
        self._data_source = ColumnDataSource(data=dict(x=[], y=[]))
        self._plot = None
        self._lock = Lock()
        # self._data_dictionary = dict(x=[], y=[])

    def get_bokeh_diagram(self):
        with self._lock:
            self._plot = figure(
                x_range=[self.chartMinX, self.chartMaxX],
                y_range=[self.chartMinY, self.chartMaxY],
                x_axis_label=self.xAxisLabel,
                y_axis_label=self.yAxisLabel,
                title=self.title,
                height=300,
                width=370
            )

            # self._data_source = ColumnDataSource(data=self._data_dictionary)

            self._plot.line(x='x', y='y', source=self._data_source)

            if self.maxYValue is not None:
                label = 'ceiling' if self.maxYValueLabel is None else self.maxYValueLabel
                self._plot.line(x=[self.chartMinX, self.chartMaxX], y=[self.maxYValue, self.maxYValue], legend=label,
                                color='red')

            if self.minYValue is not None:
                label = 'floor' if self.minYValueLabel is None else self.minYValueLabel
                self._plot.line(x=[self.chartMinX, self.chartMaxX], y=[self.minYValue, self.minYValue], legend=label,
                                color='black')

        return self._plot

    def set_max_y_value(self, maxYValue, maxYValueLabel=None):
        """
        :type maxYValue: int
        :type maxYValueLabel: str
        """
        with self._lock:
            self.maxYValue = maxYValue
            self.maxYValueLabel = maxYValueLabel
            label = 'ceiling' if self.maxYValueLabel is None else self.maxYValueLabel
            self._plot.line(x=[self.chartMinX, self.chartMaxX], y=[self.maxYValue, self.maxYValue], legend=label,
                            color='red')

    def set_y_max(self, max):
        self._plot.y_range.end = max

    def update(self, x, y):
        with self._lock:
            if y > self._plot.y_range.end:
                self._plot.y_range.end = y * 1.30

            self._data_source.stream(dict(x=[x], y=[y]))


class DataTable(AbstractBokehProvider):
    """
    :type title: str
    """

    _types = {}

    def __init__(self, title, column_identifiers, column_kwargs=None, bokeh_kwargs=None):
        """
        :type title: str
        :type column_identifiers: list[str]
        :type column_kwargs: list[dict[str]]
        :type bokeh_kwargs: dict[str]
        """

        self.title = title
        self._data = dict()
        self._column_identifiers = column_identifiers

        table_columns = []
        for i in range(len(column_identifiers)):
            title = column_identifiers[i]
            self._data[title.replace(' ', '')] = []

            if column_kwargs is None:
                table_columns.append(TableColumn(field=title.replace(' ', ''), title=title))
            else:
                table_columns.append(TableColumn(field=title.replace(' ', ''), title=title, **column_kwargs[i]))

        self._data_source = None
        self._bokeh_table = None
        self._lock = Lock()

        self._bokeh_kwargs = bokeh_kwargs

        self._data_source = ColumnDataSource(data=self._data)

        if bokeh_kwargs is None:
            self._bokeh_table = bokeh.models.widgets.DataTable(
                source=self._data_source, columns=table_columns, row_headers=False
            )
        else:
            self._bokeh_table = bokeh.models.widgets.DataTable(
                source=self._data_source, columns=table_columns, row_headers=False, **bokeh_kwargs
            )

    def get_bokeh_diagram(self):
        return self._bokeh_table

    # def append_data(self, value_dict):
    #     with self._lock:
    #         new_dict = dict()
    #         for title in self._data:
    #             new_dict[title] = self._data[title] + value_dict[title]
    # 
    #         self._data = new_dict
    #         self._data_source.data = new_dict
    # 
    # def prepend_data(self, value_dict):
    #     with self._lock:
    #         new_dict = dict()
    #         for title in self._data:
    #             new_dict[title] = value_dict[title] + self._data[title]
    # 
    #         self._data = new_dict
    #         self._data_source.data = new_dict

    def replace_data(self, value_dict):
        with self._lock:

            headers = value_dict.keys()

            for title in headers:
                if ' ' in title:
                    values = value_dict.pop(title)
                    value_dict[title.replace(' ', '')] = values

            self._data = value_dict
            self._data_source.data = value_dict


class DemoDashboard:
    """
    :type scenario_configuration: ScenarioConductorConfiguration
    """

    def __init__(self):
        self._das_status_visualization = DasStatusVisualization()
        self._bandwidth_visualization = BandwidthVisualization()
        self._validation_status = ValidationStatus()

    def initialize_with_scenario_configuration(self, scenario_configuration):
        self._bandwidth_visualization = BandwidthVisualization(scenario_configuration=scenario_configuration)
        self._validation_status = ValidationStatus(scenario_configuration=scenario_configuration)

    def das_ready(self, message):
        self._das_status_visualization.das_ready(message=message)

    def perturbation_detected(self, message):
        self._das_status_visualization.perturbation_detected(message=message)

    def adapting(self, message):
        self._das_status_visualization.adapting(message=message)

    def adaption_completed(self, message):
        self._das_status_visualization.adapting(message=message)

    def done(self, message):
        self._das_status_visualization.done(message=message)

    def update_bandwidth(self, time_delta, total_kbytes_transferred):
        self._bandwidth_visualization.update_bandwidth(
            time_delta=time_delta,
            total_kbytes_transferred=total_kbytes_transferred)

    def get_bokeh_object(self):
        return column(
            self._das_status_visualization.get_bokeh_diagram(),
            row(
                self._validation_status.get_bokeh_diagram(),
                self._bandwidth_visualization.get_bokeh_diagram()
            )
        )

    def validation_in_progress(self, message):
        self._das_status_visualization.executing_scenario(message)

    def update_validation_status(self, results):
        self._validation_status.update_validation_status(results)

    def load_scenario_configuration(self, scenario_configuration):
        self._validation_status.initialize_validation_status(scenario_configuration=scenario_configuration)
        self._bandwidth_visualization.set_max_bandwidth(scenario_configuration=scenario_configuration)


class ValidationStatus(DataTable):
    _types = DataTable._types

    def __init__(self):
        DataTable.__init__(self, title="Validation Status",
                           column_identifiers=['Test Identifier', 'Required Status', 'Actual Status', 'Result'],
                           bokeh_kwargs={'width': 432, 'height': 300},
                           column_kwargs=[
                               {
                                   'width': 166
                               },
                               {
                                   'width': 80
                               },
                               {
                                   'width': 80
                               },
                               {
                                   'width': 40,
                                   'formatter': HTMLTemplateFormatter(
                                       template='<span style="background-color:<%= Result %>;color:<%= Result %>">XXXXX</span>')
                               }
                           ]
                           )

        self._scenario_configuration = None
        # self._scenario_configuration = scenario_configuration
        # data = _get_chart_data_from_scenario_configuration(scenario_configuration=scenario_configuration)

        # self.replace_data(data)

    def initialize_validation_status(self, scenario_configuration):
        self._scenario_configuration = scenario_configuration
        data = _get_chart_data_from_scenario_configuration(scenario_configuration=scenario_configuration)
        self.replace_data(data)

    def update_validation_status(self, results):
        data = _get_chart_data_from_validation_results(self._scenario_configuration, results)
        self.replace_data(data)


class BandwidthVisualization(CartesianChart):
    _types = CartesianChart._types

    """
    :type scenario_configuration: ScenarioConductorConfiguration
    """

    # TODO: BADBADBAD NOT USING PROPER SCENARIO CONFIGURATION VALUES!!
    def __init__(self):
        CartesianChart.__init__(
            self,
            chartMinX=0,
            chartMaxX=60,
            chartMinY=0,
            chartMaxY=3072,
            xAxisLabel='Time (s)',
            yAxisLabel='KB/s transferred',
            title='Total utilized client/server bandwidth',
            # maxYValue=scenario_configuration.server.bandwidth / 8,
            maxYValue=None,
            maxYValueLabel=None
        )

    def set_max_bandwidth(self, scenario_configuration):
        """
        :type scenario_configuration: ScenarioConductorConfiguration
        """
        self.set_max_y_value(maxYValue=scenario_configuration.server.bandwidth / 8,
                             maxYValueLabel='Maximum allowed bandwidth usage')
        self.set_y_max(scenario_configuration.server.bandwidth / 4)

    def update_bandwidth(self, time_delta, total_kbytes_transferred):
        # print('VALS: ' + time_delta + ', ' + total_kbytes_transferred)
        self.update(time_delta, total_kbytes_transferred)


class DasStatusVisualization(DataTable):
    _types = DataTable._types
    phase_das_ready = 'DAS_READY'
    phase_perturbation_detected = 'PERTURBATION_DETECTED'
    phase_adapting = 'ADAPTING'
    phase_adaptation_completed = 'ADAPTATION_COMPLETED'
    phase_done = '/done'
    phase_scenario_execution = 'Scenario Execution'

    def __init__(self):
        self._update_lock = RLock()
        with self._update_lock:
            DataTable.__init__(self,
                               title="DAS Status",
                               column_identifiers=['Phase', 'Details'],
                               bokeh_kwargs={'width': 756, 'height': 160},
                               column_kwargs=[
                                   {
                                       'width': 60
                                   },
                                   {
                                   }
                               ]
                               )

            self.get_bokeh_diagram()
            self._raw_data_chart = {'Phase': [], 'Details': []}
            self._updating_thread = Thread(target=self._update_poller)
            self._updating_thread.setDaemon(True)
            self._updating_thread.start()

            self._phase_das_ready_started = False
            self._phase_perturbation_detected_started = False
            self._phase_adapting_started = False
            self._phase_adaptation_completed_started = False
            self._phase_done_started = False
            self._phase_scenario_execution_started = False

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

            sleep(1)

    def das_ready(self, message):
        with self._update_lock:
            self._raw_data_chart['Phase'].insert(0, DasStatusVisualization.phase_das_ready)
            self._raw_data_chart['Details'].insert(0, message)

    def perturbation_detected(self, message):
        with self._update_lock:
            self._raw_data_chart['Phase'].insert(0, DasStatusVisualization.phase_perturbation_detected)
            self._raw_data_chart['Details'].insert(0, message)

    def adapting(self, message):
        with self._update_lock:
            self._raw_data_chart['Phase'].insert(0, DasStatusVisualization.phase_adapting)
            self._raw_data_chart['Details'].insert(0, message)

    def adaption_completed(self, message):
        with self._update_lock:
            self._raw_data_chart['Phase'].insert(0, DasStatusVisualization.phase_adaptation_completed)
            self._raw_data_chart['Details'].insert(0, message)

    def executing_scenario(self, message):
        with self._update_lock:
            self._raw_data_chart['Phase'].insert(0, DasStatusVisualization.phase_scenario_execution)
            self._raw_data_chart['Details'].insert(0, message)

    def done(self, message):
        with self._update_lock:
            self._raw_data_chart['Phase'].insert(0, DasStatusVisualization.phase_done)
            self._raw_data_chart['Details'].insert(0, message)


class DataList(AbstractBokehProvider):
    """
    :type title: str
    :type data: list[str]
    """

    _types = {}

    def __init__(self, title, data=None):
        self.title = title
        self._data = data if data is not None else []

        self._data_source = None
        self._bokeh_table = None
        self._lock = Lock()
        self._tc = None

    def get_bokeh_diagram(self):
        with self._lock:
            if self._bokeh_table is None:
                self._data_source = ColumnDataSource(data=dict(values=self._data))
                self._tc = TableColumn(field='values', title=self.title)

                self._bokeh_table = bokeh.models.widgets.DataTable(
                    source=self._data_source, columns=[self._tc]
                )

            return self._bokeh_table

    def update(self, values):
        with self._lock:
            self._data = values + self._data
            self._data_source.data = dict(values=self._data)
