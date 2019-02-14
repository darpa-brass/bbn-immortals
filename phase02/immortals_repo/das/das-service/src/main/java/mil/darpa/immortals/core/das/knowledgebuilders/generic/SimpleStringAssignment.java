package mil.darpa.immortals.core.das.knowledgebuilders.generic;

public class SimpleStringAssignment {
	
	public SimpleStringAssignment() {}
	
	public StringBuilder getValue() {
		return value;
	}

	public int getLineStart() {
		return lineStart;
	}

	public void setLineStart(int lineStart) {
		this.lineStart = lineStart;
	}

	public int getLineEnd() {
		return lineEnd;
	}
	
	public void setLineEnd(int lineEnd) {
		this.lineEnd = lineEnd;
	}

	public boolean valueResolved() {
		return this.getValue().length() > 0;
	}
	
	private StringBuilder value = new StringBuilder();
	private int lineStart = -1;
	private int lineEnd = -1;

}
