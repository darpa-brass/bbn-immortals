package mil.darpa.immortals.core.das;

public class ApiSpecificationComponent {

	public ApiSpecificationComponent(ParadigmComponent paradigmComponent, String durableId, String specification) {
		this.paradigmComponent = paradigmComponent;
		this.durableId = durableId;
		this.specification = specification;
	}
	
	public ParadigmComponent getParadigmComponent() {
		return paradigmComponent;
	}
	
	public void setParadigmComponent(ParadigmComponent paradigmComponent) {
		this.paradigmComponent = paradigmComponent;
	}
	
	public String getDurableId() {
		return durableId;
	}
	
	public void setDurableId(String durableId) {
		this.durableId = durableId;
	}
	
	public String getSpecification() {
		return specification;
	}
	
	public void setSpecification(String specification) {
		this.specification = specification;
	}
	
	private ParadigmComponent paradigmComponent;
	private String durableId;
	private String specification;
	
}
