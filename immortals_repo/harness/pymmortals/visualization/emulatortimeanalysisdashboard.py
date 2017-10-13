import datetime
import json
import os
import time
from threading import RLock
from typing import List

from bokeh.charts import Bar
from bokeh.document import Document
from bokeh.model import Model

from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.visualization import Timestamp, EmulatorAnalysis, ChartLineData, EmulatorAnalysisSessionData
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.analyticsevent import AnalyticsEvent
from pymmortals.resources import resourcemanager
from .abstractbokehdashboard import AbstractBokehDashboard
from .visualizationobjects import AbstractBokehProvider


# noinspection PyPep8Naming
class BarChart(AbstractBokehProvider):
    def __init__(self,
                 doc: Document,
                 chartLineData: List[ChartLineData],
                 title: str,
                 ):
        AbstractBokehProvider.__init__(self, doc=doc, title=title)
        self.chartLineData = chartLineData
        self.title = title

        self._plot = None
        self._lock = RLock()

    def get_bokeh_diagram(self) -> Model:
        with self._lock:
            plot = self._plot
            if plot is None:

                data = {
                    'scenarios': [],
                    'phase': [],
                    'seconds': []
                }

                for l in self.chartLineData:

                    for x in l.xValues:
                        data['scenarios'].append(x)
                        data['phase'].append(l.label)

                    for y in l.yValues:
                        data['seconds'].append(y)

                plot = Bar(
                    data,
                    values='seconds',
                    label='scenarios',
                    stack='phase',
                    agg='mean',
                    title=self.title,
                    legend='top_right',
                    plot_width=500
                )
                self._plot = plot
                return self._plot

        return self._plot


def produce_emulator_analysis(archive_path):
    """
    :type archive_path: str
    :rtype: EmulatorAnalysisSessionData
    """

    emulators = []

    if not archive_path.endswith('/'):
        archive_path += '/'

    archive_path = os.path.abspath(archive_path)

    session_identifier = archive_path[1:-1].split('/')[-1]

    fuseki_log = os.path.join(archive_path, 'debug', 'fuseki_stdout.txt')

    line0 = open(fuseki_log, 'r').readline()
    raw_date_str = line0[line0.find('[') + 1:line0.find(']')] + '-GMT'

    start_timestamp = time.mktime(
        datetime.datetime.strptime(raw_date_str, '%Y-%m-%d %H:%M:%S-%Z').timetuple()) - time.timezone

    start_log_filepath = os.path.join(archive_path, 'debug', 'start_log.txt')

    tmp_emulator_map = {}

    with open(start_log_filepath, 'r') as input_file:
        lines = input_file.readlines()
        for line in lines:

            if line.startswith('{') and line.endswith('}\n'):
                # noinspection PyBroadException,PyPep8
                try:
                    event = AnalyticsEvent.from_json_str(line)
                    if event.eventSource.startswith('ATAKLite'):
                        if event.eventSource not in tmp_emulator_map:
                            tmp_emulator_map[event.eventSource] = {}

                        em = tmp_emulator_map[event.eventSource]
                        if event.type not in em:
                            em[event.type] = {}

                        et = em[event.type]
                        et[event.eventId] = event

                except:
                    pass

    def get_event_delta(event_identifier, e_map):
        end = float(e_map['timeMarkerEnd'][event_identifier].eventTime)
        start = float(e_map['timeMarkerStart'][event_identifier].eventTime)
        return Timestamp(start=int(start), end=int(end))

    def get_events_delta(event_a_identifier, event_b_identifier, e_map):
        start = float(e_map['timeMarkerStart'][event_b_identifier].eventTime)
        end = float(e_map['timeMarkerEnd'][event_a_identifier].eventTime)
        return Timestamp(start=int(start), end=int(end))

    for emulator_identifier in tmp_emulator_map:
        em = tmp_emulator_map[emulator_identifier]

        if '_create_emulator' in em:
            ea = EmulatorAnalysis(createEmulatorDuration=get_event_delta('_create_emulator', em),
                                  startEmulatorP1Duration=get_event_delta('startEmulatorPhase1', em),
                                  startEmulatorP2Duration=get_event_delta('startEmulatorPhase2', em),
                                  deployApplicationDuration=get_event_delta('deploy_application', em),
                                  applicationStartDuration=get_event_delta('application_start', em),
                                  applicationStopDuration=get_event_delta('application_stop', em),
                                  executionDuration=get_events_delta('application_start', 'application_stop', em))
        else:
            ea = EmulatorAnalysis(createEmulatorDuration=get_event_delta('startEmulatorPhase1', em),
                                  startEmulatorP1Duration=get_event_delta('startEmulatorPhase1', em),
                                  startEmulatorP2Duration=get_event_delta('startEmulatorPhase2', em),
                                  deployApplicationDuration=get_event_delta('deploy_application', em),
                                  applicationStartDuration=get_event_delta('application_start', em),
                                  applicationStopDuration=get_event_delta('application_stop', em),
                                  executionDuration=get_events_delta('application_start', 'application_stop', em))

        emulators.append(ea)

    return EmulatorAnalysisSessionData(sessionIdentifier=session_identifier,
                                       startTimestamp=start_timestamp,
                                       emulators=emulators)


def produce_emulator_analyses(archive_collection_path):
    """
    :type archive_collection_path: str
    :rtype: list[EmulatorAnalysisSessionData]
    """
    analyses = []
    if not archive_collection_path.endswith('/'):
        archive_collection_path += '/'

    archive_collection_path = os.path.abspath(archive_collection_path)

    for d in os.walk(archive_collection_path):
        if 'debug' in d[1]:
            analysis_path = os.path.abspath(d[0]) + '/'
            analyses.append(produce_emulator_analysis(analysis_path))

    return analyses


class Analyzer:
    """
    :type emulator_analysis_data: list[EmulatorAnalysisSessionData]
    """

    def __init__(self, emulator_analysis_data):
        self.emulator_analysis_data = emulator_analysis_data

    def produce_lines(self):
        """
        :rtype list[ChartLineData]
        """

        create_emulator_y_values = []
        start_emulator_y_values = []
        validation_start_y_values = []
        validation_end_y_values = []
        x_identifier_labels = []

        for analysis in self.emulator_analysis_data:

            create_emulator_start = analysis.emulators[0].createEmulatorDuration.start
            create_emulator_end = analysis.emulators[0].createEmulatorDuration.end
            start_emulator_start = analysis.emulators[0].startEmulatorP1Duration.start
            start_emulator_end = analysis.emulators[0].startEmulatorP2Duration.end
            execution_start = analysis.emulators[0].executionDuration.start
            execution_end = analysis.emulators[0].executionDuration.end

            for e in analysis.emulators:
                if e.createEmulatorDuration.start < create_emulator_start:
                    create_emulator_start = e.createEmulatorDuration.start

                if e.createEmulatorDuration.end > create_emulator_end:
                    create_emulator_end = e.createEmulatorDuration.end

                if e.startEmulatorP1Duration.start < start_emulator_start:
                    start_emulator_start = e.startEmulatorP1Duration.start

                if e.startEmulatorP2Duration.end > start_emulator_end:
                    start_emulator_end = e.startEmulatorP2Duration.end

                if e.executionDuration.start < execution_start:
                    execution_start = e.executionDuration.start

                if e.executionDuration.end > execution_end:
                    execution_end = e.executionDuration.end

            x_identifier_labels.append(analysis.sessionIdentifier)
            create_emulator_y_values.append((create_emulator_start / 1000) - analysis.startTimestamp)
            start_emulator_y_values.append((start_emulator_start / 1000) - analysis.startTimestamp)
            validation_start_y_values.append((execution_start / 1000) - analysis.startTimestamp)
            validation_end_y_values.append((execution_end / 1000) - analysis.startTimestamp)

        lines = list()

        lines.append(ChartLineData(xValues=x_identifier_labels, yValues=create_emulator_y_values,
                                   label='Start Test Adapter', color='blue'))
        lines.append(ChartLineData(xValues=x_identifier_labels, yValues=start_emulator_y_values,
                                   label='Create Emulators', color='orange'))
        lines.append(ChartLineData(xValues=x_identifier_labels, yValues=validation_start_y_values,
                                   label='Start Emulators', color='red'))
        lines.append(ChartLineData(xValues=x_identifier_labels, yValues=validation_end_y_values,
                                   label='Perform Validation', color='green'))

        return lines


class EmulatorTimeAnalysisDashboard(AbstractBokehDashboard):
    """
    :type emulator_analysis_data: list[EmulatorAnalysisSessionData]
    :type _analyzer: Analyzer
    """

    def modify_doc(self, doc):
        """
        :type doc: Document
        """
        chart = BarChart(doc=doc, chartLineData=self._analyzer.produce_lines(),
                         title='Scenario Runtime Comparison')
        doc.add_root(chart.get_bokeh_diagram())

    def __init__(self):
        AbstractBokehDashboard.__init__(self)

        archive_collection_path = get_configuration().visualization.emulatorTimingDashboardFilepath

        if archive_collection_path is None:
            self.emulator_analysis_data = [EmulatorAnalysisSessionData.from_dict(l) for l in
                                           resourcemanager.load_canned_emulator_analysis_data_dict_list()]

        else:
            self.emulator_analysis_data = produce_emulator_analyses(archive_collection_path=archive_collection_path)
            wd = []
            for v in self.emulator_analysis_data:
                wd.append(v.to_dict())

            json.dump(wd, open('/Users/austin/Downloads/theFilzes.json', 'w'))

        self._analyzer = Analyzer(emulator_analysis_data=self.emulator_analysis_data)
