package mil.darpa.immortals.core.das;

public class ParadigmComponent {

	public ParadigmComponent(String name, String durableId, String multiplicityOperator, int ordering) {
		this.name = name;
		this.durableId = durableId;
		this.multiplicityOperator = multiplicityOperator;
		this.ordering = ordering;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDurableId() {
		return durableId;
	}

	public void setDurableId(String durableId) {
		this.durableId = durableId;
	}

	public String getMultiplicityOperator() {
		return multiplicityOperator;
	}

	public void setMultiplicityOperator(String multiplicityOperator) {
		this.multiplicityOperator = multiplicityOperator;
	}

	public int getOrdering() {
		return ordering;
	}

	public void setOrdering(int ordering) {
		this.ordering = ordering;
	}

	private String name;
	private String durableId;
	private String multiplicityOperator;
	private int ordering;
}
