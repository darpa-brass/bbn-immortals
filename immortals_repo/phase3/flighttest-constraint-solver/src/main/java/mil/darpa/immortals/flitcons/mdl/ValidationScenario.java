package mil.darpa.immortals.flitcons.mdl;

import mil.darpa.immortals.flitcons.Configuration;
import org.kie.api.runtime.rule.AgendaFilter;

import javax.annotation.Nonnull;

public enum ValidationScenario {
	InputConfiguration(
			"Input MDLRoot Configuration",
			match -> "FaultyConfiguration".equals(match.getRule().getMetaData().get("ValidationMode")),
			Configuration.getInstance().validation),
	DauInventory(
			"DAU Inventory",
			match -> "DAUInventory".equals(match.getRule().getMetaData().get("ValidationMode")),
			Configuration.getInstance().validation),
	ResultantConfiguration(
			"Output MDLRoot Configuration",
			match -> "FixedConfiguration".equals(match.getRule().getMetaData().get("ValidationMode")),
			Configuration.getInstance().validation);

	public final String title;
	public final AgendaFilter filter;
	public final Configuration.ValidationConfiguration configuration;

	ValidationScenario(@Nonnull String title, @Nonnull AgendaFilter filter, Configuration.ValidationConfiguration configuration) {
		this.title = title;
		this.filter = filter;
		this.configuration = configuration;
	}
}
