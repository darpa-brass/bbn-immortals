package mySoot;


import java.io.*;
import java.util.*;

import mySoot.bbnAnalysisTools.DFUResourceAnalysisTransformer;
import mySoot.bbnAnalysisTools.SQLQueryIdentification;
import soot.*;


public class TransformerInvokers {
	
	// Yue: added for BBN project
	public static void dfuResourceAnalysis()
	{
		soot.options.Options.v().set_whole_program(true);
		Scene.v().setSootClassPath(AnalyzerMain.CLASSPATH + ":" + getLibs());				
		
		LinkedHashMap<String, String> mClassToMethod = AnalyzerMain.entryPoints;
		
		List<SootMethod> entry_points = new ArrayList<SootMethod>();
		
		Set<String> keySet = mClassToMethod.keySet();
		Iterator<String> keyIterator = keySet.iterator();
		while (keyIterator.hasNext()) {

			String mClass = keyIterator.next();
			
			String method = mClassToMethod.get(mClass);
			
			if(method == null)
				continue;

			System.out.println("building entry points:" + mClass + "|" + method);
			
			mClass = mClass.substring(0, mClass.indexOf("|"));
			
			SootClass main_soot_class = Scene.v().loadClassAndSupport(mClass);
			SootMethod sMethod = main_soot_class.getMethod(method);
			sMethod.setDeclaringClass(main_soot_class);

			System.out.println("entry point:" + method);

			entry_points.add(sMethod);
		}		
		
		Scene.v().setEntryPoints(entry_points);

		AnalyzerMain.dfuResourceAnalysisTransformer = new DFUResourceAnalysisTransformer();
		Transform transform = new Transform("wjtp.DFUResourceAnalysisTransformer", AnalyzerMain.dfuResourceAnalysisTransformer);
		PackManager.v().getPack("wjtp").add(transform);
		
		List<String> sootArgs = new ArrayList<String>();
		
		sootArgs.add("-output-format");
		sootArgs.add("J");
		
		sootArgs.add("-soot-class-path");
		sootArgs.add(AnalyzerMain.CLASSPATH + ":" + getLibs());
				
		sootArgs.add("-output-dir");
		sootArgs.add(AnalyzerMain.OUTPUT);


		sootArgs.add("-process-dir");
		sootArgs.add(AnalyzerMain.CLASSPATH);
		
		String[] soot_args = new String[sootArgs.size()];
		for(int i=0;i<sootArgs.size();i++){
			soot_args[i] = sootArgs.get(i);
		}

		soot.Main.main(soot_args);
	}
	
	//Yue: added for BBN
	public static void sqlResourceIdentification()
	{
		soot.options.Options.v().set_whole_program(true);
		Scene.v().setSootClassPath(AnalyzerMain.CLASSPATH + ":" + getLibs());				
		
		LinkedHashMap<String, String> mClassToMethod = AnalyzerMain.entryPoints;
		
		List<SootMethod> entry_points = new ArrayList<SootMethod>();
		
		Set<String> keySet = mClassToMethod.keySet();
		Iterator<String> keyIterator = keySet.iterator();
		while (keyIterator.hasNext()) {

			String mClass = keyIterator.next();
			
			String method = mClassToMethod.get(mClass);
			
			if(method == null)
				continue;

			System.out.println("building entry points:" + mClass + "|" + method);
			
			mClass = mClass.substring(0, mClass.indexOf("|"));
			
			SootClass main_soot_class = Scene.v().loadClassAndSupport(mClass);
			SootMethod sMethod = main_soot_class.getMethod(method);
			sMethod.setDeclaringClass(main_soot_class);

			System.out.println("entry point:" + method);

			entry_points.add(sMethod);
		}
		
		Scene.v().setEntryPoints(entry_points);

		//dfuResourceAnalysisTransformer = new DFUResourceAnalysisTransformer();
		//Transform transform = new Transform("wjtp.DFUResourceAnalysisTransformer", dfuResourceAnalysisTransformer);
		//PackManager.v().getPack("wjtp").add(transform);
		
		SQLQueryIdentification SQLQueryIdentification = new SQLQueryIdentification();
		Transform transform = new Transform("wjtp.SQLQueryIdentification", SQLQueryIdentification);
		PackManager.v().getPack("wjtp").add(transform);
	
		
		List<String> sootArgs = new ArrayList<String>();
		
		sootArgs.add("-output-format");
		sootArgs.add("J");
		
		sootArgs.add("-soot-class-path");
		sootArgs.add(AnalyzerMain.CLASSPATH + ":" + getLibs());
				
		sootArgs.add("-output-dir");
		sootArgs.add(AnalyzerMain.OUTPUT);


		sootArgs.add("-process-dir");
		sootArgs.add(AnalyzerMain.CLASSPATH);
		
		String[] soot_args = new String[sootArgs.size()];
		for(int i=0;i<sootArgs.size();i++){
			soot_args[i] = sootArgs.get(i);
		}

		soot.Main.main(soot_args);
	}

	public static void locateSinks() {

		AnalyzerMain.flowSinkTransformer = new FlowSinkTransformer();
		Transform transform1 = new Transform("jtp.FlowSinkTransformer", AnalyzerMain.flowSinkTransformer);
		PackManager.v().getPack("jtp").add(transform1);

		
		List<String> sootArgs = new ArrayList<String>();
		sootArgs.add("-output-format");
		sootArgs.add("J");
		
		sootArgs.add("-soot-class-path");
		sootArgs.add(AnalyzerMain.CLASSPATH + ":" + getLibs());
				
		sootArgs.add("-output-dir");
		sootArgs.add(AnalyzerMain.OUTPUT);		

		if(MyConstants.isTrickOn){
			loadClassExceptFromPackage(sootArgs);
		}else{
			sootArgs.add("-process-dir");
			sootArgs.add(AnalyzerMain.CLASSPATH);
		}
		
		String[] soot_args = new String[sootArgs.size()];
		for(int i=0;i<sootArgs.size();i++){
			soot_args[i] = sootArgs.get(i);
		}
		soot.Main.main(soot_args);
	}
	
	
	public static void simpleOptimization(){
		List<String> sootArgs = new ArrayList<String>();
		
		sootArgs.add("-output-format");
		sootArgs.add("c");
		
		sootArgs.add("-soot-class-path");
		sootArgs.add(AnalyzerMain.CLASSPATH + ":" + getLibs());
		
		sootArgs.add("-output-dir");
		sootArgs.add(AnalyzerMain.CLASSPATH);

		if(MyConstants.isTrickOn){
			loadClassExceptFromPackage(sootArgs);
		}else{
			sootArgs.add("-process-dir");
			sootArgs.add(AnalyzerMain.CLASSPATH);
		}
		
		sootArgs.add("-O");
		
		String[] soot_args = new String[sootArgs.size()];
		for(int i=0;i<sootArgs.size();i++){
			soot_args[i] = sootArgs.get(i);
		}

		soot.Main.main(soot_args);
	}
	
	
	public static void doGlobalBackwardDataflowAnalysis(){		
		
		//soot.options.Options.v().set_keep_line_number(true);
		soot.options.Options.v().set_whole_program(true);
		PhaseOptions.v().setPhaseOption("tag.ln", "on");
		PhaseOptions.v().setPhaseOption("cg.spark","ignore-types:true");
		
		Scene.v().setSootClassPath(AnalyzerMain.CLASSPATH + ":" + getLibs());
		
	
		LinkedHashMap<String, String> mClassToMethod = AnalyzerMain.entryPoints;
		
		List<SootMethod> entry_points = new ArrayList<SootMethod>();
		
		Set<String> keySet = mClassToMethod.keySet();
		Iterator<String> keyIterator = keySet.iterator();
		while (keyIterator.hasNext()) {
		
			String mClass = keyIterator.next();
			String method = mClassToMethod.get(mClass);
		
			System.out.println("building entry points:" + mClass + "|" + method);
			
			mClass = mClass.substring(0, mClass.indexOf("|"));
			
			SootClass main_soot_class = Scene.v().loadClassAndSupport(mClass);
			SootMethod sMethod = main_soot_class.getMethod(method);
			sMethod.setDeclaringClass(main_soot_class);
		
			//System.out.println("entry point:" + method);
		
			entry_points.add(sMethod);
		}
				
		//System.out.println("setting entry points: " + entry_points);
		Scene.v().setEntryPoints(entry_points);

		AnalyzerMain.globalBackwardDataflowAnalysis = new GlobalBackwardDataflowAnalysis();
		Transform transform1 = new Transform("wjtp.GlobalBackwardDataflowAnalysis", AnalyzerMain.globalBackwardDataflowAnalysis);
		
		PackManager.v().getPack("wjtp").add(transform1);
		
		List<String> sootArgs = new ArrayList<String>();
		
		sootArgs.add("-output-format");
		sootArgs.add("J");
		
		sootArgs.add("-soot-class-path");
		sootArgs.add(AnalyzerMain.CLASSPATH + ":" + getLibs());
				
		sootArgs.add("-output-dir");
		sootArgs.add(AnalyzerMain.OUTPUT);		

		if(MyConstants.isTrickOn){
			loadClassExceptFromPackage(sootArgs);
		}else{
			sootArgs.add("-process-dir");
			sootArgs.add(AnalyzerMain.CLASSPATH);
		}
		
		sootArgs.add("-w");
//		sootArgs.add("-p");
//		sootArgs.add("cg.spark");
//		sootArgs.add("enabled");
		
		String[] soot_args = new String[sootArgs.size()];
		for(int i=0;i<sootArgs.size();i++){
			soot_args[i] = sootArgs.get(i);
		}

		soot.Main.main(soot_args);
	}
	
	
//	private void doGlobalForwardDataflowAnalysis(){
//		
//		//soot.options.Options.v().set_whole_program(true);
//		
//		///
//		//soot.options.Options.v().set_keep_line_number(true);
//		soot.options.Options.v().set_whole_program(true);
//		PhaseOptions.v().setPhaseOption("tag.ln", "on");
//		PhaseOptions.v().setPhaseOption("cg.spark","ignore-types:true");
//		///
//		
//		Scene.v().setSootClassPath(CLASSPATH + ":" + AnalyzerMain.getLibs());
//				
//		
//		LinkedHashMap<String, String> mClassToMethod = findUncalledOverridingMethodsTransformer.getClassToMethod();
//		
//		List<SootMethod> entry_points = new ArrayList<SootMethod>();
//		
//		Set<String> keySet = mClassToMethod.keySet();
//		Iterator<String> keyIterator = keySet.iterator();
//		while (keyIterator.hasNext()) {
//
//			String mClass = keyIterator.next();
//			String method = mClassToMethod.get(mClass);
//
//			System.out.println("building entry points:" + mClass + "|" + method);
//			
//			mClass = mClass.substring(0, mClass.indexOf("|"));
//			
//			SootClass main_soot_class = Scene.v().loadClassAndSupport(mClass);
//			SootMethod sMethod = main_soot_class.getMethod(method);
//			sMethod.setDeclaringClass(main_soot_class);
//
//			System.out.println("entry point:" + method);
//
//			entry_points.add(sMethod);
//		}		
//				
//		//System.out.println("setting entry points: " + entry_points);
//		Scene.v().setEntryPoints(entry_points);
//
//		globalForwardDataflowAnalysis = new GlobalForwardDataflowAnalysis();
//		Transform transform1 = new Transform("wjtp.GlobalForwardDataflowAnalysis", globalForwardDataflowAnalysis);
//		
//		PackManager.v().getPack("wjtp").add(transform1);
//		
//		List<String> sootArgs = new ArrayList<String>();
//		
//		sootArgs.add("-output-format");
//		sootArgs.add("J");
//		
//		sootArgs.add("-soot-class-path");
//		sootArgs.add(CLASSPATH + ":" + AnalyzerMain.getLibs());
//				
//		sootArgs.add("-output-dir");
//		sootArgs.add(OUTPUT);		
//
//		if(MyConstants.isTrickOn){
//			loadClassExceptFromPackage(sootArgs);
//		}else{
//			sootArgs.add("-process-dir");
//			sootArgs.add(CLASSPATH);
//		}
//		
//		sootArgs.add("-w");
//		sootArgs.add("-p");
//		sootArgs.add("cg.spark");
//		sootArgs.add("enabled");
//		
//		String[] soot_args = new String[sootArgs.size()];
//		for(int i=0;i<sootArgs.size();i++){
//			soot_args[i] = sootArgs.get(i);
//		}
//
//		soot.Main.main(soot_args);
//	}
	
	
	public static void buildGlobalAPISubGraph(){

		soot.options.Options.v().set_whole_program(true);
		Scene.v().setSootClassPath(AnalyzerMain.CLASSPATH + ":" + getLibs());
				
		
		LinkedHashMap<String, String> mClassToMethod = AnalyzerMain.entryPoints;
		List<SootMethod> entry_points = new ArrayList<SootMethod>();
		
		Set<String> keySet = mClassToMethod.keySet();
		Iterator<String> keyIterator = keySet.iterator();
		while (keyIterator.hasNext()) {

			String mClass = keyIterator.next();
			
			String method = mClassToMethod.get(mClass);

			System.out.println("building entry points:" + mClass + "|" + method);
			
			mClass = mClass.substring(0, mClass.indexOf("|"));
			
			SootClass main_soot_class = Scene.v().loadClassAndSupport(mClass);
			SootMethod sMethod = main_soot_class.getMethod(method);
			sMethod.setDeclaringClass(main_soot_class);

//			System.out.println("entry point:" + method);

			entry_points.add(sMethod);
		}
		//System.out.println("setting entry points: " + entry_points);
		Scene.v().setEntryPoints(entry_points);

		AnalyzerMain.globalAPISubGraphTransformer = new GlobalAPISubGraphTransformer();
		Transform transform1 = new Transform("wjtp.GlobalAPISubGraphTransformer", AnalyzerMain.globalAPISubGraphTransformer);
		PackManager.v().getPack("wjtp").add(transform1);
		
		List<String> sootArgs = new ArrayList<String>();
		
		sootArgs.add("-output-format");
		sootArgs.add("J");
		
		sootArgs.add("-soot-class-path");
		sootArgs.add(AnalyzerMain.CLASSPATH + ":" + getLibs());
				
		sootArgs.add("-output-dir");
		sootArgs.add(AnalyzerMain.OUTPUT);		

		if(MyConstants.isTrickOn){
			loadClassExceptFromPackage(sootArgs);
		}else{
			sootArgs.add("-process-dir");
			sootArgs.add(AnalyzerMain.CLASSPATH);
		}
		
		String[] soot_args = new String[sootArgs.size()];
		for(int i=0;i<sootArgs.size();i++){
			soot_args[i] = sootArgs.get(i);
		}

		soot.Main.main(soot_args);

	}
	
	
//	public static void findUncalledOverridingMethods(){
//		
//		AnalyzerMain.findUncalledOverridingMethodsTransformer = new FindUncalledOverridingMethodsTransformer(AnalyzerMain.appUncalledMethods);
//		Transform transform = new Transform("jtp.FindUncalledOverridingMethodsTransformer", AnalyzerMain.findUncalledOverridingMethodsTransformer);
//		PackManager.v().getPack("jtp").add(transform);
//		
//		List<String> sootArgs = new ArrayList<String>();
//		
//		sootArgs.add("-output-format");
//		sootArgs.add("J");
//		
//		sootArgs.add("-soot-class-path");
//		sootArgs.add(AnalyzerMain.CLASSPATH + ":" + AnalyzerMain.getLibs());
//		
//		sootArgs.add("-output-dir");
//		sootArgs.add(AnalyzerMain.OUTPUT);
//
//		if(MyConstants.isTrickOn){
//			loadClassExceptFromPackage(sootArgs);
//		}else{
//			sootArgs.add("-process-dir");
//			sootArgs.add(AnalyzerMain.CLASSPATH);
//		}
//		
//		sootArgs.add("-O");
//		
//		String[] soot_args = new String[sootArgs.size()];
//		for(int i=0;i<sootArgs.size();i++){
//			soot_args[i] = sootArgs.get(i);
//		}
//
//		soot.Main.main(soot_args);
//	}
//	
//	
	public static void findUncalledMethods(){
		
		AnalyzerMain.findUncalledMethodsTransformer = new FindUncalledMethodsTransformer();
		Transform transform = new Transform("jtp.FindUncalledMethodsTransformer", AnalyzerMain.findUncalledMethodsTransformer);
		PackManager.v().getPack("jtp").add(transform);
		
		List<String> sootArgs = new ArrayList<String>();
		
		sootArgs.add("-output-format");
		sootArgs.add("J");
		
		sootArgs.add("-soot-class-path");
		sootArgs.add(AnalyzerMain.CLASSPATH + ":" + getLibs());
		
		sootArgs.add("-output-dir");
		sootArgs.add(AnalyzerMain.OUTPUT);

		if(MyConstants.isTrickOn){
			loadClassExceptFromPackage(sootArgs);
		}else{
			sootArgs.add("-process-dir");
			sootArgs.add(AnalyzerMain.CLASSPATH);
		}
		
		//sootArgs.add("-allow-phantom-refs");
		//sootArgs.add("-src-prec");
		//sootArgs.add("java");
		
		String[] soot_args = new String[sootArgs.size()];
		for(int i=0;i<sootArgs.size();i++){
			soot_args[i] = sootArgs.get(i);
		}

		soot.Main.main(soot_args);
	}
	
	

//	@SuppressWarnings("unused")
//	private void buildAPISubGraph() {
//		apiSubGraphTransformer = new APISubGraphTransformer();
//		Transform transform = new Transform("jtp.APISubGraphTransformer", apiSubGraphTransformer);
//		PackManager.v().getPack("jtp").add(transform);
//				
//		List<String> sootArgs = new ArrayList<String>();
//		
//		sootArgs.add("-output-format");
//		sootArgs.add("J");
//		
//		sootArgs.add("-soot-class-path");
//		sootArgs.add(CLASSPATH + ":" + AnalyzerMain.getLibs());
//		
//		sootArgs.add("-output-dir");
//		sootArgs.add(OUTPUT);
//
//		if(MyConstants.isTrickOn){
//			loadClassExceptFromPackage(sootArgs);
//		}else{
//			sootArgs.add("-process-dir");
//			sootArgs.add(CLASSPATH);
//		}
//		
//		String[] soot_args = new String[sootArgs.size()];
//		for(int i=0;i<sootArgs.size();i++){
//			soot_args[i] = sootArgs.get(i);
//		}
//
//		soot.Main.main(soot_args);
//	}
	
	
	
//	private void findFiles(String sDir, List<File> fileList){
//		File[] faFiles = new File(sDir).listFiles();
//		for(File file: faFiles){
//			//if(file.getName().matches("^(.*?)")){
//			if(file.getName().contains(".class")){
//				//System.out.println(file.getAbsolutePath());
//				fileList.add(file);
//			}
//			if(file.isDirectory()){
//				findFiles(file.getAbsolutePath(), fileList);
//			}
//		}
//	}
	
	public static void findFilesExcept(String sDir, List<File> fileList, List<String> exceptions){
		File[] faFiles = new File(sDir).listFiles();
		for(File file: faFiles){
			//if(file.getName().matches("^(.*?)")){
			if(file.getName().contains(".class")){
				//System.err.println(file.getAbsolutePath());
				String absPath = file.getAbsolutePath();
				String relativePath = absPath.substring(AnalyzerMain.CLASSPATH.length());
				if(!exceptions.contains(relativePath)){
					
					//System.err.println(relativePath);
					fileList.add(file);
				}else{
					//System.err.println(relativePath);
				}
			}
			if(file.isDirectory()){
				findFilesExcept(file.getAbsolutePath(), fileList, exceptions);
			}
		}
	}
	
	
	public static void doPointsToAnalysis(){
		
		soot.options.Options.v().set_keep_line_number(true);
		soot.options.Options.v().set_whole_program(true);
		PhaseOptions.v().setPhaseOption("tag.ln", "on");
		PhaseOptions.v().setPhaseOption("cg.spark","ignore-types:true");
		/*
		PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");
		PhaseOptions.v().setPhaseOption("bb.lp", "enabled:false");
		//PhaseOptions.v().setPhaseOption("bb.lp", "unsplit-original-locals:true");
		*/
		/*
		PhaseOptions.v().setPhaseOption("jb.a","enabled:false");
		PhaseOptions.v().setPhaseOption("jb.ule","enabled:false");
		PhaseOptions.v().setPhaseOption("jb.cp-ule","enabled:false");
		PhaseOptions.v().setPhaseOption("jb.ne","enabled:false");
		PhaseOptions.v().setPhaseOption("jb.uce","enabled:false");
		PhaseOptions.v().setPhaseOption("jj.a","enabled:false");
		PhaseOptions.v().setPhaseOption("jj.ule","enabled:false");
		PhaseOptions.v().setPhaseOption("jj.cp-ule","enabled:false");
		PhaseOptions.v().setPhaseOption("jj.ne","enabled:false");
		PhaseOptions.v().setPhaseOption("jj.uce","enabled:false");
		PhaseOptions.v().setPhaseOption("sop.cpf","enabled:false");
		PhaseOptions.v().setPhaseOption("jop.uce1","enabled:false");
		PhaseOptions.v().setPhaseOption("jop.ubf1","enabled:false");
		PhaseOptions.v().setPhaseOption("jop.uce2","enabled:false");
		PhaseOptions.v().setPhaseOption("jop.ubf2","enabled:false");
		PhaseOptions.v().setPhaseOption("jop.ule","enabled:false");
		PhaseOptions.v().setPhaseOption("gb.ule","enabled:false");
		
		//PhaseOptions.v().setPhaseOption("bb.lso","enabled:false");
		PhaseOptions.v().setPhaseOption("bb.ule","enabled:false");
		*/
		/*
		soot.options.Options.v().set_keep_line_number(true);
		soot.options.Options.v().set_whole_program(true);
		Scene.v().setSootClassPath(CLASSPATH + ":" + ANDROID);

		
		//soot.options.Options.v().setPhaseOption("cg","implicit-entry:false");
		soot.options.Options.v().setPhaseOption("cg.spark","enabled:true");
		//soot.options.Options.v().setPhaseOption("cg.spark","ignore-types:true");
		//soot.options.Options.v().setPhaseOption("cg.spark","field-based:false");
		//soot.options.Options.v().setPhaseOption("cg.spark","types-for-sites:false");
		//soot.options.Options.v().setPhaseOption("cg.spark","vta:true");	
		soot.options.Options.v().setPhaseOption("cg.spark","on-fly-cg:false");
		soot.options.Options.v().setPhaseOption("cg.spark","set-impl:hash");
		//soot.options.Options.v().setPhaseOption("cg.spark","double-set-old:hybrid");
		//soot.options.Options.v().setPhaseOption("cg.spark","double-set-new:hybrid");
		soot.options.Options.v().setPhaseOption("cg.spark","verbose:true");	
		soot.options.Options.v().setPhaseOption("cg.spark","on-fly-cg:true");
		soot.options.Options.v().setPhaseOption("cg.spark","propagator:worklist");
		soot.options.Options.v().setPhaseOption("cg.spark","dump-solution:true");
		*/

		//LinkedHashMap<String, String> mClassToMethod = FindEntryMethodsTransformer.getClassToMethod();
		
		/*

		List<SootMethod> entry_points = new ArrayList<SootMethod>();

		Set<String> keySet = mClassToMethod.keySet();
		Iterator<String> keyIterator = keySet.iterator();
		while (keyIterator.hasNext()) {

			String mClass = keyIterator.next();
			String method = mClassToMethod.get(mClass);

			//System.out.println("building entry points:" + mClass + "|" + method);

			mClass = mClass.substring(0, mClass.indexOf("|"));

			SootClass main_soot_class = Scene.v().loadClassAndSupport(mClass);
			SootMethod sMethod = main_soot_class.getMethod(method);
			sMethod.setDeclaringClass(main_soot_class);

			//System.out.println("entry point:" + method);

			entry_points.add(sMethod);
		}		
		
		//System.out.println("setting entry points: " + entry_points);
		Scene.v().setEntryPoints(entry_points);
		*/
		
		/*
		List<SootMethod> entry_points = new ArrayList<SootMethod>();
		Scene.v().loadBasicClasses();
		SootClass main_soot_class =  Scene.v().loadClassAndSupport("Leakage");
		SootMethod sMethod = main_soot_class.getMethod("void main(java.lang.String[])");
		entry_points.add(sMethod);
		Scene.v().setEntryPoints(entry_points);
		*/
		
		AnalyzerMain.pointsToAnalysisTransformer = new PointsToAnalysisTransformer();
		Transform transform1 = new Transform("wjtp.TestSceneTransformer", AnalyzerMain.pointsToAnalysisTransformer);
		
		PackManager.v().getPack("wjtp").add(transform1);
		
		List<String> sootArgs = new ArrayList<String>();
		
		sootArgs.add("-output-format");
		sootArgs.add("J");
		
		sootArgs.add("-soot-class-path");
		sootArgs.add(AnalyzerMain.CLASSPATH + ":" + getLibs());
				
		sootArgs.add("-output-dir");
		sootArgs.add(AnalyzerMain.OUTPUT);		

		if(MyConstants.isTrickOn){
			loadClassExceptFromPackage(sootArgs);
		}else{
			sootArgs.add("-process-dir");
			sootArgs.add(AnalyzerMain.CLASSPATH);
		}
					
		sootArgs.add("-w");
		sootArgs.add("-p");
		sootArgs.add("cg.spark");
		sootArgs.add("enabled");		
		
		String[] soot_args = new String[sootArgs.size()];
		for(int i=0;i<sootArgs.size();i++){
			soot_args[i] = sootArgs.get(i);
		}

		soot.Main.main(soot_args);
	}
	

//	private void doGlobalForwardDataflowAnalysis(){
//		
//		//soot.options.Options.v().set_whole_program(true);
//		
//		///
//		//soot.options.Options.v().set_keep_line_number(true);
//		soot.options.Options.v().set_whole_program(true);
//		PhaseOptions.v().setPhaseOption("tag.ln", "on");
//		PhaseOptions.v().setPhaseOption("cg.spark","ignore-types:true");
//		///
//		
//		Scene.v().setSootClassPath(CLASSPATH + ":" + AnalyzerMain.getLibs());
//				
//		
//		LinkedHashMap<String, String> mClassToMethod = findUncalledOverridingMethodsTransformer.getClassToMethod();
//		
//		List<SootMethod> entry_points = new ArrayList<SootMethod>();
//		
//		Set<String> keySet = mClassToMethod.keySet();
//		Iterator<String> keyIterator = keySet.iterator();
//		while (keyIterator.hasNext()) {
//
//			String mClass = keyIterator.next();
//			String method = mClassToMethod.get(mClass);
//
//			System.out.println("building entry points:" + mClass + "|" + method);
//			
//			mClass = mClass.substring(0, mClass.indexOf("|"));
//			
//			SootClass main_soot_class = Scene.v().loadClassAndSupport(mClass);
//			SootMethod sMethod = main_soot_class.getMethod(method);
//			sMethod.setDeclaringClass(main_soot_class);
//
//			System.out.println("entry point:" + method);
//
//			entry_points.add(sMethod);
//		}		
//				
//		//System.out.println("setting entry points: " + entry_points);
//		Scene.v().setEntryPoints(entry_points);
//
//		globalForwardDataflowAnalysis = new GlobalForwardDataflowAnalysis();
//		Transform transform1 = new Transform("wjtp.GlobalForwardDataflowAnalysis", globalForwardDataflowAnalysis);
//		
//		PackManager.v().getPack("wjtp").add(transform1);
//		
//		List<String> sootArgs = new ArrayList<String>();
//		
//		sootArgs.add("-output-format");
//		sootArgs.add("J");
//		
//		sootArgs.add("-soot-class-path");
//		sootArgs.add(CLASSPATH + ":" + AnalyzerMain.getLibs());
//				
//		sootArgs.add("-output-dir");
//		sootArgs.add(OUTPUT);		
//
//		if(MyConstants.isTrickOn){
//			loadClassExceptFromPackage(sootArgs);
//		}else{
//			sootArgs.add("-process-dir");
//			sootArgs.add(CLASSPATH);
//		}
//		
//		sootArgs.add("-w");
//		sootArgs.add("-p");
//		sootArgs.add("cg.spark");
//		sootArgs.add("enabled");
//		
//		String[] soot_args = new String[sootArgs.size()];
//		for(int i=0;i<sootArgs.size();i++){
//			soot_args[i] = sootArgs.get(i);
//		}
//
//		soot.Main.main(soot_args);
//	}
	
	
	private static void loadClassExceptFromPackage(List<String> sootArgs){
		List<File> fileList = new ArrayList<File>();
		/*
		List<String> exceptions = new ArrayList<String>();
		for(int i=0;i<MyConstants.EXCLUDE_PROCESS_PACKAGES.length;i++){
			exceptions.add(MyConstants.EXCLUDE_PROCESS_PACKAGES[i]);
		}
				
		findFilesExcept(CLASSPATH, fileList, exceptions);
		*/
		findFilesExcept(AnalyzerMain.CLASSPATH, fileList, AnalyzerMain.unloadedClasses);
		
		//System.out.println("Selectively loading classes:");
		
		for(File file : fileList){
			//System.out.println(file.getName());
			String absoluteName = file.getAbsolutePath();
			String relativeName = absoluteName.substring(
					absoluteName.indexOf(AnalyzerMain.CLASSPATH) + AnalyzerMain.CLASSPATH.length(), absoluteName.length());
			String className = relativeName.substring(0, relativeName.indexOf(".class"));
			className = className.replace("/", ".");
			while(className.startsWith(".")){
				className = className.substring(1);
			}
			//System.out.println("xjtu clname:" + className);
			sootArgs.add(className);
		}
	}
	
	
	private static String getLibs() {
		String third_party_libs = "";
		
//		File[] files = new File(DROPBOX).listFiles();
//	    for(File dir: files)
//	    {
//	    	if(dir.isDirectory())
//	    	{
//	    		third_party_libs += ":" + dir.getAbsolutePath() + "/classes.jar";
//	    	}
//	    }
		
		
		File[] files = new File(AnalyzerMain.THIRD_PARTY_LIBS).listFiles();
	    for(File jar: files)
	    {
	    	if(jar.getName().endsWith(".jar"))
	    		third_party_libs += ":" + jar.getAbsolutePath();

	    }
	    		
	    //return AnalyzerMain.ANDROID_24 + ":" + third_party_libs + ":" + AnalyzerMain.RT;
	    return AnalyzerMain.ANDROID_24 + ":" + AnalyzerMain.RT + third_party_libs;
	}
}
