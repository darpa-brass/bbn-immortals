package mySoot.bbnAnalysisTools;

import java.util.*;
import soot.*;

public class ClassHierarchyAnalysisTransformer extends BodyTransformer
{
	// This data structure holds the class information along with all its methods. We use this information for DFU function identification.
	private LinkedHashMap<SootClass, LinkedList<SootMethod>> classToMethodMapping = 
			new LinkedHashMap<SootClass, LinkedList<SootMethod>>();
	
	@Override
	protected void internalTransform(Body body, String string, @SuppressWarnings("rawtypes") Map map) 
	{
		SootMethod method = body.getMethod();
		SootClass clazz = method.getDeclaringClass();
		
		if(classToMethodMapping.containsKey(clazz))
		{
			LinkedList<SootMethod> list = classToMethodMapping.get(clazz);
			list.add(method);
		}
		else
		{
			LinkedList<SootMethod> list = new LinkedList<SootMethod>();
			list.add(method);
			classToMethodMapping.put(clazz, list);
		}
		
		//System.out.println("class : " + clazz.getName() + " has method: "+ method.getName());
	}
}