from typing import List

from bokeh.models import ColumnDataSource

from .serializable import Serializable


# noinspection PyPep8Naming
class ChartLineData(Serializable):
    _validator_values = {}

    def __init__(self,
                 xValues: List[float],
                 yValues: List[float],
                 label: str,
                 color: str):
        super().__init__()
        self.xValues = xValues
        self.yValues = yValues
        self.label = label
        self.color = color
        self._data_source = ColumnDataSource(data=dict(x=self.xValues, y=self.yValues))

    def get_data_source(self) -> ColumnDataSource:
        return self._data_source


# noinspection PyPep8Naming
class VariableData(Serializable):
    _validator_values = {}

    def __init__(self,
                 variableIdentifier: str,
                 unit: str,
                 minValue: float = None,
                 maxValue: float = None,
                 step: float = None,
                 values: List[float] = None):
        super().__init__()
        has_range = minValue is not None and maxValue is not None and step is not None
        has_values = values is not None
        if has_range == has_values:
            raise Exception("VariableData can specify range details or values, not both!")

        self.variableIdentifier = variableIdentifier
        self.unit = unit
        self.minValue = minValue
        self.maxValue = maxValue
        self.step = step
        self.values = values

    def get_values(self, step=None):
        if self.values is None:
            if step is None:
                step = int(self.step * 1000000)
            else:
                step = int(step * 1000000)

            minValue = int(self.minValue * 1000000)
            maxValue = int(self.maxValue * 1000000) + step
            rval = []
            for n in range(minValue, maxValue, step):
                rval.append(float(n // float(1000000.0)))
            return rval
        else:
            return self.values


# noinspection PyPep8Naming
class FormulaData(Serializable):
    _validator_values = {}

    def __init__(self,
                 formula: str,
                 resultUnit: str,
                 variableData: List[VariableData]):
        super().__init__()
        self.formula = formula
        self.resultUnit = resultUnit
        self.variableData = variableData

    def get_variable(self, variable_identifier):
        return [x for x in self.variableData if x.variableIdentifier == variable_identifier][0]


# noinspection PyPep8Naming
class ChartData(Serializable):
    _validator_values = {}

    def __init__(self,
                 xAxisLabel: str,
                 yAxisLabel: str,
                 title: str,
                 chartLines: List[ChartLineData],
                 chartHeight: int = None,
                 chartWidth: int = None):
        super().__init__()
        self.xAxisLabel = xAxisLabel
        self.yAxisLabel = yAxisLabel
        self.title = title
        self.chartLines = chartLines
        self.chartHeight = chartHeight
        self.chartWidth = chartWidth


class Timestamp(Serializable):
    _validator_values = {}

    def __init__(self,
                 start: int,
                 end: int,
                 delta: int = None):
        super().__init__()
        self.start = start
        self.end = end
        self.delta = delta if delta is not None else end - start


# noinspection PyPep8Naming
class EmulatorAnalysis(Serializable):
    _validator_values = {}

    def __init__(self,
                 createEmulatorDuration: Timestamp,
                 startEmulatorP1Duration: Timestamp,
                 startEmulatorP2Duration: Timestamp,
                 deployApplicationDuration: Timestamp,
                 applicationStartDuration: Timestamp,
                 applicationStopDuration: Timestamp,
                 executionDuration: Timestamp):
        super().__init__()
        self.createEmulatorDuration = createEmulatorDuration
        self.startEmulatorP1Duration = startEmulatorP1Duration
        self.startEmulatorP2Duration = startEmulatorP2Duration
        self.deployApplicationDuration = deployApplicationDuration
        self.applicationStartDuration = applicationStartDuration
        self.applicationStopDuration = applicationStopDuration
        self.executionDuration = executionDuration


# noinspection PyPep8Naming
class EmulatorAnalysisSessionData(Serializable):
    _validator_values = {}

    def __init__(self,
                 sessionIdentifier: str,
                 startTimestamp: int,
                 emulators: List[EmulatorAnalysis]):
        super().__init__()
        self.sessionIdentifier = sessionIdentifier
        self.startTimestamp = startTimestamp
        self.emulators = emulators
