package mil.darpa.immortals.core.das.knowledgebuilders.schemadependency;

import mil.darpa.immortals.core.das.exceptions.InvalidOrMissingParametersException;

public class Parameter {
	
	public Parameter() {}

	public Parameter(String columnName, int ordinalPosition) {
		this.columnName = columnName;
		this.ordinalPosition = ordinalPosition;
	}
	
	public Parameter(String columnName, int sqlType, Object value, int ordinalPosition, String parameterUri) {
		this.columnName = columnName;
		this.type = sqlType;
		this.value = value;
		this.ordinalPosition = ordinalPosition;
		this.parameterUri = parameterUri;
	}
	
	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
	public int getOrdinalPosition() {
		return ordinalPosition;
	}

	public void setOrdinalPosition(int ordinalPosition) {
		this.ordinalPosition = ordinalPosition;
	}
	
	public String getParameterUri() {
		return parameterUri;
	}

	public void setParameterUri(String parameterUri) {
		this.parameterUri = parameterUri;
	}

	public static String formatToString(Parameter p) {
		
		if (p == null) {
			throw new InvalidOrMissingParametersException("Missing Parameter p");
		}
		
		switch (p.getType()) {
			case java.sql.Types.BIGINT:
			case java.sql.Types.DECIMAL:
			case java.sql.Types.DOUBLE:
			case java.sql.Types.FLOAT:
			case java.sql.Types.NUMERIC:
			case java.sql.Types.INTEGER:
			case java.sql.Types.SMALLINT:
				if (p.getValue() == null) {
					return "NULL";
				} else {
					return p.getValue().toString();
				}
			case java.sql.Types.VARCHAR:
			case java.sql.Types.NVARCHAR:
			case java.sql.Types.CHAR:
				if (p.getValue() == null) {
					return "NULL";
				} else {
					return "'" + p.getValue().toString().replaceAll("'", "''") + "'";					
				}
			default:
				if (p.getValue() == null) {
					return "NULL";
				} else {
					return p.getValue().toString();
				}
		}
	}
	
	private String columnName;
	private int type;
	private Object value;
	private int ordinalPosition;
	private String parameterUri;
}
