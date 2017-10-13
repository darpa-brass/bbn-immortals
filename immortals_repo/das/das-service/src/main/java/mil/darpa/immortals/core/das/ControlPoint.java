package mil.darpa.immortals.core.das;

public class ControlPoint {

	public ControlPoint() {}
	
	public ControlPoint(String uri, String uuid, String className, String classUrl, String dataflowEntryPoint) {
		this.uri = uri;
		this.uuid = uuid;
		this.className = className;
		this.classUrl = classUrl;
		this.dataflowEntryPoint = dataflowEntryPoint;
	}
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getClassUrl() {
		return classUrl;
	}

	public void setClassUrl(String classUrl) {
		this.classUrl = classUrl;
	}
	
	public String getDataflowEntryPoint() {
		return dataflowEntryPoint;
	}

	public void setDataflowEntryPoint(String dataflowEntryPoint) {
		this.dataflowEntryPoint = dataflowEntryPoint;
	}

	private String uri;
	private String uuid;
	private String className;
	private String classUrl;
	private String dataflowEntryPoint;
}
