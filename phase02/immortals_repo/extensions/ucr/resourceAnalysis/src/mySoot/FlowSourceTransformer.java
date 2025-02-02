package mySoot;

import java.util.*;
import soot.jimple.*;
import soot.*;

/*
 * This transformer locates the declaring classes of methods where every source statement gets called
 */
public class FlowSourceTransformer extends BodyTransformer {

	private List<String> m_Classes;
		
	private LinkedHashMap<String, String> mClassToMethod;

	public FlowSourceTransformer() {
		m_Classes = new ArrayList<String>();
		
		mClassToMethod  = new LinkedHashMap<String, String>();
	}

	@Override
	protected void internalTransform(Body body, String string, Map map) {
		
		SootMethod method = body.getMethod();
		
		if(method.getSubSignature().equals(AnalyzerMain.INFO_SOURCE)){
			SootClass soot_class = method.getDeclaringClass();
			if(!soot_class.isPhantom()){
				if(MyConstants.DEBUG_INFO)
					System.out.println("adding source class: " + soot_class.getName());
				m_Classes.add(soot_class.getName());
				mClassToMethod.put(soot_class.getName()+"|"+method.getSignature(),
						method.getSubSignature());
			}
		}
				
		Iterator iter = body.getUnits().iterator();
		
		// For every unit in the body
		while(iter.hasNext()){
			Stmt s = (Stmt)iter.next();	
			
			if(s instanceof DefinitionStmt){
				Value rhs = ((DefinitionStmt) s).getRightOp();
				if(rhs instanceof InvokeExpr){
					// INFO_SOURCE is the current source statement
					if(((InvokeExpr) rhs).getMethod().getSignature().equals(AnalyzerMain.INFO_SOURCE)){
						SootClass soot_class = method.getDeclaringClass();
						if(!soot_class.isPhantom()){
							if(MyConstants.DEBUG_INFO)
								System.out.println("adding source class: " + soot_class.getName());
							m_Classes.add(soot_class.getName());
							mClassToMethod.put(soot_class.getName()+"|"+method.getSignature(),
									method.getSubSignature());
						}
					}
				}else if(rhs instanceof InstanceFieldRef){
					if(((InstanceFieldRef) rhs).getField().getSignature().equals(AnalyzerMain.INFO_SOURCE)){
						if(MyConstants.DEBUG_INFO)
							System.out.println("[SOURCE] " + ((InstanceFieldRef) rhs).getField().getSignature());
						SootClass soot_class = method.getDeclaringClass();
						if(!soot_class.isPhantom()){
							if(MyConstants.DEBUG_INFO)
								System.out.println("adding source class: " + soot_class.getName());
							m_Classes.add(soot_class.getName());
							mClassToMethod.put(soot_class.getName()+"|"+method.getSignature(),
									method.getSubSignature());
						}
					}
				}
				//if source is location, also need to consider this case. To be fixed later.
				/*
				 * 	public void onLocationChanged(android.location.Location)
    				{
        				com.flurry.android.FlurryAgent r0;
        				android.location.Location r1;
        				java.lang.Throwable $r2, $r4;

        				r0 := @this: com.flurry.android.FlurryAgent;
        				r1 := @parameter0: android.location.Location;
        				entermonitor r0;

     					label0:
        					r0.<com.flurry.android.FlurryAgent: android.location.Location D> = r1;
        				label1:
        				exitmonitor r0;
        				return;
        			}
				 */
			}else if(s instanceof InvokeStmt){
				// INFO_SOURCE is the current source statement
				if((s.getInvokeExpr()).getMethod().getSignature().equals(AnalyzerMain.INFO_SOURCE)){
					SootClass soot_class = method.getDeclaringClass();
					if(!soot_class.isPhantom()){
						System.out.println("adding source class: " + soot_class.getName());
						m_Classes.add(soot_class.getName());
						mClassToMethod.put(soot_class.getName()+"|"+method.getSignature(),
								method.getSubSignature());
					}
				}

			}
		}
		
		/*
		if (method.getName().equals(MyConstants.mainEntry) || method.getName().equals(MyConstants.onCreateEntry)
			|| method.getName().equals(MyConstants.threadEntry)) {
			System.out.println(method.toString());
			SootClass soot_class = method.getDeclaringClass();
			System.out.println("adding class: " + soot_class.getName());
			m_Classes.add(soot_class.getName());
			mClassToMethod.put(soot_class.getName(), method.getName());
		}
		*/
	}
	
	private boolean equalsMethod(String sig1, String sig2){
		if(sig1.equals(sig2)){
			return true;
		}
		return false;
	}

	public List<String> getClasses() {
		return m_Classes;
	}
		
	public LinkedHashMap<String, String> getClassToMethod(){
		return this.mClassToMethod;
	}
}
