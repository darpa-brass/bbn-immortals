package mil.darpa.immortals.core.das.knowledgebuilders.schemadependency;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.MySQLGroupConcat;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.NumericBind;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.WithinGroupExpression;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;

public class GenericSQLVisitor implements SelectItemVisitor, SelectVisitor, ExpressionVisitor, FromItemVisitor, ItemsListVisitor {

	private boolean disjunctiveFilter = false;
	List<String> tables = new ArrayList<String>();
	List<String> projection = new ArrayList<String>();
	List<Parameter> parameters = new ArrayList<Parameter>();
	List<LiteralReference> literalsInFilter = new ArrayList<>();
	
	@Override
	public void visit(ExpressionList expressionList) {
		
		for (Expression e : expressionList.getExpressions()) {
			e.accept(this);
		}
	}

	@Override
	public void visit(MultiExpressionList multiExprList) {
	}

	@Override
	public void visit(Table tableName) {
		tables.add(tableName.getFullyQualifiedName());
	}

	@Override
	public void visit(SubJoin subjoin) {
	}

	@Override
	public void visit(LateralSubSelect lateralSubSelect) {
	}

	@Override
	public void visit(ValuesList valuesList) {
	}

	@Override
	public void visit(TableFunction tableFunction) {
	}

	@Override
	public void visit(NullValue nullValue) {
	}

	@Override
	public void visit(Function function) {
	}

	@Override
	public void visit(SignedExpression signedExpression) {
	}

	@Override
	public void visit(JdbcParameter jdbcParameter) {
	}

	@Override
	public void visit(JdbcNamedParameter jdbcNamedParameter) {
	}

	@Override
	public void visit(DoubleValue doubleValue) {
	}

	@Override
	public void visit(LongValue longValue) {
	}

	@Override
	public void visit(HexValue hexValue) {
	}

	@Override
	public void visit(DateValue dateValue) {
	}

	@Override
	public void visit(TimeValue timeValue) {
	}

	@Override
	public void visit(TimestampValue timestampValue) {
	}

	@Override
	public void visit(Parenthesis parenthesis) {
		parenthesis.getExpression().accept(this);
	}

	@Override
	public void visit(StringValue stringValue) {
	}

	@Override
	public void visit(Addition addition) {
		visitBinaryExpression(addition);
	}

	@Override
	public void visit(Division division) {
		visitBinaryExpression(division);
	}

	@Override
	public void visit(Multiplication multiplication) {
		visitBinaryExpression(multiplication);
	}

	@Override
	public void visit(Subtraction subtraction) {
		visitBinaryExpression(subtraction);
	}

	@Override
	public void visit(AndExpression andExpression) {
		visitBinaryExpression(andExpression);
	}

	@Override
	public void visit(OrExpression orExpression) {
		setDisjunctiveFilter(true);
		visitBinaryExpression(orExpression);
	}

	@Override
	public void visit(Between between) {
		between.getLeftExpression().accept(this);
		between.getBetweenExpressionStart().accept(this);
		between.getBetweenExpressionEnd().accept(this);
	}

	@Override
	public void visit(EqualsTo equalsTo) {
		visitBinaryExpression(equalsTo);
	}

	@Override
	public void visit(GreaterThan greaterThan) {
		visitBinaryExpression(greaterThan);
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		visitBinaryExpression(greaterThanEquals);
	}

	@Override
	public void visit(InExpression inExpression) {
	}

	@Override
	public void visit(IsNullExpression isNullExpression) {
	}

	@Override
	public void visit(LikeExpression likeExpression) {
		visitBinaryExpression(likeExpression);
	}

	@Override
	public void visit(MinorThan minorThan) {
		visitBinaryExpression(minorThan);
	}

	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		visitBinaryExpression(minorThanEquals);
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		visitBinaryExpression(notEqualsTo);
	}

	@Override
	public void visit(Column tableColumn) {
	}

	@Override
	public void visit(SubSelect subSelect) {
		subSelect.getSelectBody().accept(this);
	}

	@Override
	public void visit(CaseExpression caseExpression) {
	}

	@Override
	public void visit(WhenClause whenClause) {
	}

	@Override
	public void visit(ExistsExpression existsExpression) {
	}

	@Override
	public void visit(AllComparisonExpression allComparisonExpression) {
	}

	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
	}

	@Override
	public void visit(Concat concat) {
	}

	@Override
	public void visit(Matches matches) {
	}

	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
	}

	@Override
	public void visit(BitwiseOr bitwiseOr) {
	}

	@Override
	public void visit(BitwiseXor bitwiseXor) {
	}

	@Override
	public void visit(CastExpression cast) {
	}

	@Override
	public void visit(Modulo modulo) {
	}

	@Override
	public void visit(AnalyticExpression aexpr) {
	}

	@Override
	public void visit(WithinGroupExpression wgexpr) {
	}

	@Override
	public void visit(ExtractExpression eexpr) {
	}

	@Override
	public void visit(IntervalExpression iexpr) {
	}

	@Override
	public void visit(OracleHierarchicalExpression oexpr) {
	}

	@Override
	public void visit(RegExpMatchOperator rexpr) {
	}

	@Override
	public void visit(JsonExpression jsonExpr) {
	}

	@Override
	public void visit(JsonOperator jsonExpr) {
	}

	@Override
	public void visit(RegExpMySQLOperator regExpMySQLOperator) {
	}

	@Override
	public void visit(UserVariable var) {
	}

	@Override
	public void visit(NumericBind bind) {
	}

	@Override
	public void visit(KeepExpression aexpr) {
	}

	@Override
	public void visit(MySQLGroupConcat groupConcat) {
	}

	@Override
	public void visit(RowConstructor rowConstructor) {
	}

	@Override
	public void visit(OracleHint hint) {
	}

	@Override
	public void visit(TimeKeyExpression timeKeyExpression) {
	}

	@Override
	public void visit(DateTimeLiteralExpression literal) {
	}

	@Override
	public void visit(NotExpression aThis) {
	}

	@Override
	public void visit(PlainSelect plainSelect) {
		
		plainSelect.getSelectItems().forEach(s -> s.accept(this));
		
		plainSelect.getFromItem().accept(this);
				
		if (plainSelect.getJoins() != null) {
			for (Join j : plainSelect.getJoins()) {
				j.getRightItem().accept(this);
			}
		}
		
		if (plainSelect.getWhere() != null) {
			this.literalsInFilter = new ArrayList<LiteralReference>();
			plainSelect.getWhere().accept(this);
		}
	}

	@Override
	public void visit(SetOperationList setOpList) {
	}

	@Override
	public void visit(WithItem withItem) {
	}

	@Override
	public void visit(AllColumns allColumns) {
	}

	@Override
	public void visit(AllTableColumns allTableColumns) {
		projection.add(allTableColumns.toString());
	}

	@Override
	public void visit(SelectExpressionItem selectExpressionItem) {
		projection.add(selectExpressionItem.toString());
	}
	
	public boolean isDisjunctiveFilter() {
		return disjunctiveFilter;
	}

	public void setDisjunctiveFilter(boolean disjunctiveFilter) {
		this.disjunctiveFilter = disjunctiveFilter;
	}
	
	public void visitBinaryExpression(BinaryExpression binaryExpression) {
		
		binaryExpression.getLeftExpression().accept(this);
		binaryExpression.getRightExpression().accept(this);
		
		Expression lhs = binaryExpression.getLeftExpression();
		Expression rhs = binaryExpression.getRightExpression();
		
		if (rhs.getClass().equals(JdbcParameter.class)
				&& lhs.getClass().equals(Column.class)) {
			parameters.add(new Parameter(lhs.toString(), parameters.size() + 1));
		} else if (rhs.getClass().equals(Column.class)
				&& lhs.getClass().equals(JdbcParameter.class)) {
			parameters.add(new Parameter(rhs.toString(), parameters.size() + 1));
		}
		
		if (SQLParserUtilities.isLiteral(rhs) && SQLParserUtilities.isColumn(lhs)) {
			this.literalsInFilter.add(new LiteralReference(lhs.toString(), rhs, this.literalsInFilter.size() + 1));
		} else if (SQLParserUtilities.isColumn(rhs) && SQLParserUtilities.isLiteral(lhs)) {
			this.literalsInFilter.add(new LiteralReference(rhs.toString(), lhs, this.literalsInFilter.size() + 1));
		}
	}
	
	
	public List<Parameter> getParameters() {
		
		return parameters;
	}
	
	public List<String> getProjection() {
		return projection;
	}
	
	public List<LiteralReference> getLiteralsInFilter() {
		return literalsInFilter;
	}
	
}
