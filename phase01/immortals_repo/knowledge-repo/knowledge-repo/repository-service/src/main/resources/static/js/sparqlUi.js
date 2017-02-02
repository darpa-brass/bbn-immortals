var serviceBase="http://localhost:8080";
var immortalsRepositoryService=serviceBase+"/immortalsRepositoryService";
var graphViewerService=serviceBase+"/graphViewerUi"

var graphs = [];
var initializedGraphList = false;
var sparqlEditor;
var queries = [];

$( document ).ready(function() {
	//add the SPARQL editor
    sparqlEditor = YASQE($('#sparqlBox'));
	sparqlEditor.setSize("100%", "25%");
	
	//populate graph names
	updateGraphNames($("#knowledgeGraphs"));
	
	//populate SPARQL queries
	addQueries();
});

function addQueries(){
	//var test = 'PREFIX im: <http:\/\/darpa.mil\/immortals\/ontology\/r2.0.0#> \nPREFIX dfu: <http:\/\/darpa.mil\/immortals\/ontology\/r2.0.0\/dfu\/instance#> \nPREFIX lp_func: <http:\/\/darpa.mil\/immortals\/ontology\/r2.0.0\/functionality\/locationprovider#> \nPREFIX bytecode: <http:\/\/darpa.mil\/immortals\/ontology\/r2.0.0\/bytecode#> \nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#> \nPREFIX rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#> \nPREFIX owl: <http:\/\/www.w3.org\/2002\/07\/owl#>\n\nSELECT *\nWHERE { \t  \t\n  GRAPH <http:\/\/localhost:3030\/ds\/data\/d5cde715-037a-4e25-95a4-760d717c05cb-IMMoRTALS-r2.0.0> { \t\t\t\n    ?dfu a owl:Class .             \n    ?dfu im:hasFunctionalityAbstraction ?functionalityUri .             \n    ?dfu im:hasClassPointer ?classPointer .             \n    ?class a bytecode:AClass .             \n    ?class im:hasBytecodePointer ?classPointer .             \n    ?class im:hasClassName ?className .             \n    filter (?functionalityUri = <http:\/\/darpa.mil\/immortals\/ontology\/r2.0.0\/functionality\/locationprovider#LocationProvider>)    \n  }\n}';
	
	var queryList = $("#queryList");
	
	addQuery(queryList,'[none]','');
	addQuery(queryList,'empty','');
	addQuery(queryList,'select 10 random triples','PREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\r\nPREFIX rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#>\r\nSELECT * WHERE {\r\n  GRAPH <$GRAPH> {\r\n    ?sub ?pred ?obj .\r\n  }\r\n} \r\nLIMIT 10');
	addQuery(queryList,'select 1000 random triples','PREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\r\nPREFIX rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#>\r\nSELECT * WHERE {\r\n  GRAPH <$GRAPH> {\r\n    ?sub ?pred ?obj .\r\n  }\r\n} \r\nLIMIT 1000');
	
	addQuery(queryList,'select all JARs','SELECT *\r\nWHERE { \t  \t\r\n  GRAPH <$GRAPH> { \t\t\t\r\n    ?jar a <http:\/\/darpa.mil\/immortals\/ontology\/r2.0.0\/bytecode#JarArtifact> .    \r\n  }\r\n}');
	addQuery(queryList,'select location provider DFUs','PREFIX im: <http:\/\/darpa.mil\/immortals\/ontology\/r2.0.0#> \r\nPREFIX dfu: <http:\/\/darpa.mil\/immortals\/ontology\/r2.0.0\/dfu\/instance#> \r\nPREFIX lp_func: <http:\/\/darpa.mil\/immortals\/ontology\/r2.0.0\/functionality\/locationprovider#> \r\nPREFIX bytecode: <http:\/\/darpa.mil\/immortals\/ontology\/r2.0.0\/bytecode#> \r\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#> \r\nPREFIX rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#> \r\nPREFIX owl: <http:\/\/www.w3.org\/2002\/07\/owl#>\r\n\r\nSELECT *\r\nWHERE { \t  \t\r\n  GRAPH <$GRAPH> { \t\t\t\r\n    ?dfu a dfu:DfuInstance .             \r\n    ?dfu im:hasFunctionalityAbstraction ?functionalityUri .             \r\n    ?dfu im:hasClassPointer ?classPointer .             \r\n    ?class a bytecode:AClass .             \r\n    ?class im:hasBytecodePointer ?classPointer .             \r\n    ?class im:hasClassName ?className .             \r\n    filter (?functionalityUri = <http:\/\/darpa.mil\/immortals\/ontology\/r2.0.0\/functionality\/locationprovider#LocationProvider>)    \r\n  }\r\n}');
	addQuery(queryList,'select all JARs containing a location provider DFU','PREFIX im: <http:\/\/darpa.mil\/immortals\/ontology\/r2.0.0#> \r\nPREFIX dfu: <http:\/\/darpa.mil\/immortals\/ontology\/r2.0.0\/dfu\/instance#> \r\nPREFIX lp_func: <http:\/\/darpa.mil\/immortals\/ontology\/r2.0.0\/functionality\/locationprovider#> \r\nPREFIX bytecode: <http:\/\/darpa.mil\/immortals\/ontology\/r2.0.0\/bytecode#> \r\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#> \r\nPREFIX rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#> \r\nPREFIX owl: <http:\/\/www.w3.org\/2002\/07\/owl#>\r\n\r\nSELECT ?jar\r\nWHERE { \t  \t\r\n  GRAPH <$GRAPH> { \t\t\t\r\n    ?dfu a dfu:DfuInstance .             \r\n    ?dfu im:hasFunctionalityAbstraction <http:\/\/darpa.mil\/immortals\/ontology\/r2.0.0\/functionality\/locationprovider#LocationProvider> .             \r\n    ?dfu im:hasClassPointer ?classPointer .             \r\n    ?class a bytecode:AClass .             \r\n    ?class im:hasBytecodePointer ?classPointer .\r\n    ?classArtifact im:hasClassModel ?class .\r\n    ?jar a bytecode:JarArtifact .\r\n    ?jar im:hasJarContents ?classArtifact . \r\n  }\r\n}');
	
}

function addQuery(queryList,queryTag,queryText){
	queryList.append('<option>'+queryTag+'</option>');
	queries[queryTag] = queryText;
}

function updateSparql(){
	var query = getSelectedQuery();
	sparqlEditor.setValue(queries[query]);
	sparqlEditor.setSize("100%", "25%");
}

function getSparql(){
	var sparql = sparqlEditor.getValue();
	console.log(sparql);
	return sparql;
}

function getSelectedQuery(){
	var selected = $('#queryList').find(":selected").text();
	console.log(selected);
	return selected;
}

function getSelectedGraph(){
	var selected = $('#knowledgeGraphs').find(":selected").text();
	console.log(selected);
	return selected;
}

function executeQuery(){
	var graphName = getSelectedGraph();
	var sparql = getSparql();
	
	var queryUrl = sparql;
	queryUrl = queryUrl.replace("\$GRAPH",graphName);
	queryUrl = encodeURIComponent(queryUrl);
	queryUrl = graphViewerService + '?query=' + queryUrl;
	
	console.log('queryURL is ' + queryUrl);
	
	$('#visualizationIFrame').attr('src',queryUrl);
	//'?query=PREFIX%20im%3A%20%3Chttp%3A%2F%2Fdarpa.mil%2Fimmortals%2Fontology%2Fr2.0.0%23%3E%20%0APREFIX%20dfu%3A%20%3Chttp%3A%2F%2Fdarpa.mil%2Fimmortals%2Fontology%2Fr2.0.0%2Fdfu%2Finstance%23%3E%20%0APREFIX%20lp_func%3A%20%3Chttp%3A%2F%2Fdarpa.mil%2Fimmortals%2Fontology%2Fr2.0.0%2Ffunctionality%2Flocationprovider%23%3E%20%0APREFIX%20bytecode%3A%20%3Chttp%3A%2F%2Fdarpa.mil%2Fimmortals%2Fontology%2Fr2.0.0%2Fbytecode%23%3E%20%0APREFIX%20rdf%3A%20%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23%3E%20%0APREFIX%20rdfs%3A%20%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E%20%0A%0ASELECT%20*%0AWHERE%20{%20%09%20%20%09%0A%20%20GRAPH%20%3Chttp%3A%2F%2Flocalhost%3A3030%2Fds%2Fdata%2Fb68ba330-4fa0-4a6a-b341-f629c91195cc-IMMoRTALS-r2.0.0%3E%20{%20%09%09%09%0A%20%20%20%20%3Fdfu%20a%20dfu%3ADfuInstance%20.%20%20%20%20%20%20%20%20%20%20%20%20%20%0A%20%20%20%20%3Fdfu%20im%3AhasFunctionalityAbstraction%20%3FfunctionalityUri%20.%20%20%20%20%20%20%20%20%20%20%20%20%20%0A%20%20%20%20%3Fdfu%20im%3AhasClassPointer%20%3FclassPointer%20.%20%20%20%20%20%20%20%20%20%20%20%20%20%0A%20%20%20%20%3Fclass%20a%20bytecode%3AAClass%20.%20%20%20%20%20%20%20%20%20%20%20%20%20%0A%20%20%20%20%3Fclass%20im%3AhasBytecodePointer%20%3FclassPointer%20.%20%20%20%20%20%20%20%20%20%20%20%20%20%0A%20%20%20%20%3Fclass%20im%3AhasClassName%20%3FclassName%20.%20%20%20%20%20%20%20%20%20%20%20%20%20%0A%20%20%20%20filter%20(%3FfunctionalityUri%20%3D%20%3Chttp%3A%2F%2Fdarpa.mil%2Fimmortals%2Fontology%2Fr2.0.0%2Ffunctionality%2Flocationprovider%23LocationProvider%3E)%20%20%20%20%0A%20%20}}%0A%0A%0A'
}

function clearGraphs(){
	console.log("clearing graphs");
	
	var args = {
			type: "DELETE",
			url: immortalsRepositoryService + "/zeroizeFuseki",
			success: function(data){
				var value = console.log(data);
				$("#knowledgeGraphs").empty();
			}
		};
		
		$.ajax(args);
}

function bootstrap(){
	console.log("bootstrapping");
	
	var args = {
			type: "POST",
			url: immortalsRepositoryService + "/bootstrap",
			success: function(data){
				var value = console.log(data);
			}
		};
		
		$.ajax(args);
}

function updateGraphNames(selectList){
	$.get( immortalsRepositoryService+"/graphs", function( graphNames ) {
		
		if(!initializedGraphList){
			selectList.empty();
			initializedGraphList = true;
		}
		
		//add anything new
		for (var i = 0; i < graphNames.length; i++) {
			var graphName = graphNames[i];
			
			if($.inArray(graphName,graphs) === -1){
				selectList.append('<option>'+graphName+'</option>');
				graphs.push(graphName);
			}
		}
		
		setTimeout(function(){updateGraphNames(selectList);},3000);
	});
}