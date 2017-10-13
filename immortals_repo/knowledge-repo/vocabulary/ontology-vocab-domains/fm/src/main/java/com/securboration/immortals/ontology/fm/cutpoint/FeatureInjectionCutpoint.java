package com.securboration.immortals.ontology.fm.cutpoint;

import com.securboration.immortals.ontology.core.HumanReadable;
import com.securboration.immortals.ontology.core.UniquelyIdentifiable;

/**
 * Describes the intersection of a point of variation with an application. E.g.,
 * a Feature might need to be injected via dependency injection.
 * 
 * @author jstaples
 *
 */
public abstract class FeatureInjectionCutpoint implements HumanReadable, UniquelyIdentifiable {
	
	/**
	 * A human readable description of the cutpoint
	 */
	private String description;
	
	/**
	 * A unique identifier for the cutpoint
	 */
	private String uuid;
	
	/**
	 * A code template into which the feature can be injected
	 */
	private String injectedCodeTemplate;

	@Override
	public String getHumanReadableDesc() {
		return description;
	}

	@Override
	public String getUuid() {
		return uuid;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getInjectedCodeTemplate() {
		return injectedCodeTemplate;
	}

	public void setInjectedCodeTemplate(String injectedCodeTemplate) {
		this.injectedCodeTemplate = injectedCodeTemplate;
	}
	
//	IMMoRTALS_resources:Vp1--LocationProvider-566e516f-f986-debe-9546-1cf805e846da IMMoRTALS:name "VP1: Location Provider";
//    a IMMoRTALS_resources:VariationPoint;
//    IMMoRTALS:base IMMoRTALS_resources:VariationPoint;
//    IMMoRTALS:hasUuid "BCC0A94D-C2B1-40AD-8056-E3DDBD46585E";
//    IMMoRTALS:hasOwnerClass IMMoRTALS_bytecode:SaCommService-c627cc37-0bcc-b4df-a1c8-0713476fd1a3;
//    IMMoRTALS:hasDataflowEntryPoint IMMoRTALS_cp2:cp2DataflowGraph;
//    IMMoRTALS:hasUsageParadigm <http://darpa.mil/immortals/ontology/r2.0.0/spec#DeclareInitWorkClean>;
//    IMMoRTALS:hasBinding IMMoRTALS_resources:ImageReportRate-62f7d22b-e810-0ac8-6218-d1b0a2bd57ef, IMMoRTALS_resources:DefaultImageSize-a8de7034-9b7d-c479-bf80-c6b3f4d5b727, IMMoRTALS_resources:TotalAvailableServerBandwidth-af54e16a-71af-9c38-dee3-d8a1cdc60848, IMMoRTALS_resources:NumberOfClients-3d54a70c-abd9-5cb1-5060-37ddf48a2bd5, IMMoRTALS_resources:PliReportRate-095c6061-2eaa-6ec2-3f64-3112ffb5c670;
//    IMMoRTALS:hasResourceProfile IMMoRTALS_resources:BandwidthResourceProfile-06c369ac-cf1d-d96e-e85d-ac8d3b964c5c;
//    IMMoRTALS:hasResource IMMoRTALS_resources_network:NetworkBandwidth;
//    IMMoRTALS:hasAClass IMMoRTALS_bytecode:SaCommService-c627cc37-0bcc-b4df-a1c8-0713476fd1a3;
//    IMMoRTALS:hasFunctionalityAbstraction IMMoRTALS_functionality_locationprovider:LocationProvider;
//    IMMoRTALS:hasAspect IMMoRTALS_functionality_locationprovider:GetCurrentLocationAspect-81776732-89cc-3137-f132-fd7597bcd11e.
	
//	"PREFIX res: <http://darpa.mil/immortals/ontology/r2.0.0/resources#> " +
//			"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
//
//			"SELECT ?vp_uri ?uuid ?class_name ?class_url ?dataflowEntryPoint ?usageParadigm ?functionalityAbstractionUri ?template_url " +
//			"WHERE { " +
//			"GRAPH <" + bootstrapUri + "> { " +
//			"  ?vp_uri a res:VariationPoint . " + 
//			"  ?vp_uri im:hasUuid ?uuid . " +
//			"  ?vp_uri im:hasOwnerClass ?class . " +
//			"  ?class im:hasClassName ?class_name . " +
//			"  ?class im:hasClassUrl ?class_url . " +
//			" ?class im:hasTemplateFile ?template_url . " +
//			"  ?vp_uri im:hasDataflowEntryPoint ?dataflowEntryPoint . " +
//			"  ?vp_uri im:hasFunctionalityAbstraction ?functionalityUri . " +
//			" ?vp_uri im:hasUsageParadigm ?usageParadigm . " +
//			" } " +
//			"FILTER (?functionalityUri = " + functionalitySpecification.getFunctionalityUri() + ") " +
//			"} ";


}
