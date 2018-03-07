package mil.darpa.immortals.core.das;

import java.util.ArrayList;
import java.util.List;

public class UsageParadigm {
	
	public UsageParadigm(String name, String durableId) {
		this.setName(name);
		this.setDurableId(durableId);
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

	public List<ParadigmComponent> getComponents() {
		return components;
	}

	public void setComponents(List<ParadigmComponent> components) {
		this.components = components;
	}
	
	public UsageParadigm addComponent(ParadigmComponent component) {
		this.components.add(component);
		return this;
	}

	private String name;
	private String durableId;
	private List<ParadigmComponent> components = new ArrayList<ParadigmComponent>();
}
