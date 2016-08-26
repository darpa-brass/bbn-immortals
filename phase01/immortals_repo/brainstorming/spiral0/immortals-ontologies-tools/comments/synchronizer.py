import os
import re
import sys


def process_java_file(file, remove_only=False):
    """
    Synchronizes the Javadoc comments in a file, or removes existing comment annotations.
    :param file: The file to synchronize
    :param remove_only: Boolean flag for removing existing annotations only
    :return: None
    """
    if not file.readable():
        return None

    content = file.read()
    cleaned = remove_annotations(content)
    update = synchronize_comments(cleaned) if not remove_only else cleaned
    file.truncate(0)
    file.seek(0)
    file.write(update)


def remove_annotations(text):
    """
    Removes all existing RdfsComment annotations from the text string
    :param text: The text string to process
    :return: A text string with all RdfsComment annotations removed
    """
    annotation_name = r"^\s*@(com\.securboration\.immortals\.ontology\.annotations\.)?RdfsComment"

    # Opening parenthesis of the annotation parameter
    parameter_start = r"\(\s*"

    # Matches everything within quotes, including escaped quotes e.g. "this is a \"test\" line"
    quote_text = "(\"(" r"\\" "\"|[^\"])*\")"

    # Handles the case of the text being broken up across multiple lines and joined with a plus
    plus = r"\s*\+\s*\n\s*"

    # Closing parenthesis for the parameter
    parameter_end = r"\s*\)"

    # Parameter of the annotation, expecting quoted text possibly broken up across multiple lines
    annotation_parameter = parameter_start + quote_text + "(" + plus + quote_text + "|" + parameter_end + ")+"
    annotation_end = r"\s*?\n"

    return re.sub(annotation_name + annotation_parameter + annotation_end, '', text, flags=re.M)


def synchronize_comments(text):
    """
    Adds RdfsComment annotations for each set of Javadoc comments found in the text string
    :param text: The text string to process
    :return: A transformed text string with RdfsComment annotations after every Javadoc comment
    """
    out_text = ""
    start = 0

    # Find all Javadoc comments that are not commented out, capture the comment lines, and capture the amount of
    # white space on the following line (used to allow for proper indenting of the annotation).
    regex = r"(?!\s*//\s*)(?<=/\*\*)(?:\s*)(.*?)(?:\s*\*/\s*\n)(\s*)"

    for match in re.finditer(regex, text, re.S):
        # Remove leading white space and *s and escape quotes in the text.
        javadoc = " ".join([s.lstrip(" \t\r\f\v*").replace("\"", r"\"") for s in str(match.group(1)).split('\n')])

        # Insert the annotation inline with the existing text
        out_text += text[start:match.end()] + build_annotation(javadoc, len(match.group(2)))
        start = match.end()

    out_text += text[start:]

    return out_text


def build_annotation(comment, offset=0, indent=4, width=120):
    """
    Builds the annotation string to the specified line width by splitting the comment string onto newlines as necessary.
    It assumes that the annotation will be inserted in the correct position of the target string and will not pad the
    start of the annotation with the offset or indent.
    :param comment: The preprocessed comment text.
    :param offset: The newline offset for this annotation. All new comment lines will start at this offset +
                   indent amount.
    :param indent: The newline indent off of the start of the annotation (which is already at an offset, see above).
    :param width: The maximum line width for the output annotation.
    :return: The formatted annotation string
    """
    annotation = "@com.securboration.immortals.ontology.annotations.RdfsComment("

    line_start = "\""
    line_end = "\" +\n"
    annotation_end = "\")\n"
    line_prefix = ' ' * offset
    indent_space = ' ' * indent

    max_line_length = width - offset - indent - len(line_start) - len(line_end)

    if len(annotation) + len(comment) + len(line_start) + len(annotation_end) > width:
        split_comment = "\n"
        start = 0
        end = max_line_length

        while len(comment) - start > max_line_length:
            last_space = comment[start: end].rfind(' ')
            if last_space <= 0:
                last_space = end
            else:  # offset the relative last_space position by the start position
                last_space += start

            split_comment += line_prefix + indent_space + line_start + comment[start: last_space] + line_end

            start = last_space
            end = start + max_line_length

        # Grab the remainder of the comment
        annotation += split_comment + line_prefix + indent_space + line_start + comment[start:]
    else:
        annotation += line_start + comment

    annotation += annotation_end + line_prefix

    return annotation


if __name__ == "__main__":
    usage = """Usage: synchronizer.py [-r] path
    -r      Remove Mode: Removes all existing RdfsComment annotations in the processed file(s).
    path    Path to a java file or directory containing java files. In directory mode, the script will walk the
            directory processing all java files encountered."""

    path = sys.argv[len(sys.argv) - 1]

    # -r switch on the command line for "remove" mode to remove RdfsComment annotations from a file instead of update
    remove = False
    if len(sys.argv) > 2 and sys.argv[1] == "-r":
        remove = True

    if os.path.isdir(path):
        for dir_path, _, file_names in os.walk(path):
            file_list = [os.path.join(dir_path, f) for f in file_names if os.path.splitext(f)[1] == ".java"]
            for file_path in file_list:
                with open(file_path, 'r+') as java_file:
                    process_java_file(java_file, remove)

    elif os.path.isfile(path):
        if os.path.splitext(path)[1] == ".java":
            with open(path, 'r+') as java_file:
                process_java_file(java_file, remove)
        else:
            print("Error - Expecting Java file (.java)")
    else:
        print("Error - Invalid file or directory path.\n" + usage)
