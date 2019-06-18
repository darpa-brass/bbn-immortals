#!/usr/bin/env bash

# A hack to get around the odb scripts being unable to take MDL-violating XML

sed '/<\/NameValues>/{
	r s5_dauInventory.xml
	:a
	n
	ba
}' scenario5_input_mdlRoot.xml > scenario5.xml

