package mil.darpa.immortals.core.das.knowledgebuilders.schemadependency;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;

import mil.darpa.immortals.core.das.knowledgebuilders.generic.AbstractCodePatternResolver;
import mil.darpa.immortals.core.das.knowledgebuilders.generic.SimpleStringAssignment;
import mil.darpa.immortals.core.das.knowledgebuilders.generic.StringVariableValueVisitor;

public class PreparedStatementPatternResolver extends AbstractCodePatternResolver {

	@Override
	public Map<String, Object> resolvePattern(CompilationUnit cu, TypeSolver typeSolver) {

		Map<String, Object> components = new HashMap<>();
		DataLinkageMetadata dataLinkageMetadata = new DataLinkageMetadata();
		
		Optional<MethodCallExpr> methodCall = cu.findFirst(MethodCallExpr.class, 
				t -> t.getName().asString().equals(SQL_INVOKING_METHOD_NAME));
		
		if (methodCall.isPresent()) {
			
			Expression query = methodCall.get().getArgument(0);
			Expression conn = (NameExpr) methodCall.get().getChildNodes().get(0);
			
			String queryType = JavaParserFacade.get(typeSolver).solve(query)
					.getCorrespondingDeclaration().getType().describe();
			String connType = JavaParserFacade.get(typeSolver).solve(conn)
					.getCorrespondingDeclaration().getType().describe();

			if (queryType.equalsIgnoreCase(String.class.getName()) && 
					connType.equalsIgnoreCase(java.sql.Connection.class.getName())) {
				//Get the query
				StringVariableValueVisitor sc = new StringVariableValueVisitor();
				SimpleStringAssignment assignment = sc.getStaticAssignment(query.toString(), methodCall.get());
				
				if (assignment != null && assignment.valueResolved()) {
					dataLinkageMetadata.setOriginalSql(assignment.getValue().toString());
					dataLinkageMetadata.setSqlLineNumberStart(assignment.getLineStart());
					dataLinkageMetadata.setSqlLineNumberEnd(assignment.getLineEnd());
					dataLinkageMetadata.setSqlVariableName(query.toString());
					components.put(DATA_ACCESS_DFU_METADATA, dataLinkageMetadata);
				}
			}
		}
		
		return components;
	}
	
	public static final String DATA_ACCESS_DFU_METADATA = "DATA_ACCESS_DFU_METADATA";
	private static final String SQL_INVOKING_METHOD_NAME = "prepareStatement";
	

}
