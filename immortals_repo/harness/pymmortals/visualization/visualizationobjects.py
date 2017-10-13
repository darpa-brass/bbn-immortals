from functools import partial
from typing import Tuple, Dict, Union, List

from bokeh.document import Document
from bokeh.models import ColumnDataSource, TableColumn
from bokeh.models import DataTable as BokehDataTable
from bokeh.plotting import Figure, figure

from pymmortals.datatypes.visualization import ChartLineData, ChartData, FormulaData


# noinspection PyClassHasNoInit,PyAbstractClass
class AbstractBokehProvider:
    def __init__(self, doc: Document, title: str):
        self._doc = doc
        self._title = title

    def get_bokeh_diagram(self):
        raise NotImplementedError


# noinspection PyPep8Naming
class CartesianChart(AbstractBokehProvider):
    @classmethod
    def from_chart_data(cls, doc: Document, chart_data: ChartData):
        chart = cls(
            doc=doc,
            x_axis_label=chart_data.xAxisLabel,
            y_axis_label=chart_data.yAxisLabel,
            title=chart_data.title,
            chart_height=chart_data.chartHeight,
            chart_width=chart_data.chartWidth
        )
        for line in chart_data.chartLines:
            chart.add_line(line)
        return chart

    def __init__(self,
                 doc: Document,
                 x_axis_label: str,
                 y_axis_label: str,
                 title: str,
                 chart_height: int = None,
                 chart_width: int = None,
                 x_range: Tuple[float, float] = None,
                 y_range: Tuple[float, float] = None,
                 y_upper_viewable_multiplier: float = None
                 ):
        AbstractBokehProvider.__init__(self, doc=doc, title=title)

        self._line_data = {}

        self._y_upper_viewable_multiplier: float = \
            y_upper_viewable_multiplier if y_upper_viewable_multiplier is not None else 1

        self._plot: Figure = figure(
            x_range=x_range if x_range is not None else (0, 0),
            y_range=y_range if y_range is not None else (0, 0),
            x_axis_label=x_axis_label,
            y_axis_label=y_axis_label,
            title=self._title,
            height=300 if chart_height is None else chart_height,
            width=370 if chart_width is None else chart_width
        )

    def get_bokeh_diagram(self) -> Figure:
        return self._plot

    def _update_viewable_x_range(self, x_values: Union[List[int], List[float]]):
        min_x = self._plot.x_range.start if self._plot.x_range is not None else None
        max_x = self._plot.x_range.end if self._plot.x_range is not None else None

        if len(x_values) > 0 and (isinstance(x_values[0], int) or isinstance(x_values[0], float)):

            for x in x_values:
                if min_x is None:
                    min_x = x
                if max_x is None:
                    max_x = x
                if x < min_x:
                    min_x = x
                if x > max_x:
                    max_x = x

        self._set_x_range(min_x=min_x, max_x=max_x)

    def _update_viewable_y_range(self, y_values: Union[List[int], List[float]]):
        min_y = self._plot.y_range.start if self._plot.y_range is not None else None
        max_y = self._plot.y_range.end if self._plot.y_range is not None else None

        if len(y_values) > 0 and (isinstance(y_values[0], int) or isinstance(y_values[0], float)):
            # min_y = None
            # max_y = None

            for y in y_values:
                if min_y is None:
                    min_y = y
                if max_y is None:
                    max_y = y
                if y < min_y:
                    min_y = y
                if y * self._y_upper_viewable_multiplier > max_y:
                    max_y = y * self._y_upper_viewable_multiplier

        self._set_y_range(min_y=min_y, max_y=max_y)

    def set_x_range(self, min_x: float = None, max_x: float = None):
        self._doc.add_next_tick_callback(partial(self._set_x_range, min_x=min_x, max_x=max_x))

    def _set_x_range(self, min_x: float = None, max_x: float = None):
        if min_x is not None:
            self._plot.x_range.start = min_x
        if max_x is not None:
            self._plot.x_range.end = max_x

    def set_y_range(self, min_y: float = None, max_y: float = None):
        self._doc.add_next_tick_callback(partial(self._set_y_range, min_y=min_y, max_y=max_y))

    def _set_y_range(self, min_y: float = None, max_y: float = None):
        if min_y is not None:
            self._plot.y_range.start = min_y
        if max_y is not None:
            self._plot.y_range.end = max_y

    def add_line(self, chart_line_data: ChartLineData, refit_x: bool = True, refit_y: bool = True):
        if chart_line_data.label in list(self._line_data.keys()):
            raise Exception('A line with the identifier "' + chart_line_data.label + '" Already exists in chart "'
                            + self._title + '"!')

        self._doc.add_next_tick_callback(partial(self._add_line, chart_line_data=chart_line_data,
                                                 refit_x=refit_x, refit_y=refit_y))

    def _add_line(self, chart_line_data: ChartLineData, refit_x: bool, refit_y: bool):

        if refit_x:
            if chart_line_data.xValues is None or len(chart_line_data.xValues) == 0:
                self._set_x_range(min_x=0, max_x=0)

            elif not isinstance(chart_line_data.xValues[0], str):
                self._update_viewable_x_range(chart_line_data.xValues)

        if refit_y:
            if chart_line_data.yValues is None or len(chart_line_data.yValues) == 0:
                self._set_y_range(min_y=0, max_y=0)

            elif not isinstance(chart_line_data.yValues[0], str):
                self._update_viewable_y_range(chart_line_data.yValues)

        data_source = chart_line_data.get_data_source()

        self._plot.line(x='x', y='y', source=data_source, legend=chart_line_data.label, color=chart_line_data.color)

        self._line_data[chart_line_data.label] = chart_line_data

    def append_line(self, x: int, y: int, identifier: str = None, refit_x: bool = True, refit_y: bool = True):
        if identifier is None and len(list(self._line_data.keys())) > 1:
            raise Exception(
                'The identifier must be supplied for chart "' + self._title + '" since it contains '
                + 'multiple lines!')

        elif identifier not in list(self._line_data.keys()):
            raise Exception(
                'No line with identifier "' + identifier + '" exists in chart "' + self._title + '"!')

        else:
            identifier = identifier if identifier is not None else list(self._line_data.keys())[0]
            self._doc.add_next_tick_callback(partial(self._append_line, x=x, y=y, identifier=identifier,
                                                     refit_x=refit_x, refit_y=refit_y))

    def _append_line(self, x: Union[float, int, str], y: Union[float, int, str],
                     identifier: str, refit_x: bool, refit_y: bool):
        if refit_x and not isinstance(x, str):
            self._update_viewable_x_range(x_values=[x])

        if refit_y and not isinstance(y, str):
            self._update_viewable_y_range(y_values=[y])

        line = self._line_data[identifier]  # type: ChartLineData
        line.xValues.append(x)
        line.yValues.append(y)
        line.get_data_source().stream(dict(x=[x], y=[y]))


class DataTable(AbstractBokehProvider):
    """
    :type _data: dict
    :type _bokeh_table: Figure
    """

    _types = {}

    def __init__(self, doc: Document, title: str, column_identifiers: List[str],
                 column_kwargs=None,
                 bokeh_kwargs=None):
        AbstractBokehProvider.__init__(self, doc=doc, title=title)
        self._data = dict()

        table_columns: List[TableColumn] = []
        for i in range(len(column_identifiers)):
            title = column_identifiers[i]
            self._data[title.replace(' ', '')] = []

            if column_kwargs is None:
                table_columns.append(TableColumn(field=title.replace(' ', ''), title=title))
            else:
                table_columns.append(TableColumn(field=title.replace(' ', ''), title=title, **column_kwargs[i]))

        self._data_source: ColumnDataSource = None

        self._data_source = ColumnDataSource(data=self._data)

        if bokeh_kwargs is None:
            self._bokeh_table: BokehDataTable = BokehDataTable(
                source=self._data_source, columns=table_columns, row_headers=False
            )
        else:
            self._bokeh_table: BokehDataTable = BokehDataTable(
                source=self._data_source, columns=table_columns, row_headers=False, **bokeh_kwargs
            )

    def get_bokeh_diagram(self) -> BokehDataTable:
        return self._bokeh_table

    def replace_data(self, value_dict: Dict[str, List[str or float]]):
        headers = list(value_dict.keys())

        for title in headers:
            if ' ' in title:
                values = value_dict.pop(title)
                value_dict[title.replace(' ', '')] = values

        self._data = value_dict

        try:
            self._doc.add_next_tick_callback(self._replace_data)
        except ValueError as ve:
            if str(ve) != "callback has already been added":
                raise ve

    def _replace_data(self):
        self._data_source.data = self._data


class CalculatedDataset:
    def __init__(self, formula_data: FormulaData, default_values: Dict[str, float] = None):
        self.formula_data = formula_data
        self.default_values = default_values if default_values is not None else dict()

    def produce_line_data(self, changing_variable_identifier: str, label: str, color: str,
                          variable_static_overrides: Dict[str, float],
                          changing_variable_axis: str = None) -> ChartLineData:

        for key in list(self.default_values.keys()):
            exec(key + ' = ' + str(self.default_values[key]))

        changing_variable_values = []
        resultant_values = []

        variable = None

        for key in variable_static_overrides:
            val = variable_static_overrides[key]
            exec(key + ' = ' + str(val))

        for var in self.formula_data.variableData:
            if var.variableIdentifier == changing_variable_identifier:
                variable = var
                break

        for variable_value in variable.get_values():
            exec(variable.variableIdentifier + ' = ' + str(variable_value))
            resultant_values.append(eval(self.formula_data.formula))
            changing_variable_values.append(variable_value)

        if changing_variable_axis is None or changing_variable_axis == 'y':
            return ChartLineData(
                xValues=resultant_values,
                yValues=changing_variable_values,
                color=color,
                label=label)

        elif changing_variable_axis == 'x':
            return ChartLineData(
                xValues=changing_variable_values,
                yValues=resultant_values,
                color=color,
                label=label)

        else:
            raise Exception("changing_variable_axis must be 'x' or 'y'!")
