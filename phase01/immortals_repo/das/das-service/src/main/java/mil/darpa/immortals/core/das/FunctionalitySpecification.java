package mil.darpa.immortals.core.das;

import java.util.ArrayList;
import java.util.List;

public class FunctionalitySpecification {

	public FunctionalitySpecification(String functionalitySpecificationUri, String functionalityUri) {
		this.functionalitySpecificationUri = functionalitySpecificationUri;
		this.functionalityUri = functionalityUri;
		propertyUris = new ArrayList<String>();
	}
	
	public String getFunctionalitySpecificationUri() {
		return functionalitySpecificationUri;
	}
	
	public void setFunctionalitySpecificationUri(String functionalitySpecificationUri) {
		this.functionalitySpecificationUri = functionalitySpecificationUri;
	}
	
	public String getFunctionalityUri() {
		return functionalityUri;
	}

	public void setFunctionalityUri(String functionalityUri) {
		this.functionalityUri = functionalityUri;
	}
	
	public List<String> getPropertyUris() {
		return propertyUris;
	}

	public void setPropertyUris(List<String> propertyUris) {
		this.propertyUris = propertyUris;
	}
	
	public void addPropertyUri(String propertyUri) {
		propertyUris.add(propertyUri);
	}
	
	
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(getFunctionalityUri());

		if (propertyUris != null) {
			sb.append(" - Properties: ");
			
			sb.append("(");
			
			for (String p : getPropertyUris()) {
				sb.append(p + " : ");
			}
			
			sb.append(")");
		}
		
		return sb.toString();
	}



	private String functionalitySpecificationUri;
	private String functionalityUri;
	private List<String> propertyUris;
}
