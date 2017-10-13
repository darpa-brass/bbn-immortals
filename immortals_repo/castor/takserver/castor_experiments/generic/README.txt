
1) Copy query files to examples/ folder.


4) Set data model.

Open castor-input/datamodel.json.
Set value of "headMode":
-Predicate name must match query name.
-Type of each attribute in head mode should match type of attribute in schema.
E.g., query1_all(+event,+source,+type)


5) Run Castor: run.sh.

Run run.sh.
Output will be written to out/out.txt.
Learned definition will be written to out/definition.txt.

If you want Castor to return all candidate queries, add option -globalDefinition when running Castor in run.sh file.
