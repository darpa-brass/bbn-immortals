package mil.darpa.immortals.core.das;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CodeMetricMetaData {

	public CodeMetricMetaData() {}
		
	public CodeMetricMetaData addVariable(Variable variable) {
		
		if (variable != null) {
			variables.put(variable.getUri(), variable);
		}
		
		return this;
	}
	
	public Variable getVariable(String uri) {
		
		Variable result = null;
		
		if (uri != null && uri.trim().length() > 0) {
			result = variables.get(uri);
		}
		
		return result;
	}
	
	public Collection<Variable> getVariables() {
		
		return variables.values();
	}
	
	private Map<String, Variable> variables = new HashMap<String, Variable>();

	public static class Variable {
		
		public Variable(String uri, VariableTypeValue type) {
			setUri(uri);
			setType(type);
		}

		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		public VariableTypeValue getType() {
			return type;
		}

		public void setType(VariableTypeValue type) {
			this.type = type;
		}

		private String uri;
		private VariableTypeValue type;
	}
	
	public enum VariableTypeValue {

		INDEPENDENT_VARIABLE("INDEPENDENT_VARIABLE", "Indepedent Variable"),
	    DEPENDENT_VARIABLE("DEPENDENT_VARIABLE", "Dependent Variable"),
	    CONFIGURATION_VARIABLE("CONFIGURATION_VARIABLE", "Configuration Variable");

		private final String value;
		private final String description;

		VariableTypeValue(String value, String description) {
			this.value = value;
			this.description = description;
	    }

	    public String value() {
	        return value;
	    }
	    
	    public String description() {
	    	return description;
	    }

		public static VariableTypeValue fromValue(String v) {
	    	
			VariableTypeValue result = null;
	    	
	        for (VariableTypeValue c: VariableTypeValue.values()) {
	            if (c.value.equals(v)) {
	                result = c;
	            }
	        }
	        
	        return result;
	    }

	}
}
