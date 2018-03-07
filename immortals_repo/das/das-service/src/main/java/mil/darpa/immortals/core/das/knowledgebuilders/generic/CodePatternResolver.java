package mil.darpa.immortals.core.das.knowledgebuilders.generic;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;

public interface CodePatternResolver {
	
	Map<String, Object> resolvePattern(CompilationUnit cu, TypeSolver typeSolver);
	Map<String, Object> resolvePattern(CompilationUnit cu, TypeSolver typeSolver, Map<String, Object> parameters);
	
}
