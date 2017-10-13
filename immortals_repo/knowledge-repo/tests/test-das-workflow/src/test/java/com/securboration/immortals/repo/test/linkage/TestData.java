package com.securboration.immortals.repo.test.linkage;


public class TestData {
    
    public static final String inputFromBytecodeAnalysis = "" +
            "<http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#DfuInstance-35ca4d39-5f63-4651-8a0c-d3512cb66a86>\r\n" + 
            "        a       <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#DfuInstance> ;\r\n" + 
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasClassPointer>\r\n" + 
            "                \"noPdMEeuRMUaUp9m2HZ/ACj/+9ETbsw2C9pWyQn4X6I=\" ;\r\n" + 
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasDfuProperties>\r\n" + 
            "                <http://darpa.mil/immortals/ontology/r2.0.0/resources/gps/properties#TrustedProperty-c790aafb-7d2c-4dbe-80aa-7cfea279021e> ;\r\n" + 
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasFunctionalAspects>\r\n" + 
            "                <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#FunctionalAspectInstance-18719437-d956-4579-897e-6c29acb5cdbb> , <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#FunctionalAspectInstance-b26fb015-ada0-48b3-8568-6cfa5107c446> ;\r\n" + 
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasFunctionalityAbstraction>\r\n" + 
            "                <http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#LocationProvider> .\r\n" + 
            "#these should be inferred using the UCR analysis\r\n" + 
            "#        <http://darpa.mil/immortals/ontology/r2.0.0#hasResourceDependencies>\r\n" + 
            "#                <http://darpa.mil/immortals/ontology/r2.0.0/resources#UsbResource> , <http://darpa.mil/immortals/ontology/r2.0.0/resources/gps#GpsSatelliteConstellation> .\r\n" + 
            "            \r\n" + 
            "<http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#FunctionalAspectInstance-18719437-d956-4579-897e-6c29acb5cdbb>\r\n" + 
            "        a       <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#FunctionalAspectInstance> ;\r\n" + 
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasAbstractAspect>\r\n" + 
            "                <http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#GetCurrentLocationAspect> ;\r\n" + 
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasMethodPointer>\r\n" + 
            "                \"noPdMEeuRMUaUp9m2HZ/ACj/+9ETbsw2C9pWyQn4X6I=/methods/getTrustedLocation()Lmil/darpa/immortals/datatypes/Coordinates;\" ;\r\n" + 
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasReturnValueToSemanticType>\r\n" + 
            "                <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#ReturnValueToSemanticTypeBinding-543fa401-225e-41aa-9876-72beab9a47ee> .\r\n" + 
            "                \r\n" + 
            "<http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#FunctionalAspectInstance-b26fb015-ada0-48b3-8568-6cfa5107c446>\r\n" + 
            "        a       <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#FunctionalAspectInstance> ;\r\n" + 
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasAbstractAspect>\r\n" + 
            "                <http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#InitializeAspect> ;\r\n" + 
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasArgsToSemanticTypes>\r\n" + 
            "                <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#ArgToSemanticTypeBinding-90fdc1ea-0a56-4f70-84a7-78727799d319> ;\r\n" + 
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasMethodPointer>\r\n" + 
            "                \"noPdMEeuRMUaUp9m2HZ/ACj/+9ETbsw2C9pWyQn4X6I=/methods/initialize(Landroid/content/Context;)V\" ;\r\n" + 
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasReturnValueToSemanticType>\r\n" + 
            "                <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#ReturnValueToSemanticTypeBinding-221540a1-1e82-4a28-97b0-49f99b327df6> .";
    
    public static final String inputFromUcr = "" +
            "@prefix IMMoRTALS_resources_gps: <http://darpa.mil/immortals/ontology/r2.0.0/resources/gps#> .\r\n" + 
            "@prefix IMMoRTALS_analysis_profiling_properties: <http://darpa.mil/immortals/ontology/r2.0.0/analysis/profiling/properties#> .\r\n" + 
            "@prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> .\r\n" + 
            "@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n" + 
            "@prefix owl:   <http://www.w3.org/2002/07/owl#> .\r\n" + 
            "@prefix IMMoRTALS_measurement: <http://darpa.mil/immortals/ontology/r2.0.0/measurement#> .\r\n" + 
            "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\r\n" + 
            "@prefix IMMoRTALS_analysis: <http://darpa.mil/immortals/ontology/r2.0.0/analysis#> .\r\n" + 
            "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\r\n" + 
            "@prefix IMMoRTALS_analysis_profiling: <http://darpa.mil/immortals/ontology/r2.0.0/analysis/profiling#> .\r\n" + 
            "\r\n" + 
            "IMMoRTALS_analysis:AnalysisReport-4b4c0fee-5a28-4e14-b490-d5972cc47161\r\n" + 
            "        a       IMMoRTALS_analysis:AnalysisReport ;\r\n" + 
            "        IMMoRTALS:hasDiscoveredDependency\r\n" + 
            "                IMMoRTALS_analysis_profiling:SimpleResourceDependencyAssertion-822f571d-2196-4525-91a4-b324bd0add8c ;\r\n" + 
            "        IMMoRTALS:hasMeasurementProfile\r\n" + 
            "                IMMoRTALS_measurement:MeasurementProfile-6763dd0f-3cd2-4b82-8b3e-5ac825f3aa3a .\r\n" + 
            "                \r\n" + 
            "IMMoRTALS_analysis_profiling:SimpleResourceDependencyAssertion-822f571d-2196-4525-91a4-b324bd0add8c\r\n" + 
            "        a                        IMMoRTALS_analysis_profiling:SimpleResourceDependencyAssertion ;\r\n" + 
            "        IMMoRTALS:hasDependency  IMMoRTALS_resources_gps:GpsReceiver .\r\n" + 
            "\r\n" + 
            "IMMoRTALS_measurement:MeasurementProfile-6763dd0f-3cd2-4b82-8b3e-5ac825f3aa3a\r\n" + 
            "        a                         IMMoRTALS_measurement:MeasurementProfile ;\r\n" + 
            "        IMMoRTALS:hasCodeUnit     IMMoRTALS_measurement:CodeUnitPointer-5db020e9-a9ce-493b-b3bb-8dc6b2ca46aa ;\r\n" + 
            "        IMMoRTALS:hasMeasurement  IMMoRTALS_measurement:MeasurementInstance-1b283f28-4d84-4230-bb13-0f59dee3000f , IMMoRTALS_measurement:MeasurementInstance-0f8c33b1-071a-4dce-9168-2088acd430b9 .\r\n" + 
            "\r\n" + 
            "IMMoRTALS_measurement:CodeUnitPointer-5db020e9-a9ce-493b-b3bb-8dc6b2ca46aa\r\n" + 
            "        a                           IMMoRTALS_measurement:CodeUnitPointer ;\r\n" + 
            "        IMMoRTALS:hasClassName      \"mil/darpa/immortals/dfus/location/LocationProviderSaasmSimulated.class\" ;\r\n" + 
            "        IMMoRTALS:hasMethodName     \"getTrustedLocation\" ;\r\n" + 
            "        IMMoRTALS:hasPointerString  \"noPdMEeuRMUaUp9m2HZ/ACj/+9ETbsw2C9pWyQn4X6I=/methods/getTrustedLocation()Lmil/darpa/immortals/datatypes/Coordinates;\" .\r\n" + 
            "\r\n" + 
            "";
    
    
    public static final String ucrBadInput = "" +
            "@prefix IMMoRTALS_resources_gps: <http://darpa.mil/immortals/ontology/r2.0.0/resources/gps#> .\r\n" +
            "@prefix IMMoRTALS_analysis_profiling_properties: <http://darpa.mil/immortals/ontology/r2.0.0/analysis/profiling/properties#> .\r\n" +
            "@prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> .\r\n" +
            "@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n" +
            "@prefix owl:   <http://www.w3.org/2002/07/owl#> .\r\n" +
            "@prefix IMMoRTALS_measurement: <http://darpa.mil/immortals/ontology/r2.0.0/measurement#> .\r\n" +
            "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\r\n" +
            "@prefix IMMoRTALS_analysis: <http://darpa.mil/immortals/ontology/r2.0.0/analysis#> .\r\n" +
            "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\r\n" +
            "@prefix IMMoRTALS_analysis_profiling: <http://darpa.mil/immortals/ontology/r2.0.0/analysis/profiling#> .\r\n" +
            "\r\n" +
            "IMMoRTALS_analysis:AnalysisReport-4b4c0fee-5a28-4e14-b490-d5972cc47161\r\n" +
            "        a       IMMoRTALS_analysis:AnalysisReport ;\r\n" +
            "        IMMoRTALS:hasDiscoveredDependency\r\n" +
            "                IMMoRTALS_analysis_profiling:SimpleResourceDependencyAssertion-822f571d-2196-4525-91a4-b324bd0add8c ;\r\n" +
            "        IMMoRTALS:hasMeasurementProfile\r\n" +
            "                IMMoRTALS_measurement:MeasurementProfile-6763dd0f-3cd2-4b82-8b3e-5ac825f3aa3a .\r\n" +
            "                \r\n" +
            "IMMoRTALS_measurement:MeasurementProfile-6763dd0f-3cd2-4b82-8b3e-5ac825f3aa3a\r\n" +
            "        a                         IMMoRTALS_measurement:MeasurementProfile ;\r\n" +
            "        IMMoRTALS:hasCodeUnit     IMMoRTALS_measurement:CodeUnitPointer-5db020e9-a9ce-493b-b3bb-8dc6b2ca46aa ;\r\n" +
            "        IMMoRTALS:hasMeasurement  IMMoRTALS_measurement:MeasurementInstance-1b283f28-4d84-4230-bb13-0f59dee3000f , IMMoRTALS_measurement:MeasurementInstance-0f8c33b1-071a-4dce-9168-2088acd430b9 .\r\n" +
            "\r\n" +
            "IMMoRTALS_measurement:CodeUnitPointer-5db020e9-a9ce-493b-b3bb-8dc6b2ca46aa\r\n" +
            "        a                           IMMoRTALS_measurement:CodeUnitPointer ;\r\n" +
            "        IMMoRTALS:hasClassName      \"mil/darpa/immortals/dfus/location/LocationProviderSaasmSimulated.class\" ;\r\n" +
            "        IMMoRTALS:hasMethodName     \"getTrustedLocation\" ;\r\n" +
            "        IMMoRTALS:hasPointerString  \"noPdMEeuRMUaUp9m2HZ/ACj/+9ETbsw2C9pWyQn4X6I=/methods/getTrustedLocation()Lmil/darpa/immortals/datatypes/Coordinates;\" .\r\n" +
            "\r\n" +
            "";
    
    public static final String customSecurInput = "" +
            "<http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#DfuInstance-35ca4d39-5f63-4651-8a0c-d3512cb66a86>\r\n" +
            "        a       <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#DfuInstance> ;\r\n" +
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasClassPointer>\r\n" +
            "                \"noPdMEeuRMUaUp9m2HZ/ACj/+9ETbsw2C9pWyQn4X6I=\" ;\r\n" +
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasDfuProperties>\r\n" +
            "                <http://darpa.mil/immortals/ontology/r2.0.0/resources/gps/properties#TrustedProperty-c790aafb-7d2c-4dbe-80aa-7cfea279021e> ;\r\n" +
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasFunctionalAspects>\r\n" +
            "                <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#FunctionalAspectInstance-18719437-d956-4579-897e-6c29acb5cdbb> , <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#FunctionalAspectInstance-b26fb015-ada0-48b3-8568-6cfa5107c446> ;\r\n" +
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasFunctionalityAbstraction>\r\n" +
            "                <http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#LocationProvider> .\r\n" +
            "#these should be inferred using the UCR analysis\r\n" +
            "#        <http://darpa.mil/immortals/ontology/r2.0.0#hasResourceDependencies>\r\n" +
            "#                <http://darpa.mil/immortals/ontology/r2.0.0/resources#UsbResource> , <http://darpa.mil/immortals/ontology/r2.0.0/resources/gps#GpsSatelliteConstellation> .\r\n" +
            "            \r\n" +
            "<http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#DfuInstance-35ca4d39-5f63-4651-8a0c-d3512cb66a87>\r\n" +
            "        a       <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#DfuInstance> ;\r\n" +
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasClassPointer>\r\n" +
            "                \"noPdMEeuRMUaUp9m2HZ/ACj/+9ETbsw2C9pWyQn4X6I=\" ;\r\n" +
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasDfuProperties>\r\n" +
            "                <http://darpa.mil/immortals/ontology/r2.0.0/resources/gps/properties#TrustedProperty-c790aafb-7d2c-4dbe-80aa-7cfea279021e> ;\r\n" +
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasFunctionalAspects>\r\n" +
            "                <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#FunctionalAspectInstance-18719437-d956-4579-897e-6c29acb5cdbc> , <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#FunctionalAspectInstance-b26fb015-ada0-48b3-8568-6cfa5107c446> ;\r\n" +
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasFunctionalityAbstraction>\r\n" +
            "                <http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#LocationProvider> .\r\n" +
            "#these should be inferred using the UCR analysis\r\n" +
            "#        <http://darpa.mil/immortals/ontology/r2.0.0#hasResourceDependencies>\r\n" +
            "#                <http://darpa.mil/immortals/ontology/r2.0.0/resources#UsbResource> , <http://darpa.mil/immortals/ontology/r2.0.0/resources/gps#GpsSatelliteConstellation> .\r\n" +
            "            \r\n" +
            "<http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#FunctionalAspectInstance-18719437-d956-4579-897e-6c29acb5cdbb>\r\n" +
            "        a       <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#FunctionalAspectInstance> ;\r\n" +
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasAbstractAspect>\r\n" +
            "                <http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#GetCurrentLocationAspect> ;\r\n" +
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasMethodPointer>\r\n" +
            "                \"noPdMEeuRMUaUp9m2HZ/ACj/+9ETbsw2C9pWyQn4X6I=/methods/getTrustedLocation()Lmil/darpa/immortals/datatypes/Coordinates;\" ;\r\n" +
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasReturnValueToSemanticType>\r\n" +
            "                <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#ReturnValueToSemanticTypeBinding-543fa401-225e-41aa-9876-72beab9a47ee> .\r\n" +
            "                \r\n" +
            
            "<http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#FunctionalAspectInstance-b26fb015-ada0-48b3-8568-6cfa5107c446>\r\n" +
            "        a       <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#FunctionalAspectInstance> ;\r\n" +
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasAbstractAspect>\r\n" +
            "                <http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#InitializeAspect> ;\r\n" +
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasArgsToSemanticTypes>\r\n" +
            "                <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#ArgToSemanticTypeBinding-90fdc1ea-0a56-4f70-84a7-78727799d319> ;\r\n" +
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasMethodPointer>\r\n" +
            "                \"noPdMEeuRMUaUp9m2HZ/ACj/+9ETbsw2C9pWyQn4X6I=/methods/initialize(Landroid/content/Context;)V\" ;\r\n" +
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasReturnValueToSemanticType>\r\n" +
            "                <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#ReturnValueToSemanticTypeBinding-221540a1-1e82-4a28-97b0-49f99b327df6> .\r\n" +
            
            "<http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#FunctionalAspectInstance-18719437-d956-4579-897e-6c29acb5cdbc>\r\n" +
            "        a       <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#FunctionalAspectInstance> ;\r\n" +
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasAbstractAspect>\r\n" +
            "                <http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#GetCurrentLocationAspect> ;\r\n" +
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasMethodPointer>\r\n" +
            "                \"noPdMEeuRMUaUp9m2HZ/ACj/+9ETbsw2C9pWyQn4X6I=/methods/getTrustedLocation()Lmil/darpa/immortals/datatypes/Coordinates;\" ;\r\n" +
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasReturnValueToSemanticType>\r\n" +
            "                <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#ReturnValueToSemanticTypeBinding-543fa401-225e-41aa-9876-72beab9a47ee> .\r\n" +
            "                \r\n";

    public static final String customUcrInput = "" +
            "@prefix IMMoRTALS_resources_gps: <http://darpa.mil/immortals/ontology/r2.0.0/resources/gps#> .\r\n" +
            "@prefix IMMoRTALS_analysis_profiling_properties: <http://darpa.mil/immortals/ontology/r2.0.0/analysis/profiling/properties#> .\r\n" +
            "@prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> .\r\n" +
            "@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n" +
            "@prefix owl:   <http://www.w3.org/2002/07/owl#> .\r\n" +
            "@prefix IMMoRTALS_measurement: <http://darpa.mil/immortals/ontology/r2.0.0/measurement#> .\r\n" +
            "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\r\n" +
            "@prefix IMMoRTALS_analysis: <http://darpa.mil/immortals/ontology/r2.0.0/analysis#> .\r\n" +
            "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\r\n" +
            "@prefix IMMoRTALS_analysis_profiling: <http://darpa.mil/immortals/ontology/r2.0.0/analysis/profiling#> .\r\n" +
            "\r\n" +
            "IMMoRTALS_analysis:AnalysisReport-4b4c0fee-5a28-4e14-b490-d5972cc47161\r\n" +
            "        a       IMMoRTALS_analysis:AnalysisReport ;\r\n" +
            "        IMMoRTALS:hasDiscoveredDependency\r\n" +
            "                IMMoRTALS_analysis_profiling:SimpleResourceDependencyAssertion-822f571d-2196-4525-91a4-b324bd0add8c ;\r\n" +
            "        IMMoRTALS:hasMeasurementProfile\r\n" +
            "                IMMoRTALS_measurement:MeasurementProfile-6763dd0f-3cd2-4b82-8b3e-5ac825f3aa3a .\r\n" +
            "                \r\n" +
            "IMMoRTALS_analysis_profiling:SimpleResourceDependencyAssertion-822f571d-2196-4525-91a4-b324bd0add8c\r\n" +
            "        a                        IMMoRTALS_analysis_profiling:SimpleResourceDependencyAssertion ;\r\n" +
            "        IMMoRTALS:hasDependency  IMMoRTALS_resources_gps:GpsReceiver .\r\n" +
            "\r\n" +
            "IMMoRTALS_measurement:MeasurementProfile-6763dd0f-3cd2-4b82-8b3e-5ac825f3aa3a\r\n" +
            "        a                         IMMoRTALS_measurement:MeasurementProfile ;\r\n" +
            "        IMMoRTALS:hasCodeUnit     IMMoRTALS_measurement:CodeUnitPointer-5db020e9-a9ce-493b-b3bb-8dc6b2ca46aa ;\r\n" +
            "        IMMoRTALS:hasMeasurement  IMMoRTALS_measurement:MeasurementInstance-1b283f28-4d84-4230-bb13-0f59dee3000f , IMMoRTALS_measurement:MeasurementInstance-0f8c33b1-071a-4dce-9168-2088acd430b9 .\r\n" +
            "\r\n" +
            "IMMoRTALS_measurement:CodeUnitPointer-5db020e9-a9ce-493b-b3bb-8dc6b2ca46aa\r\n" +
            "        a                           IMMoRTALS_measurement:CodeUnitPointer ;\r\n" +
            "        IMMoRTALS:hasClassName      \"mil/darpa/immortals/dfus/location/LocationProviderSaasmSimulated.class\" ;\r\n" +
            "        IMMoRTALS:hasMethodName     \"getTrustedLocation\" ;\r\n" +
            "        IMMoRTALS:hasPointerString  \"noPdMEeuRMUaUp9m2HZ/ACj/+9ETbsw2C9pWyQn4X6I=/methods/getTrustedLocation()Lmil/darpa/immortals/datatypes/Coordinates;\" .\r\n" +
            "\r\n" +
            "";
    
    public static final String expectedOutput = "" +
            "<http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#DfuInstance-35ca4d39-5f63-4651-8a0c-d3512cb66a86>\r\n" + 
            "        <http://darpa.mil/immortals/ontology/r2.0.0#hasResourceDependencies>\r\n" + 
            "                <http://darpa.mil/immortals/ontology/r2.0.0/resources/gps#GpsReceiver> .\r\n" + 
            "";

    public static final String expectedTriple =
            "[[http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#DfuInstance-35ca4d39-5f63-4651-8a0c-d3512cb66a86]" +
                    " [http://darpa.mil/immortals/ontology/r2.0.0#hasResourceDependencies]" +
                    " [http://darpa.mil/immortals/ontology/r2.0.0/resources/gps#GpsReceiver]\n]";

}
