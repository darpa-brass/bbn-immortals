package mil.darpa.immortals.core.das;

import java.util.ArrayList;
import java.util.List;

public class ApiSpecification {
	
	public ApiSpecification(UsageParadigm usageParadigm) {
		this.usageParadigm = usageParadigm;
	}
	
	public ApiSpecification addComponent(ApiSpecificationComponent component) {
		this.apiSpecificationComponents.add(component);
		return this;
	}
	
	public UsageParadigm getUsageParadigm() {
		return usageParadigm;
	}

	public void setUsageParadigm(UsageParadigm usageParadigm) {
		this.usageParadigm = usageParadigm;
	}

	private UsageParadigm usageParadigm;
	private List<ApiSpecificationComponent> apiSpecificationComponents = new ArrayList<ApiSpecificationComponent>();
}
