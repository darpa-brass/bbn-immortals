#!/usr/bin/env python
#
# This is used to convert classes from the normal compilable format to
# templates. It currently only does the SACommunicationService file, and will
# probably be moved to a more centralized place once code generation is used
# for the server component.

import sys
import io
import os

deletionPointStartIdentifier = "2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceWith{"
deletionPointEndIdentifier = "2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceEnd"

sourceDirectory = "src/"
targetDirectory = "src-templates/"


def main():
    controlPointFile = "com/bbn/ataklite/service/SACommunicationService.java"
    targetTemplateFile =  "com/bbn/ataklite/service/SACommunicationService.java"

    sourceFile = open(sourceDirectory + controlPointFile)
    sourceLines = sourceFile.readlines()
    sourceFile.flush()
    sourceFile.close()

    targetLines = templatizeLines(sourceLines)
    writeLinesToFile(targetLines, targetDirectory + targetTemplateFile)


def templatizeLines(sourceFileLines):
    outputLines = []

    replacementString = None

    for line in sourceFileLines:
        if deletionPointStartIdentifier in line:
            if replacementString is None:
                if line.count('{') == 1 and line.count('}') == 1:
                    replacementString = line[line.find('{')+1:line.find('}')] + "\n";
                else:
                    raise Exception("Expected two '\"' in the deleation start point to enclose the replacement text but found none!")
            else:
                raise Exception("Successive Deletion start points found!")

        elif deletionPointEndIdentifier in line:
            if replacementString is not None:
                outputLines.append(replacementString)
                replacementString = None

            else:
                raise Exception("Deletion end found without being preceeded by a matching deletion start point!")

        elif replacementString is None:
            outputLines.append(line);

    return outputLines


def writeLinesToFile(lines, targetFilePath):
    dirPath = os.path.dirname(targetFilePath)

    if os.path.exists(dirPath):
        if os.listdir(dirPath) != []:
            raise Exception("Cannot output templates to already existing files! Please remove them first!")

    else:
        os.makedirs(dirPath)

    targetFile = open(targetFilePath, 'w')
    targetFile.writelines(lines)
    targetFile.flush()
    targetFile.close()

if __name__ == "__main__":
    main()
