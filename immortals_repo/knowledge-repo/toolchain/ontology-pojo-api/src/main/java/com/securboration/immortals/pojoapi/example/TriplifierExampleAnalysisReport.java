package com.securboration.immortals.pojoapi.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.securboration.immortals.ontology.analysis.AnalysisReport;
import com.securboration.immortals.ontology.analysis.cg.CallGraphEdge;
import com.securboration.immortals.ontology.analysis.profiling.SimpleResourceDependencyAssertion;
import com.securboration.immortals.ontology.analysis.profiling.properties.DynamicBytesConsumed;
import com.securboration.immortals.ontology.analysis.profiling.properties.DynamicCallGraph;
import com.securboration.immortals.ontology.analysis.profiling.properties.DynamicInstructionCount;
import com.securboration.immortals.ontology.android.AndroidPlatform;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.measurement.CodeUnitPointer;
import com.securboration.immortals.ontology.measurement.MeasurementInstance;
import com.securboration.immortals.ontology.measurement.MeasurementProfile;
import com.securboration.immortals.ontology.resources.gps.GpsReceiver;
import com.securboration.immortals.pojoapi.Triplifier;

public class TriplifierExampleAnalysisReport {
    
    public static void main(String[] args) throws IOException{
        
        AnalysisReport analysisData = generateMockAnalysisData();
        
        Triplifier.serializeToTriples(
            analysisData, 
            new File("./target/mockdata/test.ttl").getAbsolutePath()
            );
        
    }
    
    private static class MockClassBeingAnalyzed{
        
        @SuppressWarnings("unused")
        private int function1(int x, int y, int z){return -1;}
        
        @SuppressWarnings("unused")
        private String function2(
                String arg1, 
                List<Map<Map<String,String>,Map<String,String>>> arg2
                ){return UUID.randomUUID().toString();}
        
    }
    
    private static class AnotherMockClassBeingAnalyzed{
        
        @SuppressWarnings("unused")
        private int function3(){return -1;}
        
        @SuppressWarnings("unused")
        private String function4(){return UUID.randomUUID().toString();}
        
    }
    
    private static SimpleResourceDependencyAssertion makeDependencyAssertion(
            final CodeUnitPointer subject,
            final Class<? extends Resource> dependency
            ){
        SimpleResourceDependencyAssertion d = new SimpleResourceDependencyAssertion();
        
        d.setCodeUnit(subject);
        d.setDependency(dependency);
        
        return d;
    }
    
    private static MeasurementInstance getInstructionCountMeasurement(
            final String qualifier, 
            final long count
            ){
        MeasurementInstance measurement = new MeasurementInstance();
        
        DynamicInstructionCount d = new DynamicInstructionCount();
        d.setNumberOfDynamicInstructionsExecuted(count);
        
        measurement.setMeasuredValue(d);
        measurement.setQualifier(qualifier);
        
        return measurement;
    }
    
    private static MeasurementInstance getMemoryUseMeasurement(
            final String qualifier, 
            final long bytesConsumed
            ){
        MeasurementInstance measurement = new MeasurementInstance();
        
        DynamicBytesConsumed d = new DynamicBytesConsumed();
        d.setNumberOfBytesConsumed(bytesConsumed);
        
        measurement.setMeasuredValue(d);
        measurement.setQualifier(qualifier);
        
        return measurement;
    }
    
    private static MeasurementProfile getMockMeasurements(
            final String runTag,
            final CodeUnitPointer c,
            final long numBytes,
            final long numInstructions
            ){
        MeasurementProfile profile = new MeasurementProfile();
        profile.setCodeUnit(c);
        profile.setMeasurement(
            new MeasurementInstance[]{
                    getMemoryUseMeasurement(runTag,numBytes),
                    getInstructionCountMeasurement(runTag,numInstructions),
            });
        
        return profile;
    }
    
    private static MeasurementProfile getCallGraphMeasurement(
            final String runTag, 
            final DynamicCallGraph g
            ){
        MeasurementProfile p = new MeasurementProfile();
        
        MeasurementInstance graphMeasurement = new MeasurementInstance();
        graphMeasurement.setMeasuredValue(g);
        graphMeasurement.setQualifier(runTag);
        
        p.setMeasurement(new MeasurementInstance[]{
                graphMeasurement
        });
        
        return p;
    }
    
    private static CallGraphEdge getEdge(CodeUnitPointer from, CodeUnitPointer to){
        CallGraphEdge edge = new CallGraphEdge();
        
        edge.setCalledMethod(to);
        edge.setOriginMethod(from);
        
        return edge;
    }
    
    private static AnalysisReport generateMockAnalysisData() throws IOException{
        
        AnalysisReport report = new AnalysisReport();
        
        final CodeUnitPointer f1 = 
                ExamplePointerHelper.getPointer(
                    MockClassBeingAnalyzed.class,
                    "function1"
                    );
        final CodeUnitPointer f2 = 
                ExamplePointerHelper.getPointer(
                    MockClassBeingAnalyzed.class,
                    "function2"
                    );
        final CodeUnitPointer f3 = 
                ExamplePointerHelper.getPointer(
                    AnotherMockClassBeingAnalyzed.class,
                    "function3"
                    );
        final CodeUnitPointer f4 = 
                ExamplePointerHelper.getPointer(
                    AnotherMockClassBeingAnalyzed.class,
                    "function4"
                    );
        
        List<SimpleResourceDependencyAssertion> dependencies = new ArrayList<>();
        List<MeasurementProfile> measurements = new ArrayList<>();
        
        //add some mock dependencies
        {
            dependencies.add(makeDependencyAssertion(f1,GpsReceiver.class));
            dependencies.add(makeDependencyAssertion(f2,AndroidPlatform.class));
        }
        
        //add some mock measurements 
        {
            //about instruction count/mem usage during test run #1
            {
                measurements.add(getMockMeasurements("run1",f1,100l,10000l));
                measurements.add(getMockMeasurements("run1",f2,10000l,10l));
            }
            
            //about instruction count/mem usage during test run #1
            {
                measurements.add(getMockMeasurements("run2",f1,100l,10000l));
                measurements.add(getMockMeasurements("run2",f2,10000l,10l));
            }
        }
        
        //add the call graph as a measurement
        {
            DynamicCallGraph g = new DynamicCallGraph();
            
            List<CallGraphEdge> edges = new ArrayList<>();
            
            {
                edges.add(getEdge(f1,f2));
                edges.add(getEdge(f2,f3));
                edges.add(getEdge(f3,f4));
                edges.add(getEdge(f4,f1));
            }
            
            g.setObservedInvocations(edges.toArray(new CallGraphEdge[]{}));
            
            measurements.add(getCallGraphMeasurement("run1",g));
        }
        
        report.setDiscoveredDependency(
            dependencies.toArray(new SimpleResourceDependencyAssertion[]{})
            );
        report.setMeasurementProfile(
            measurements.toArray(new MeasurementProfile[]{})
            );
        
        return report;
    }

}
