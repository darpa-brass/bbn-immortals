package mySoot.bbnAnalysisTools;

import java.util.*;

import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;

public class SQLQueryIdentification extends SceneTransformer
{
	public CallGraph callgraph;
	public final String SQL_QUERY = "<java.sql.PreparedStatement: java.sql.ResultSet executeQuery()>";
	public static final LinkedHashMap<Stmt, SootMethod> sqlQueryStmtToMethodMapping = new LinkedHashMap<Stmt, SootMethod>();
	
	//@Override 
	protected void internalTransform(String arg0, @SuppressWarnings("rawtypes") Map arg1) {
		//this.callgraph = Scene.v().getCallGraph(); //this line was originally there.
		
		// go through each class to collect class and method mapping
		// In the meanwhile, find lowest level methods that call resource usage APIs
		
		Iterator<SootClass> itClass = Scene.v().getClasses().iterator();
		
		while(itClass.hasNext())
		{
			SootClass clazz = itClass.next();
			if(clazz.isPhantom() || !clazz.isApplicationClass() || clazz.isLibraryClass())
				continue;
			
			List<SootMethod> methods = clazz.getMethods();

			for(SootMethod method: methods)
			{
				if(!method.isConcrete() || (method.getSource() == null))
					continue;
				
//				if(!method.getSignature().contains("getAllForUserSinceTime"))
//					continue;
				
				Body b = method.retrieveActiveBody();
				// search statement that invokes SQL query
				// For every unit in the body, search invocations.
				Iterator<Unit> iter = b.getUnits().iterator();
				while(iter.hasNext())
				{
					Stmt s = (Stmt)iter.next();
					
					if(s.containsInvokeExpr()) {
						InvokeExpr ie = s.getInvokeExpr();
						String signature = ie.getMethod().getSignature();
						
						if (signature.equals(SQL_QUERY)) {
							//String sig = method.toString() + MyConstants.SIGNATURE_SEPERATOR + s.toString();
							sqlQueryStmtToMethodMapping.put(s, method);
						}
					}
				}//end while each statement
			}//end for each method
		}//end for each class
		System.err.println("\n*****Contain sql.PreparedStatement*****");
		for (Stmt sqlPrepStmt:sqlQueryStmtToMethodMapping.keySet()) {
			System.err.println("Method:  " + sqlQueryStmtToMethodMapping.get(sqlPrepStmt));
			System.err.println(sqlPrepStmt + "\n");
		}
		System.err.println("\n*****Contain sql.PreparedStatement*****\n\n");
	}
}