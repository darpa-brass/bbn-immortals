package mySoot.bbnAnalysisTools;


import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mySoot.AnalyzerMain;

public class ResourceUsageAPIs{

	public static List<String> DFUs = new ArrayList<String>();
	public static List<String> dataFlow_source = new ArrayList<String>();
	private static List<String> callgraph_sink = new ArrayList<String>();
	
	public static Pair<LinkedList<String>, LinkedList<String>> partialCallGraphPoints = 
			new Pair<LinkedList<String>, LinkedList<String>>(new LinkedList<String>(), new LinkedList<String>());
	
	
	public static boolean isDataFlowSource(String signature){
		if(dataFlow_source.contains(signature)){
			return true;
		}
		return false;
	}
	
	public static boolean isCallGraphSink(String signature){
		if(callgraph_sink.contains(signature)){
			return true;
		}
		return false;
	}
	
	public static void setup()
	{
		SetSourcesAndSinks();
		SetDFUs();
		SetPartialCallGraphInfo();
	}
	
	private static void SetSourcesAndSinks()
	{
		callgraph_sink.add("<java.io.FileOutputStream: void write(byte[])>");
	}
	
	
	private static void getFileNames(List<String> fileNames, Path dir) 
	{
	    try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) 
	    {
	        for (Path path : stream) 
	        {
	            if(path.toFile().isDirectory()) 
	            {
	                getFileNames(fileNames, path);
	            } 
	            else
	            {
	                fileNames.add(path.toAbsolutePath().toString());
	                //System.out.println(path.getFileName());
	            }
	        }
	    } 
	    catch(IOException e) 
	    {
	    	System.err.println("error occurred during \"getFileNames\":" + e.getMessage());
	    }
	    //return fileNames;
	}
	
	
	
	// extract annotation from each class file and check if the class is DFU
	private static void SetDFUs()
	{
		ArrayList<String> fileNames = new ArrayList<String>();
		getFileNames(fileNames, Paths.get(AnalyzerMain.CLASSPATH));
		
		for(String filePath : fileNames)
		{
			String annotation = AnnotationReader.ExtractAnnotation(filePath);
			if(annotation != null && annotation.contains("DfuAnnotation"))
			{
				String file = filePath.substring(AnalyzerMain.CLASSPATH.length());
				file = file.replace("/", ".");
				file = file.replace(".class", "");
				System.err.println("Discovered DFU: " + file);
				DFUs.add(file);
			}
		}
	}
	
	
	private static void SetPartialCallGraphInfo()
	{
		LinkedList<String> startingPoints = partialCallGraphPoints.getFirst();
		LinkedList<String> endingPoints = partialCallGraphPoints.getSecond();
		
		//Starting points:
		//CP1
		/*
			mil.darpa.immortals.dfus.location.LocationProviderAndroidGpsBuiltIn.initialize(...)
			mil.darpa.immortals.dfus.location.LocationProviderAndroidGpsBuiltIn.getLastKnownLocation()
			
			mil.darpa.immortals.dfus.location.LocationProviderBluetoothGpsSimulated.initialize(...)
			mil.darpa.immortals.dfus.location.LocationProviderBluetoothGpsSimulated.getCurrentLocation()
			
			mil.darpa.immortals.dfus.location.LocationProviderManualSimulated.initialize(...)
			mil.darpa.immortals.dfus.location.LocationProviderManualSimulated.getTrustedLocation()
			
			mil.darpa.immortals.dfus.location.LocationProviderUsbGpsSimulated.initialize(...)
			mil.darpa.immortals.dfus.location.LocationProviderUsbGpsSimulated.getLastKnownLocation()
		 */
		startingPoints.push("<mil.darpa.immortals.dfus.location.LocationProviderAndroidGpsBuiltIn: void initialize(android.content.Context)>");
		startingPoints.push("<mil.darpa.immortals.dfus.location.LocationProviderAndroidGpsBuiltIn: mil.darpa.immortals.datatypes.Coordinates getLastKnownLocation()>");
		
		startingPoints.push("<mil.darpa.immortals.dfus.location.LocationProviderBluetoothGpsSimulated: void initialize(android.content.Context)>");
		startingPoints.push("<mil.darpa.immortals.dfus.location.LocationProviderBluetoothGpsSimulated: mil.darpa.immortals.datatypes.Coordinates getCurrentLocation()>");
		
//		startingPoints.push("");
//		startingPoints.push("");
//		
//		startingPoints.push("");
//		startingPoints.push("");
		
		//CP2
		/*
			mil.darpa.immortals.dfus.images.BitmapReader.consume(...)
		 */
		startingPoints.push("<mil.darpa.immortals.dfus.images.BitmapReader: void consume(java.lang.Object)>");
		
		//Misc
		/*
			mil.darpa.immortals.dfus.LatestSaFileByteWriter.write(...)
			mil.darpa.immortals.dfus.LatestSaFileByteReader.read(...)
		 */
//		startingPoints.push("");
//		startingPoints.push("");
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		//Ending Points:
		//Network
		/*
			com.bbn.ataklite.net.produce()
			com.bbn.ataklite.net.Dispatcher.consume(...)
			java.net.SocketInputStream.read(...)
			java.net.SocketOutputStream.write(...)
		 */
		endingPoints.push("<com.bbn.ataklite.net.Dispatcher: void consume(java.lang.Object)>");
//		endingPoints.push("");
//		endingPoints.push("");
//		endingPoints.push("");
		
		//Misc
		/*
			android.bluetooth.BluetoothAdapter.getDefaultAdapter()
			android.location.LocationManager.requestLocationUpdates(....)
			android.hardware.usb.UsbManager.getDeviceList()
			android.hardware.usb.UsbManager.getAccessoryList()
		 */
		endingPoints.push("<android.bluetooth.BluetoothAdapter: android.bluetooth.BluetoothAdapter getDefaultAdapter()>");
		endingPoints.push("<android.location.LocationManager: void requestLocationUpdates(java.lang.String,long,float,android.location.LocationListener)>");
		endingPoints.push("<android.hardware.usb.UsbManager: java.util.HashMap getDeviceList()>");
		//endingPoints.push("");
		
		
		//File
		/*
			java.io.FileOutputStream.write(...)
			java.io.FileOutputStream.read(...) 
		 */
		endingPoints.push("<java.io.FileOutputStream: void write(byte[])>");
//		endingPoints.push("");
	}
}
