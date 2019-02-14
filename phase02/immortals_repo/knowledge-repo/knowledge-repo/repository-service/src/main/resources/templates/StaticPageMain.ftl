<#import "/spring.ftl" as spring/>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css"/>
<script type="text/javascript" src="/js/jquery-3.1.0.min.js"></script>
<script type="text/javascript" src="/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/js/cytoscape.min.js"></script>
<title>${title}</title>
<style>
.mycol{padding-left:0px}
.mycol{padding-right:0px}
.list-group-item:hover{background-color:lightblue;}
.claytonobject{min-height:60px}
div.tooltip-inner {
    max-width: 800px;
	}
</style>
<script >
//on page load
	$(function(){
		$("#notfound_tab").hide();
		$("#graphText").hide();
		$("#queryText").hide();
		graphName = $("#graphText").text();;
		individualUri=$("#queryText").text();
		request();
	});
</script>
<script>
//initializations
window.url = "/relationships/getRelatedNodes";
window.graphName=""//http://localhost:3030/ds/data/42a43412-d6f5-41cb-9140-c48068d29a70-IMMoRTALS-r2.0.0";
window.individualUri=""//"http://darpa.mil/immortals/ontology/r2.0.0/analysis#CotByterToDispatcher";
window.composed = "";
window.jsonp = "";
window.previous = [];
window.forwards = [];
window.engaged = false;
window.bootstrapme = function(){
	$.post("/immortalsRepositoryService/bootstrap",function(data){
		graphName = "http://localhost:3030/ds/data/"+data;
		//console.info(data);
	});
};
window.mytestvar = "http://localhost:8080/relationships/getRelatedNodes?graphName=doesntmatter&individualUri=PREFIX%20im%3A%20%3Chttp%3A%2F%2Fdarpa.mil%2Fimmortals%2Fontology%2Fr2.0.0%23%3E%20%0APREFIX%20dfu%3A%20%3Chttp%3A%2F%2Fdarpa.mil%2Fimmortals%2Fontology%2Fr2.0.0%2Fdfu%2Finstance%23%3E%20%0APREFIX%20lp_func%3A%20%3Chttp%3A%2F%2Fdarpa.mil%2Fimmortals%2Fontology%2Fr2.0.0%2Ffunctionality%2Flocationprovider%23%3E%20%0APREFIX%20bytecode%3A%20%3Chttp%3A%2F%2Fdarpa.mil%2Fimmortals%2Fontology%2Fr2.0.0%2Fbytecode%23%3E%20%0APREFIX%20rdf%3A%20%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23%3E%20%0APREFIX%20rdfs%3A%20%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E%20%0A%0ASELECT%20*%0AWHERE%20%7B%20%09%20%20%09%0A%20%20GRAPH%20%3Chttp%3A%2F%2Flocalhost%3A3030%2Fds%2Fdata%2Fd5cde715-037a-4e25-95a4-760d717c05cb-IMMoRTALS-r2.0.0%3E%20%7B%20%09%09%09%0A%20%20%20%20%3Fdfu%20a%20dfu%3ADfuInstance%20.%20%20%20%20%20%20%20%20%20%20%20%20%20%0A%20%20%20%20%3Fdfu%20im%3AhasFunctionalityAbstraction%20%3FfunctionalityUri%20.%20%20%20%20%20%20%20%20%20%20%20%20%20%0A%20%20%20%20%3Fdfu%20im%3AhasClassPointer%20%3FclassPointer%20.%20%20%20%20%20%20%20%20%20%20%20%20%20%0A%20%20%20%20%3Fclass%20a%20bytecode%3AAClass%20.%20%20%20%20%20%20%20%20%20%20%20%20%20%0A%20%20%20%20%3Fclass%20im%3AhasBytecodePointer%20%3FclassPointer%20.%20%20%20%20%20%20%20%20%20%20%20%20%20%0A%20%20%20%20%3Fclass%20im%3AhasClassName%20%3FclassName%20.%20%20%20%20%20%20%20%20%20%20%20%20%20%0A%20%20%20%20filter%20(%3FfunctionalityUri%20%3D%20%3Chttp%3A%2F%2Fdarpa.mil%2Fimmortals%2Fontology%2Fr2.0.0%2Ffunctionality%2Flocationprovider%23LocationProvider%3E)%20%20%20%20%0A%20%20%7D%7D%0A%0A%0A ";
</script>
<script>
window.cleartable = function(){
	$("#tablebody").empty();
	$(".tooltip").remove();
}

window.compose = function(){
		window.composed = url+"?"+"graphName="+encodeURIComponent(graphName)+"&individualUri="+encodeURIComponent(individualUri);
};

window.request = function(){
	window.compose();
	//console.info(window.composed);
	$.get(window.composed,function(data){
		//console.info(data);
		window.jsonp = data;
		window.cleartable();
		window.filltable();
		$('[data-toggle="tooltip"]').tooltip({container: 'body',delay:175})
	});
};

window.engage = function(){
	var inputbox = $("#uri_input")[0].value;
	if (inputbox){
		individualUri=inputbox;
	}
	request();
}

window.request_new = function(){
	window.test = $(this);
	var node = $(this);
	previous.push(individualUri);
	var desired = node.data("fulluri");
	window.individualUri=desired;
	window.request();
}

window.request_previous = function(){
	if(previous.length == 0){
		return 0;
	}
	else{
		forwards.unshift(individualUri);
		var wanted = previous.pop();
		individualUri=wanted;
		request();
	}
}

window.request_forwards = function(){
	if(forwards.length == 0){
		return 0;
	}
	else{
		previous.push(individualUri);
		var wanted = forwards.shift();
		individualUri=wanted;
		request();
	}
}

window.filltable = function(){
	var edges = jsonp.edges;
	var flag = 0;
	if (jsonp.nodeType === "QUERY_RESULT"){
		var subject = "Query";
		flag = 1;
	}
	else{
		var subject = jsonp.label;
	}
	var subject_type = jsonp.nodeType;
	
	for (i = 0; i < edges.length; i++){
		var edge = edges[i];
		var paster = generate_html(edge.name,edge.sink.label,edge.sink.nodeType,i);
		if (paster != 0){
			$("#tablebody").append(['<tr class="forcleaning">',paster.subject,paster.edge,paster.object,'</tr>']);
		}
	}
	var first;
	if (edges.length == 0){
		$("#tablebody").append('<tr class=""><td class="h-100 d-inline-block claytonsubject"></td><td class="h-100 d-inline-block claytonedge">no value</td><td class="h-100 d-inline-block well claytonobject novalue">no value</td>');
		first = $("#tablebody>tr>td").first();
	}
	else{
		first = $("#tablebody>td").first();
	}
	//$("#tablebody>td").first().html(myslice(subject));
	if (flag == 0){
		first.html(myslice(subject));
	}
	else{
		first.html(subject);
	}
	$(".claytonsubject").addClass("well");
	first.tooltip({
	container:"body",
	placement:"top",
	html:true,
	title:subject,
	});
	$(".claytonedge").addClass("well");
	$(".claytonobject").on("click",window.request_new);
	$(".claytonstatic").off("click");
	$(".novalue").off("click");
};

window.generate_html = function(name,label,nodeType,iteration){ //name is edge, label is object, nodeType is Object's nodeType
	var returnvalue = Object();
	if (!label){
		return 0;
	}
	returnvalue.subject = '<td class="h-100 d-inline-block claytonsubject forcleaning"> </td>';
	returnvalue.edge = '<td class="h-100 d-inline-block claytonedge forcleaning" data-fulluri="'+name+'" data-toggle="tooltip" data-placement="top" title="'+name+'">'+myslice(name,"#")+'</td>';
	if (nodeType === "OBJECT"){
		returnvalue.object = '<td class="h-100 d-inline-block forcleaning"><div data-toggle="tooltip" data-placement="top" title="'+label+'" class="h-100 btn btn-block btn-primary claytonobject" data-fulluri="'+label+'" data-nodetype="'+nodeType+'">'+myslice(label)+'</div></td>';
	}
	else if (nodeType === "QUERY_SOLUTION"){
		returnvalue.object = '<td class="h-100 d-inline-block forcleaning"><div data-toggle="tooltip" data-placement="top" title="'+label+'" class="h-100 btn btn-block btn-primary claytonobject" data-fulluri="'+label+'" data-nodetype="'+nodeType+'">'+'Solution' + iteration.toString() + '</div></td>';
	}
	else{
		returnvalue.object = '<td class="well d-inline-block h-100 claytonobject claytonstatic" style="margin-bottom:0px;" data-nodetype="'+nodeType+'"><em>'+label+'</em></td>';
	}
	return returnvalue;
};

window.myslice = function(string,delimiter = "/"){
	string = string.slice(string.lastIndexOf(delimiter)+1);
//	string = string.replace(/JJJJJ/g,"");
//	string = string.replace(/QQQQQ/g,"");
//	string = string.replace(/OBJECT/g,"");
	return string;
}

window.myslice2 = function(string,delimiter = "/"){
	string = string.replace(/JJJJJ/g,"");
	string = string.replace(/QQQQQ/g,"");
	string = string.replace(/OBJECT/g,"");
	return string;
}

</script>
</head>

<body>
<div id = "queryText" class="col-sm-12"><#escape query as query?xhtml>${query}</#escape></div>
<div id = "graphText" class="col-sm-12"><#escape graph as graph?xhtml>${graph}</#escape></div>
<div class="col-sm-12">
<div class="btn-toolbar mb-3" role="toolbar" aria-label="Toolbar with button groups">
	<div class="btn-group mr-2" role="group" aria-label="First group">
		<button type="button" class="btn btn-primary" id="backbutton" onclick="request_previous()">Back</button>
		<button type="button" class="btn btn-primary" id="forward" onclick="request_forwards()">Forward</button>
	</div>
	<div class="input-group col-sm-4">
	</div>
</div>
</div>


<div class="col-sm-8">
	<table class="table table-bordered">
		<thead class="thead-inverse">
			<tr>
				<th class="col-sm-4">Subject</th>
				<th class="col-sm-4">Predicate</th>
				<th class="col-sm-4">Object</th>
			</tr>
		</thead>
		
		<tbody id="tablebody">
		</tbody>
	</table>
</div>

</body>
</html>