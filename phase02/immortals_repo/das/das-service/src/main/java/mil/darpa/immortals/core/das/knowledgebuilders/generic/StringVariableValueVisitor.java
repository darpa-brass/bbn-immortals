package mil.darpa.immortals.core.das.knowledgebuilders.generic;

import java.util.Comparator;

import com.github.javaparser.Position;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import mil.darpa.immortals.core.das.exceptions.InvalidOrMissingParametersException;

public class StringVariableValueVisitor extends VoidVisitorAdapter<SimpleStringAssignment> {
	
	public StringVariableValueVisitor() {
	}
	
	@Override
	public void visit(BinaryExpr n, SimpleStringAssignment arg) {
		super.visit(n, arg);
	}

	@Override
	public void visit(StringLiteralExpr n, SimpleStringAssignment arg) {
		
		if (arg.getLineStart() == -1) {
			arg.setLineStart(n.getBegin().get().line);
		}
		
		arg.getValue().append(n.getValue());
		
		arg.setLineEnd(n.getEnd().get().line);
	}

	@Override
	public void visit(VariableDeclarationExpr n, SimpleStringAssignment arg) {
		super.visit(n, arg);
	}
	
	@Override
	public void visit(AssignExpr n, SimpleStringAssignment arg) {
		super.visit(n, arg);
	}

	public SimpleStringAssignment getStaticAssignment(String variableName, Node referenceSite) {
		
		if (variableName == null || variableName.isEmpty() || referenceSite == null) {
			throw new InvalidOrMissingParametersException();
		}
		
		SimpleStringAssignment assignment = new SimpleStringAssignment();

		Node currentNode = referenceSite.getParentNode().orElse(null);
		Position referencePosition = referenceSite.getBegin().get();
		
		//This is limited to the power/scope of static analysis, but it may work well
		//for many patterns involving SQL variable declaration and assignment (since even
		//parameterized SQL often relies on static String assignments).
		while (currentNode != null && !assignment.valueResolved()) {
			Class<?> type = currentNode.getClass();
			if (type ==  ClassOrInterfaceDeclaration.class) {
				currentNode.findAll(FieldDeclaration.class).stream().
						filter(t -> t.getVariables().size() == 1 &&
						 	t.getVariables().get(0).getNameAsString().equals(variableName) &&
						 	t.getVariables().get(0).getInitializer().isPresent() &&
						 	t.getRange().get().isBefore(referencePosition)).findFirst().ifPresent(v -> this.visit(v, assignment));;
			} else if (type == MethodDeclaration.class ||
					type == BlockStmt.class ||
					type == ExpressionStmt.class) {
				
				currentNode.findAll(AssignExpr.class).stream()
						.filter(ae -> ae.getRange().get().isBefore(referencePosition) &&
								ae.getTarget().isNameExpr() && ae.getTarget().toNameExpr().get().getNameAsString().equals(variableName))
						.max(assignExprCompar).ifPresent(v -> this.visit(v, assignment));

				if (!assignment.valueResolved()) {
					currentNode.findAll(VariableDeclarationExpr.class).stream()
						.filter(t -> t.getVariables().size() == 1 &&
							 	t.getVariables().get(0).getNameAsString().equals(variableName) &&
							 	t.getVariables().get(0).getInitializer().isPresent() &&
							 	t.getRange().get().isBefore(referencePosition)).findFirst().ifPresent(v -> this.visit(v, assignment));
				}
			}
			
			currentNode = currentNode.getParentNode().orElse(null);
		}
		
		return assignment;
	}
	
	final Comparator<AssignExpr> assignExprCompar = 
			(p1, p2) -> p1.getEnd().get().compareTo(p2.getEnd().get());

}
