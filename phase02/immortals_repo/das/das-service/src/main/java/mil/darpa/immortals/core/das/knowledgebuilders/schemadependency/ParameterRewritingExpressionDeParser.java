package mil.darpa.immortals.core.das.knowledgebuilders.schemadependency;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class ParameterRewritingExpressionDeParser extends ExpressionDeParser {

	private List<String> parameterColumns = new ArrayList<String>();
	private static final String NOT = " NOT ";
	private boolean parameterParsed = false;

	@Override
	public void visit(AndExpression andExpression) {
		super.visit(andExpression);
	}

	@Override
	public void visit(Between between) {

		if (between.getLeftExpression().getClass().equals(Column.class) &&
				(between.getBetweenExpressionStart().getClass().equals(JdbcParameter.class) || 
						between.getBetweenExpressionEnd().getClass().equals(JdbcParameter.class))) {
			parameterColumns.add(((Column) between.getLeftExpression()).toString());
		} else {
			super.visit(between);
		}
	}

	@Override
	public void visit(EqualsTo equalsTo) {
		checkForParameterReference(equalsTo);
		localVisitBinaryExpression(equalsTo, " = ");
	}

	@Override
	public void visit(GreaterThan greaterThan) {
		checkForParameterReference(greaterThan);
		localVisitBinaryExpression(greaterThan, " > ");
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		checkForParameterReference(greaterThanEquals);
		localVisitBinaryExpression(greaterThanEquals, " >= ");
	}

	@Override
	public void visit(MinorThan minorThan) {
		checkForParameterReference(minorThan);
		localVisitBinaryExpression(minorThan, " < ");
	}

	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		checkForParameterReference(minorThanEquals);
		localVisitBinaryExpression(minorThanEquals, " <= ");
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		checkForParameterReference(notEqualsTo);
		localVisitBinaryExpression(notEqualsTo, " <> ");		
	}

	@Override
	public void visit(OrExpression orExpression) {
		super.visit(orExpression);
	}
	
	public List<String> getParameterColumns() {
		return parameterColumns;
	}

	private boolean checkForParameterReference(BinaryExpression expression) {
		
		boolean result = false;
		
		if (expression.getLeftExpression().getClass().equals(Column.class) &&
				expression.getRightExpression().getClass().equals(JdbcParameter.class)) {
			parameterColumns.add(((Column) expression.getLeftExpression()).toString());
			parameterParsed = true;
			result = true;
		} else if (expression.getLeftExpression().getClass().equals(JdbcParameter.class) &&
				expression.getRightExpression().getClass().equals(Column.class)) {
			parameterColumns.add(((Column) expression.getLeftExpression()).toString());
			parameterParsed = true;
			result = true;
		}
		
		return result;
	}
	
    private void localVisitBinaryExpression(BinaryExpression binaryExpression, String operator) {
        
    	int startIndex = super.getBuffer().length();
    	
    	if (binaryExpression.isNot()) {
            super.getBuffer().append(NOT);
        }
    	
        binaryExpression.getLeftExpression().accept(this);
        
        if (parameterParsed) {
        	parameterParsed = false;
        	super.getBuffer().delete(startIndex, super.getBuffer().length()+1);
        	super.getBuffer().append("TRUE");
        } else {
        	super.getBuffer().append(operator);
            binaryExpression.getRightExpression().accept(this);
            if (parameterParsed) {
            	parameterParsed = false;
            	super.getBuffer().delete(startIndex, super.getBuffer().length()+1);
            	super.getBuffer().append("TRUE");
            }
        }
    }

}
