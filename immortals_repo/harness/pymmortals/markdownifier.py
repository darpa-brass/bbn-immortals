from typing import List, Set, Dict, Optional

from pymmortals.datatypes.serializable import Serializable
from pymmortals.pojoizer import PojoClassBuilderConfig, import_type_omissions, AbstractPojoBuilderConfig, \
    DocumentationTag, PojoEnumBuilderConfig, Pojoizer, ConversionMethod


def _fillout_matrix(matrix: List[List[str]]):
    max_column_width = list() # type: List[int]

    # Seed the initial column values
    for ci in range(len(matrix[0])):
        max_column_width.append(len(matrix[0][ci]))

    # Go through the columns and set the max width to the proper max possible value
    for ri in range(len(matrix)):
        for ci in range(len(matrix[ri])):
            max_column_width[ci] = max(max_column_width[ci], len(matrix[ri][ci]))

    # Pad the values to match that width
    for ri in range(len(matrix)):
        for ci in range(len(matrix[ri])):
            if matrix[ri][ci] == '---':
                matrix[ri][ci] = matrix[ri][ci].ljust(max_column_width[ci], '-')
            else:
                matrix[ri][ci] = matrix[ri][ci].ljust(max_column_width[ci], ' ')


def _generate_markdown(config: AbstractPojoBuilderConfig,
                       apis: Optional[Set[DocumentationTag]],
                       omit_unstable: bool) -> \
        List[str]:
    lines = list()
    api_indentation_level = '#### '

    if apis is None or len(apis.intersection(config.apis)) > 0 and \
            (not omit_unstable or not config.unstable):
        if isinstance(config, PojoEnumBuilderConfig):
            lines.append('\n' + api_indentation_level + config.class_name + '  \n')
            lines.append('__Type__: String Constant  \n')
            lines.append('__Description__: ' + config.description[1:-1] + '  \n\n')

            matrix = list()
            header = list()
            header.append('Values')
            separator = list()
            separator.append('---')

            for field in config.fields:
                if not field.unstable:
                    header.append(field.name[:1].upper() + field.name[1:])
                    separator.append('---')

            matrix.append(header)
            matrix.append(separator)

            for value in config.instance_labels:
                row = [value]
                for data in config.instance_parameter_fields[value]:
                    if not data.unstable:
                        row.append(data.value[1:-1])
                matrix.append(row)

            _fillout_matrix(matrix)

            for l in matrix:
                lines.append('| ' + ' | '.join(l) + ' |  \n')

        elif isinstance(config, PojoClassBuilderConfig):
            lines.append('\n' + api_indentation_level + config.class_name + '  \n')
            lines.append('__Type__: JSON Object  \n')
            lines.append('__Description__: ' + config.description[1:-1] + '  \n\n')

            matrix = list()
            header = list()
            header.append('Field')
            header.append('Type')
            header.append('Description')
            separator = list()
            separator.append('---')
            separator.append('---')
            separator.append('---')

            matrix.append(header)
            matrix.append(separator)

            object_rows = list()
            for field in config.fields:
                if apis is None or len(apis.intersection(field.apis)) > 0:
                    row = list()
                    row.append(field.name)
                    row.append('Generic JSON Object' if (field.unstable and omit_unstable) else field.raw_type)
                    row.append(field.description[1:-1])
                    object_rows.append(row)
            matrix = matrix + sorted(object_rows, key=lambda x: x[0])

            _fillout_matrix(matrix)

            for l in matrix:
                lines.append('| ' + ' | '.join(l) + ' |  \n')

        return lines


class Markdownifier(Pojoizer):
    def __init__(self):
        super().__init__(conversion_method=ConversionMethod.VARS, do_generate_markdown=True)

    def get_display_order(self, root_config: AbstractPojoBuilderConfig, apis: Set[DocumentationTag],
                          omit_unstable: bool):
        rval = list() # type: List[str]

        def _get_display_order(config: AbstractPojoBuilderConfig, display_order: List[str]):
            if isinstance(config, PojoClassBuilderConfig):
                all_fields = sorted((config.fields + config.inherited_fields), key=lambda x: x.name)
            else:
                all_fields = config.fields

            for field in all_fields:
                if not (field.unstable and omit_unstable):
                    if len(apis.intersection(field.apis)) > 0:
                        if field.core_type() not in import_type_omissions and field.core_type() != config.class_name:
                            if field.core_type() not in display_order:
                                display_order.append(field.core_type())

                            field_classpath = config.imports[field.core_type()] \
                                [len(self.target_package) + 1:-(len(field.core_type()) + 1)]
                            next_config = next(
                                (x for x in self.path_classes[field_classpath] if x.class_name == field.core_type()),
                                None) # type: AbstractPojoBuilderConfig
                            _get_display_order(next_config, display_order)

        rval.append(root_config.class_name)

        _get_display_order(root_config, rval)
        return rval

    def markdownify(self, apis: Set[DocumentationTag], input_example: Serializable):
        return self._generate_pojo_spec_lines(apis=apis, omit_unstable=True,
                                                       root_class_name=input_example.__class__.__name__)

    def _generate_pojo_spec_lines(self, apis: Set[DocumentationTag], omit_unstable: bool, root_class_name: str) -> \
            List[str]:
        self._preprocess_class_data()

        lines = list() # type: List[str]
        name_lines_map = dict() # type: Dict[str, List[str]]
        display_order = None

        for key in self.path_classes:
            for config in self.path_classes[key]:
                if root_class_name is not None and root_class_name == config.class_name:
                    display_order = self.get_display_order(config, apis, omit_unstable)

                if not config.unstable and (apis is None
                                            or len(apis.intersection(config.apis)) > 0):
                    md_lines = _generate_markdown(
                        config=config, apis=apis, omit_unstable=omit_unstable)

                    if root_class_name is None:
                        lines = lines + md_lines
                    else:
                        name_lines_map[config.class_name] = md_lines

        if root_class_name is not None:
            if display_order is None:
                raise Exception('Unable to find root class "' + root_class_name + '"!')

            else:
                # Remove unstable items from the display order if necessary
                for key in self.path_classes:
                    for config in self.path_classes[key]:
                        if config.unstable and config.class_name in display_order:
                            display_order.remove(config.class_name)

                if set(display_order) != set(name_lines_map.keys()):
                    display_extras = set(display_order).difference(set(name_lines_map.keys()))
                    found_extras = set(name_lines_map.keys()).difference(set(display_order))

                    msg = ''
                    if len(display_extras) != 0:
                        msg += ('Extra values found in the ordering determination: ' + str(display_extras) + '.\n')

                    if len(found_extras) != 0:
                        msg += ('Extra values found in tagged classes: ' + str(found_extras) + '.\n')

                    msg += 'Are you sure your provided example matches the API tags the java is annotated with?'

                    raise Exception(msg)

                for key in display_order:
                    lines = lines + name_lines_map[key]

        return lines
