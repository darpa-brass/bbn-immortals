package mySoot.bbnAnalysisTools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.securboration.immortals.helpers.ImmortalsPointerHelper;
import com.securboration.immortals.ontology.analysis.AnalysisReport;
import com.securboration.immortals.ontology.analysis.cg.CallGraphEdge;
import com.securboration.immortals.ontology.analysis.profiling.SimpleResourceDependencyAssertion;
import com.securboration.immortals.ontology.analysis.profiling.properties.DynamicBytesConsumed;
import com.securboration.immortals.ontology.analysis.profiling.properties.DynamicCallGraph;
import com.securboration.immortals.ontology.analysis.profiling.properties.DynamicInstructionCount;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.measurement.CodeUnitPointer;
import com.securboration.immortals.ontology.measurement.MeasurementInstance;
import com.securboration.immortals.ontology.measurement.MeasurementProfile;

import com.securboration.immortals.ontology.resources.gps.GpsReceiver;
import com.securboration.immortals.ontology.resources.BluetoothResource;
import com.securboration.immortals.ontology.resources.UsbResource;

import com.securboration.immortals.pojoapi.Triplifier;

import mySoot.AnalyzerMain;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.Edge;

public class TripleGeneration {
	
	private static DFUResourceAnalysisTransformer dat = AnalyzerMain.dfuResourceAnalysisTransformer;
	
	public static void generateTriples() {
        try {
    		AnalysisReport analysisData = generateAnalysisData();
    		
    		//print dependencies
    		System.err.println("\ndependencies #: " + analysisData.getDiscoveredDependency().length);
    		for(SimpleResourceDependencyAssertion s: analysisData.getDiscoveredDependency())
    			System.err.println("dependencies: " + s.getDependency().getName());
    		
    		
    		//String tirpleFileName = "/home/yduan/yueduan/bbnAnalysis/" + AnalyzerMain.APPNAME + ".triples.ttl";//"/RESULTS/triples.ttl";
    		String tirpleFileName = "/RESULTS/" + AnalyzerMain.APPNAME + ".triples.ttl";
    		Triplifier.serializeToTriples(analysisData, new File(tirpleFileName).getAbsolutePath());
			
		} catch (IOException e) {
			System.err.println("error during triplifier: " + e.getMessage());
		}
	}

	// CodeUnitPointer list is generated in DFUResourceAnalysisTransformer
	private static AnalysisReport generateAnalysisData() throws IOException {
		AnalysisReport report = new AnalysisReport();
		
		List<SimpleResourceDependencyAssertion> dependencies = new ArrayList<>();
        List<MeasurementProfile> measurements = new ArrayList<>();
        
    	//add resource dependencies
        for(String sig : dat.methodToResourceMapping.keySet())
        {
        	CodeUnitPointer cup = dat.cupList.get(sig);
        	{
	        	LinkedList<String> resources = dat.methodToResourceMapping.get(sig);
	        	for(String res : resources) {
	        		if(res.contains("GPS")) {
	        			SimpleResourceDependencyAssertion srda = makeDependencyAssertion(cup, GpsReceiver.class);
	        			dependencies.add(srda);
	        		}
	        		else if(res.contains("Bluetooth")) {
	        			SimpleResourceDependencyAssertion srda = makeDependencyAssertion(cup, BluetoothResource.class);
	        			dependencies.add(srda);
	        		}
	        		else if(res.contains("USB")) {
	        			SimpleResourceDependencyAssertion srda = makeDependencyAssertion(cup, UsbResource.class);
	        			dependencies.add(srda);
	        		}
	        	} //end for(String res : resources)
        	}
        }//end for(String sig : dat.methodToResourceMapping.keySet())

        
    	//add measurements
        for(int i = 0; i < ResourceUsageAPIs.DFUs.size(); i++)
		{
			String dfu = ResourceUsageAPIs.DFUs.get(i);
			LinkedList<String> funcs = dat.prif.dfuToDfuFuncMapping.get(dfu);
			
			// output per function usages
			if(!funcs.isEmpty())
			{
				for(String func: funcs)
				{
					for(String sig : dat.cupList.keySet()) {
//						System.err.println("\nfunc : " + func);
//						System.err.println("sig : " + sig + "\n");

						if(func.contains(sig)) {
							CodeUnitPointer f1 = dat.cupList.get(sig);
							System.err.println("add measurements for : " + func);
		        			int[] nums = dat.prif.dynamicResults.get(func);
		        			measurements.add(getMeasurements("run1",f1, 
		        					nums[ParseConfigFiles.MEMORY_INDEX], nums[ParseConfigFiles.CPU_INDEX]));
						}
					}//end for cupList
				}//end for funcs
			}
        }//end for ResourceUsageAPIs.DFUs
        
        //add the call graph
        {
        	DynamicCallGraph g = new DynamicCallGraph();
        	List<CallGraphEdge> edges = new ArrayList<>();
        	
        	//add all the edges
        	{
        		for(Edge edge: dat.partialCallGraph) {
        			SootMethod src = (SootMethod) edge.getSrc();
        			SootMethod tgt = (SootMethod) edge.getTgt();
        			
        			SootClass srcClazz = src.getDeclaringClass();
        			SootClass tgtClazz = tgt.getDeclaringClass();
        			
            		String srcSig = srcClazz.getShortName() + "." + src.getName();
            		String tgtSig = tgtClazz.getShortName() + "." + tgt.getName();
            		
            		if(dat.cupList.containsKey(srcSig) && dat.cupList.containsKey(tgtSig)) {
	            		CodeUnitPointer f1 = dat.cupList.get(srcSig);
	            		CodeUnitPointer f2 = dat.cupList.get(tgtSig);
	            		edges.add(getEdge(f1,f2));
            		}
            		else
            			continue;
        		}
        	}
        	
        	g.setObservedInvocations(edges.toArray(new CallGraphEdge[]{}));
            measurements.add(getCallGraphMeasurement("run1",g));
        	
        }//finish call graph
		
        report.setDiscoveredDependency(dependencies.toArray(new SimpleResourceDependencyAssertion[]{}));
        report.setMeasurementProfile(measurements.toArray(new MeasurementProfile[]{}));
		return report;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * Functions for creating CodeUnitPointers
	 */
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	public static CodeUnitPointer getPointer(Class<?> c,String methodName) throws IOException{
		CodeUnitPointer p = new CodeUnitPointer();
		p.setClassName(c.getName());
        p.setMethodName(methodName);
        p.setPointerString(ImmortalsPointerHelper.pointerForMethod(getClassBytes(c), methodName, (String[])null));
        return p;
    }
	
	
    private static byte[] getClassBytes(Class<?> c) throws IOException {
        final String thisClassName = 
                c.getName().replace(".", "/")+".class";
        
        final InputStream thisClassStream = 
                c.getClassLoader().getResourceAsStream(
                    thisClassName
                    );
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        IOUtils.copy(thisClassStream, os);
        
        return os.toByteArray();
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////


    
    
	///////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
     * Function for creating resource dependencies
     */
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
	private static SimpleResourceDependencyAssertion makeDependencyAssertion(final CodeUnitPointer subject,
			final Class<? extends Resource> dependency){
        SimpleResourceDependencyAssertion d = new SimpleResourceDependencyAssertion();
        d.setCodeUnit(subject);
        d.setDependency(dependency);
        return d;
    }
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * Functions for creating measurements
	 */
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	private static MeasurementProfile getMeasurements(final String runTag, final CodeUnitPointer c, 
			final long numBytes, final long numInstructions){
        MeasurementProfile profile = new MeasurementProfile();
        profile.setCodeUnit(c);
        profile.setMeasurement(new MeasurementInstance[]{getMemoryUseMeasurement(runTag,numBytes), getInstructionCountMeasurement(runTag,numInstructions),});
        
        return profile;
    }
	
    
    private static MeasurementInstance getInstructionCountMeasurement(final String qualifier, final long count){
        MeasurementInstance measurement = new MeasurementInstance();
        DynamicInstructionCount d = new DynamicInstructionCount();
        d.setNumberOfDynamicInstructionsExecuted(count);
        
        measurement.setMeasuredValue(d);
        measurement.setQualifier(qualifier);
        
        return measurement;
    }
    
    private static MeasurementInstance getMemoryUseMeasurement(final String qualifier, final long bytesConsumed){
        MeasurementInstance measurement = new MeasurementInstance();
        
        DynamicBytesConsumed d = new DynamicBytesConsumed();
        d.setNumberOfBytesConsumed(bytesConsumed);
        
        measurement.setMeasuredValue(d);
        measurement.setQualifier(qualifier);
        
        return measurement;
    }
	///////////////////////////////////////////////////////////////////////////////////////////////////////

    
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
     * Functions for creating call graph measurements
     */
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    private static MeasurementProfile getCallGraphMeasurement(final String runTag, final DynamicCallGraph g){
        MeasurementProfile p = new MeasurementProfile();
        
        MeasurementInstance graphMeasurement = new MeasurementInstance();
        graphMeasurement.setMeasuredValue(g);
        graphMeasurement.setQualifier(runTag);
        
        p.setMeasurement(new MeasurementInstance[]{graphMeasurement});
        
        return p;
    }
    
    private static CallGraphEdge getEdge(CodeUnitPointer from, CodeUnitPointer to){
    	CallGraphEdge edge = new CallGraphEdge();
        edge.setCalledMethod(to);
        edge.setOriginMethod(from);
        
        return edge;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
}
