package mil.darpa.immortals.core.das.knowledgebuilders.schemadependency;

import net.sf.jsqlparser.expression.Expression;

public class LiteralReference {

	public LiteralReference(String column, Expression value, int position) {
		this.position = position;
		this.column = column;
		this.value = value;
	}
	
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public Expression getValue() {
		return value;
	}

	public void setValue(Expression value) {
		this.value = value;
	}

	private int position;
	private String column;
	private Expression value;
}
