package mil.darpa.immortals.core.das.adaptationmodules.schemaevolution;

import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;

import mil.darpa.immortals.core.das.adaptationmodules.schemaevolution.LearnedQuery;
import mil.darpa.immortals.core.das.exceptions.InvalidOrMissingParametersException;
import mil.darpa.immortals.core.das.knowledgebuilders.generic.AbstractCodePatternResolver;

public class PreparedStatementParameterReorderer extends AbstractCodePatternResolver {

	@Override
	public Map<String, Object> resolvePattern(CompilationUnit cu, TypeSolver typeSolver, 
			Map<String, Object> parameters) {

		Map<String, Object> components = null;

		if (parameters == null || !parameters.containsKey(PARAM_LEARNED_QUERY)) {
			throw new InvalidOrMissingParametersException("Learned query parameter.");
		}
		
		LearnedQuery learnedQuery = (LearnedQuery) parameters.get(PARAM_LEARNED_QUERY);
		
		if (learnedQuery == null) {
			throw new InvalidOrMissingParametersException("Learned query parameter.");
		}
		
		List<MethodCallExpr> methodCalls = cu.findAll(MethodCallExpr.class, 
				t -> t.getName().asString().equals(SET_STATEMENT));
		
		for (MethodCallExpr methodCall : methodCalls) {
			Expression argument = methodCall.getArgument(0);
			Expression targetObject = (NameExpr) methodCall.getChildNodes().get(0);
			
			String argumentType = argument.getClass().getName();
			String targetObjectType = JavaParserFacade.get(typeSolver).solve(targetObject)
					.getCorrespondingDeclaration().getType().describe();

			if (argumentType.equalsIgnoreCase(com.github.javaparser.ast.expr.IntegerLiteralExpr.class.getName()) && 
					targetObjectType.equalsIgnoreCase(java.sql.PreparedStatement.class.getName())) {
				
				//Set the argument (ordinal position of parameter)
				ParameterReorderingVisitor v = new ParameterReorderingVisitor();
				v.setParameterOrder(learnedQuery.getParameterOrder());
				v.setTypeSolver(typeSolver);
				methodCall.accept(v, null);
			}
		}
		
		return components;
	}
	
	public static final String PARAM_LEARNED_QUERY = "PARAM_LEARNED_QUERY";
	private static final String SET_STATEMENT = "setString";
}
