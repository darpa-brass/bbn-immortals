package mil.darpa.immortals.core.das;

public class DFU {

	public DFU() {}
	
	public DFU(String uri, String className, DependencyCoordinate dependencyCoordinate, String functionalityTypeUri) {
		this.uri = uri;
		this.className = className;
		this.dependencyCoordinate = dependencyCoordinate;
		this.functionalityTypeUri = functionalityTypeUri;
	}
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getClassName() {
		return className;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}

	public int getResourceIndex() {
		return resourceIndex;
	}

	public void setResourceIndex(int resourceIndex) {
		this.resourceIndex = resourceIndex;
	}

	public DependencyCoordinate getDependencyCoordinate() {
		return dependencyCoordinate;
	}

	public void setDependencyCoordinate(DependencyCoordinate dependencyCoordinate) {
		this.dependencyCoordinate = dependencyCoordinate;
	}
	
	public String getFunctionalityTypeUri() {
		return functionalityTypeUri;
	}

	public void setFunctionalityTypeUri(String functionalityTypeUri) {
		this.functionalityTypeUri = functionalityTypeUri;
	}

	private String uri;
	private String className;
	private int resourceIndex;
	private DependencyCoordinate dependencyCoordinate;
	private String functionalityTypeUri;

}
