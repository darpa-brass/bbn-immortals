package mil.darpa.immortals.core.das;

import java.util.List;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import mil.darpa.immortals.core.das.sparql.MissionMetrics;

public class Test {

	public static void main(String[] args) {
		
		//2x + 3y - 2z = 1
		//-x + 7y + 6x = -2
		//4x - 3y - 5z = 1
		
		//5x+2=12
		//5x=10
		//25=(NumberOfClients*(PLIReportRate/60.0)*3.2) + (NumberOfClients*(ImageReportRate/60.0)*192000) + ((NumberOfClients-1)*NumberOfClients*(PLIReportRate/60.0)*3.2) + ((NumberOfClients-1)*NumberOfClients*(ImageReportRate/60.0)*192000)

		//RealMatrix coefficients = new Array2DRowRealMatrix(new double[][] { {5}}, false);
		//DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();
		//RealVector constants = new ArrayRealVector(new double[] { 25}, false);
		//RealVector solution = solver.solve(constants);
		
		//System.out.println("Print x:" + solution.getEntry(0));
		
		
		Number result = null;
		
		JexlEngine jexlEngine = new JexlEngine();
		jexlEngine.setLenient(false);
		jexlEngine.setSilent(false);
		 
		Expression e = jexlEngine.createExpression("(NumberOfClients*(PLIReportRate/60.0)*3.2) + (NumberOfClients*(ImageReportRate/60.0)*((((DefaultImageSize*10^6)*24)/15.0)/10^3)) + ((NumberOfClients-1)*NumberOfClients*(PLIReportRate/60.0)*3.2)+ ((NumberOfClients-1)*NumberOfClients*(ImageReportRate/60.0)*((((DefaultImageSize*10^6)*24)/15.0)/10^3))");

	    JexlContext context = new MapContext();
	    
	    context.set("NumberOfClients", 25);
	    context.set("PLIReportRate", 10);
	    context.set("ImageReportRate", 1);
	    context.set("DefaultImageSize", 1.25);
	    
	    result = (Number) e.evaluate(context);
		
		System.out.println(result);

	}
}
