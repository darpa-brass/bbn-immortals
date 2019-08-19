import xml.etree.ElementTree as ET

text_block_xml_path = "../data/09_merged.xml"
tree = ET.parse(text_block_xml_path)
root = tree.getroot()
for child in root:
    print(child.tag, child.attrib)
a = 0