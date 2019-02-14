package mySoot;

import java.util.*;

import soot.*;
import soot.jimple.*;

public class FindUncalledMethodsTransformer extends BodyTransformer {
	
	private List<SootMethod> appMethods;
	private List<SootMethod> appCalledMethods;
		
	public FindUncalledMethodsTransformer() {
		appMethods = new ArrayList<SootMethod>();
		appCalledMethods = new ArrayList<SootMethod>();
	}	
	
	@Override
	protected void internalTransform(Body body, String string, @SuppressWarnings("rawtypes") Map map) {
		SootMethod method = body.getMethod();
		SootClass clazz = method.getDeclaringClass();
		if(!clazz.isApplicationClass()){
			return;
		}
		
		if(!appMethods.contains(method)){
			appMethods.add(method);
		}
		
		Iterator<Unit> iter = body.getUnits().iterator();
		while(iter.hasNext()){
			Stmt s = (Stmt)iter.next();
			
			if(s instanceof InvokeStmt){
				SootMethod m = s.getInvokeExpr().getMethod();
								
				SootClass c = m.getDeclaringClass();
				if(c.isApplicationClass()){
					if(!appCalledMethods.contains(m)){
						appCalledMethods.add(m);
					}
				}
			}else if(s instanceof DefinitionStmt){
				Value rhs = ((DefinitionStmt) s).getRightOp();
				if(rhs instanceof InvokeExpr){
					SootMethod m = s.getInvokeExpr().getMethod();
					
					SootClass c = m.getDeclaringClass();
					if(c.isApplicationClass()){
						if(!appCalledMethods.contains(m)) {
							appCalledMethods.add(m);
						}
					}
				}
			}
		}
	}
	
	public List<SootMethod> getAppMethods() {
		return appMethods;
	}
	
	public List<SootMethod> getCalledMethods() {
		return appCalledMethods;
	}

}