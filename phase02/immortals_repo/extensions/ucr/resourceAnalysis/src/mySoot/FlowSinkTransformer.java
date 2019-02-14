package mySoot;

import java.util.*;

import soot.jimple.*;
import soot.*;

public class FlowSinkTransformer extends BodyTransformer {
	
	private List<String> m_Classes;	
	private LinkedHashMap<String, String> mClassToMethod;
	public String sinkSig = "";

	public FlowSinkTransformer() {
		m_Classes = new ArrayList<String>();		
		mClassToMethod  = new LinkedHashMap<String, String>();
	}

	@Override
	protected void internalTransform(Body body, String string, @SuppressWarnings("rawtypes") Map map) {
		
		SootMethod method = body.getMethod();

		Iterator<?> iter = body.getUnits().iterator();
		while(iter.hasNext())
		{
			Stmt s = (Stmt)iter.next();
//			if(s instanceof InvokeStmt){
//				if((s.getInvokeExpr()).getMethod().getSignature().equals(AnalyzerMain.INFO_SINK)){
//					SootClass soot_class = method.getDeclaringClass();
//					if(!soot_class.isPhantom()){
//						if(MyConstants.DEBUG_INFO)
//							System.out.println("adding sink class: " + soot_class.getName() + " |" + method);
//						m_Classes.add(soot_class.getName());
//						mClassToMethod.put(soot_class.getName()+"|"+method.getSignature(),
//								method.getSubSignature());
//					}
//				}
//			}else if(s instanceof DefinitionStmt){
//				Value rhs = ((DefinitionStmt) s).getRightOp();
//				if(rhs instanceof InvokeExpr){
//					if(((InvokeExpr) rhs).getMethod().getSignature().equals(AnalyzerMain.INFO_SINK)){
//						SootClass soot_class = method.getDeclaringClass();
//						if(!soot_class.isPhantom()){
//							if(MyConstants.DEBUG_INFO)
//								System.out.println("adding sink class: " + soot_class.getName() + " |" + method);
//							m_Classes.add(soot_class.getName());
//							mClassToMethod.put(soot_class.getName()+"|"+method.getSignature(),
//									method.getSubSignature());
//						}
//					}
//				}				 
//			}
//			//added for BBN
//			else 
			if(s.toString().equals(AnalyzerMain.INFO_SINK)	&& method.toString().equals(AnalyzerMain.SINK_METHOD)) {
				SootClass soot_class = method.getDeclaringClass();
				if(!soot_class.isPhantom()){
					if(MyConstants.DEBUG_INFO)
						System.out.println("adding conditional class: " + soot_class.getName() + MyConstants.SIGNATURE_SEPERATOR + method);
					m_Classes.add(soot_class.getName());
					mClassToMethod.put(soot_class.getName() + MyConstants.SIGNATURE_SEPERATOR + method.getSignature(), method.getSubSignature());
					sinkSig = soot_class.getName() + MyConstants.SIGNATURE_SEPERATOR + method.getSignature();
				}
			}
		}
	
	}
	
//	private boolean equalsMethod(String sig1, String sig2){
//		if(sig1.equals(sig2)){
//			return true;
//		}
//		return false;
//	}

	public List<String> getClasses() {
		return m_Classes;
	}
		
	public LinkedHashMap<String, String> getClassToMethod(){
		return this.mClassToMethod;
	}

}
