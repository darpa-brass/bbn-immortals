#!/bin/bash

# create schema
sqlcmd < ddl-schema1.sql

# load db
FILES=db-schema1/*.csv
for f in $FILES
do
  echo "Loading file $f..."
  # take action on each file. $f store current file name
  filename=$(basename "$f")
  # extension="${filename##*.}"
  filename="${filename%.*}"
  csvloader --file $f $filename -r ./log
done

# load examples
FILES=db-advisedby/*.csv
for f in $FILES
do
  echo "Loading file $f..."
  # take action on each file. $f store current file name
  filename=$(basename "$f")
  # extension="${filename##*.}"
  filename="${filename%.*}"
  csvloader --file $f $filename -r ./log
done

FILES=db-professorStudentCoauthor/*.csv
for f in $FILES
do
  echo "Loading file $f..."
  # take action on each file. $f store current file name
  filename=$(basename "$f")
  # extension="${filename##*.}"
  filename="${filename%.*}"
  csvloader --file $f $filename -r ./log
done

FILES=db-professorAdvancedStudentCoauthor/*.csv
for f in $FILES
do
  echo "Loading file $f..."
  # take action on each file. $f store current file name
  filename=$(basename "$f")
  # extension="${filename##*.}"
  filename="${filename%.*}"
  csvloader --file $f $filename -r ./log
done