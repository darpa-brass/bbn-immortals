package mil.darpa.immortals.core.das.knowledgebuilders.generic;

import java.util.HashMap;
import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;

public abstract class AbstractCodePatternResolver implements CodePatternResolver {

	/* (non-Javadoc)
	 * Default behavior is to return empty Map; concrete pattern implementations are free to not implement every override in this class.
	 * 
	 * @see mil.darpa.immortals.core.das.knowledgebuilders.code.CodePatternResolver#resolvePattern(com.github.javaparser.ast.CompilationUnit, com.github.javaparser.symbolsolver.model.resolution.TypeSolver)
	 */
	public Map<String, Object> resolvePattern(CompilationUnit cu, TypeSolver typeSolver) {
		return new HashMap<String, Object>();
	}
	
	/* (non-Javadoc)
	 * Default behavior is to return empty Map; concrete pattern implementations are free to not implement every override in this class.
	 * 
	 * @see mil.darpa.immortals.core.das.knowledgebuilders.code.CodePatternResolver#resolvePattern(com.github.javaparser.ast.CompilationUnit, com.github.javaparser.symbolsolver.model.resolution.TypeSolver, java.util.Map)
	 */
	public Map<String, Object> resolvePattern(CompilationUnit cu, TypeSolver typeSolver, Map<String, Object> parameters) {
		return new HashMap<String, Object>();
	}
	

}
