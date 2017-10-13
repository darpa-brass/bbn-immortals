#!/bin/bash

# create schema
sqlcmd < ddl-schema-baseline-strings.sql

# load database

csvloader --separator "," --skip 1 --file large_data_set/source.csv source -r ./log
csvloader --separator "," --skip 1 --file large_data_set/cot_event.csv cot_event -r ./log
csvloader --separator "," --skip 1 --file large_data_set/cot_event_position.csv cot_event_position -r ./log
