from bokeh.document import Document
from bokeh.models import Panel, Tabs

from pymmortals.datatypes.visualization import ChartData, FormulaData
from pymmortals.resources import resourcemanager
from .abstractbokehdashboard import AbstractBokehDashboard
from .visualizationobjects import CartesianChart, CalculatedDataset


class CalculatedBandwidthDashboard(AbstractBokehDashboard):
    """
    :type formula_data: FormulaData
    :type _chart_data: list[ChartData]
    :type dataset: CalculatedDataset
    """

    def __init__(self):
        AbstractBokehDashboard.__init__(self)

        self.formula_data = FormulaData.from_dict(resourcemanager.load_bandwidth_analysis_configuration_dict())
        self.dataset = CalculatedDataset(self.formula_data,
                                         {
                                             'PLIReportRate': 60
                                         })

        self._chart_data = list()

        var = self.formula_data.get_variable('ImageReportRate')
        irr_values = var.get_values(step=2)
        irr_values.insert(0, 1)
        for irr in irr_values:
            baseline_line = self.dataset.produce_line_data(
                changing_variable_identifier='NumberOfClients',
                label='Baseline',
                color='blue',
                variable_static_overrides={
                    'ImageReportRate': irr,
                    'DefaultImageSize': self.formula_data.get_variable('DefaultImageSize').get_values()[1]
                })

            max_adaptation_line = self.dataset.produce_line_data(
                changing_variable_identifier='NumberOfClients',
                label='No Solution',
                color='red',
                variable_static_overrides={
                    'ImageReportRate': irr,
                    'DefaultImageSize': self.formula_data.get_variable('DefaultImageSize').get_values()[0]
                })

            self._chart_data.append(ChartData(xAxisLabel=self.formula_data.resultUnit,
                                              yAxisLabel='NumberOfClients',
                                              title=(var.variableIdentifier + '=' + str(irr) + ' ' + var.unit),
                                              chartLines=[baseline_line, max_adaptation_line],
                                              chartHeight=400,
                                              chartWidth=400))

        var = self.formula_data.get_variable('NumberOfClients')
        for cc in var.get_values(step=2):
            baseline_line = self.dataset.produce_line_data(
                changing_variable_identifier='ImageReportRate',
                label='Baseline',
                color='blue',
                variable_static_overrides={
                    'NumberOfClients': cc,
                    'DefaultImageSize': self.formula_data.get_variable('DefaultImageSize').get_values()[1]
                })

            max_adaptation_line = self.dataset.produce_line_data(
                changing_variable_identifier='ImageReportRate',
                label='No Solution',
                color='red',
                variable_static_overrides={
                    'NumberOfClients': cc,
                    'DefaultImageSize': self.formula_data.get_variable('DefaultImageSize').get_values()[0]
                })

            self._chart_data.append(ChartData(xAxisLabel=self.formula_data.resultUnit,
                                              yAxisLabel='ImageReportRate',
                                              title=(var.variableIdentifier + '=' + str(cc) + ' ' + var.unit),
                                              chartLines=[baseline_line, max_adaptation_line],
                                              chartHeight=400,
                                              chartWidth=400))

    def modify_doc(self, doc):
        """
        :type doc: Document
        """

        panels = list()

        for cd in self._chart_data:
            plot = CartesianChart.from_chart_data(doc=doc, chart_data=cd).get_bokeh_diagram()
            plot.xaxis[0].formatter.use_scientific = False
            plot.yaxis[0].formatter.use_scientific = False
            plot.xaxis[0].major_label_orientation = 'vertical'
            plot.xaxis[0].major_label_text_align = 'center'
            plot.xaxis[0].major_label_text_baseline = 'middle'
            panels.append(Panel(child=plot, title=cd.title))

        doc.add_root(Tabs(tabs=panels))
