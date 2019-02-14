package mil.darpa.immortals.core.das.knowledgebuilders.schemadependency;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class ParameterizingExpressionDeParser extends ExpressionDeParser {

	private int parameterMask = 0;
	private int literalIndex = -1;
	private static final String NOT = " NOT ";
	
	public int getParameterMask() {
		return parameterMask;
	}

	public void setParameterMask(int parameterMask) {
		this.parameterMask = parameterMask;
	}

	@Override
	public void visit(AndExpression andExpression) {
		super.visit(andExpression);
	}

	@Override
	public void visit(Between between) {

		//if (SqlParserUtilities.isColumn(between.getLeftExpression()) &&
		//		(SqlParserUtilities.isParameter(between.getBetweenExpressionStart()) || 
		//				SqlParserUtilities.isParameter(between.getBetweenExpressionEnd()))) {
		//	parameterColumns.add(((Column) between.getLeftExpression()).toString());
		//} else {
			super.visit(between);
		//}
	}

	@Override
	public void visit(EqualsTo equalsTo) {
		localVisitBinaryExpression(equalsTo, " = ");
	}

	@Override
	public void visit(GreaterThan greaterThan) {
		localVisitBinaryExpression(greaterThan, " > ");
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		localVisitBinaryExpression(greaterThanEquals, " >= ");
	}

	@Override
	public void visit(MinorThan minorThan) {
		localVisitBinaryExpression(minorThan, " < ");
	}

	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		localVisitBinaryExpression(minorThanEquals, " <= ");
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		localVisitBinaryExpression(notEqualsTo, " <> ");		
	}

	@Override
	public void visit(OrExpression orExpression) {
		super.visit(orExpression);
	}
	
    private void localVisitBinaryExpression(BinaryExpression binaryExpression, String operator) {
        
    	Expression lhs = binaryExpression.getLeftExpression();
    	Expression rhs = binaryExpression.getRightExpression();
    	
    	boolean lhsIsLiteral = SQLParserUtilities.isLiteral(lhs);
    	boolean rhsIsLiteral = SQLParserUtilities.isLiteral(rhs);

    	if (binaryExpression.isNot()) {
            super.getBuffer().append(NOT);
        }
    	
    	boolean parameterize = false;
    	
    	if (lhsIsLiteral || rhsIsLiteral) {
    		++literalIndex;
    		boolean ignore = false;
	    	
    		if (parameterMask != -1) {
		    	int literalMask = 1 << (literalIndex);
		    	ignore = !((literalMask & parameterMask) == literalMask);
	    	}
	    	
	    	if (!ignore && SQLParserUtilities.isColumn(lhs) && rhsIsLiteral) {
	    		parameterize = true;
	    		lhs.accept(this);
	    		super.getBuffer().append(operator + " ? ");
	    	} else if (!ignore && SQLParserUtilities.isColumn(rhs) && lhsIsLiteral) {
	    		parameterize = true;
	    		super.getBuffer().append(" ? " + operator);
	    		rhs.accept(this);
	    	}
    	}
    	
    	if (!parameterize) {
    		lhs.accept(this);
    		super.getBuffer().append(operator);
    		rhs.accept(this);
    	}
    }

}
