package mil.darpa.immortals.core.das.adaptationmodules.schemaevolution;

import java.util.List;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;

public class ParameterReorderingVisitor extends ModifierVisitor<Void> {

	@Override
	public Visitable visit(MethodCallExpr n, Void arg) {
		super.visit(n, arg);

		if (n.getArguments() != null && n.getArguments().size() > 1 &&
				n.getChildNodes() != null && n.getChildNodes().size() > 1 &&
				n.getChildNodes().get(0) instanceof NameExpr) {

			Expression argument = n.getArgument(0);
			Expression targetObject = (NameExpr) n.getChildNodes().get(0);
			
			String argumentType = argument.getClass().getName();
			
			String targetObjectType = JavaParserFacade.get(typeSolver).solve(targetObject)
					.getCorrespondingDeclaration().getType().describe();
	
			if (argumentType.equalsIgnoreCase(com.github.javaparser.ast.expr.IntegerLiteralExpr.class.getName()) && 
					targetObjectType.equalsIgnoreCase(java.sql.PreparedStatement.class.getName())) {
	
				int existingIndex = n.getArgument(0).asIntegerLiteralExpr().asInt();
				Expression expr = new IntegerLiteralExpr(parameterOrder.get(existingIndex-1).intValue());
				
				n.setArgument(0, expr);
			}
		}
		
		return n;
	}

	public List<Integer> getParameterOrder() {
		return parameterOrder;
	}

	public void setParameterOrder(List<Integer> parameterOrder) {
		this.parameterOrder = parameterOrder;
	}

	public TypeSolver getTypeSolver() {
		return typeSolver;
	}

	public void setTypeSolver(TypeSolver typeSolver) {
		this.typeSolver = typeSolver;
	}

	private List<Integer> parameterOrder = null;
	private TypeSolver typeSolver = null;
}
