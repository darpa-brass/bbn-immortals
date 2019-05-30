package mil.darpa.immortals.flitcons.mdl;

import mil.darpa.immortals.flitcons.Configuration;
import org.kie.api.runtime.rule.AgendaFilter;

import javax.annotation.Nonnull;

public enum ValidationScenario {
	InputConfigurationRequirements(
			"Input MDLRoot Configuration",
			match -> "FaultyConfiguration".equals(match.getRule().getMetaData().get("ValidationMode"))),
	DauInventory(
			"Input DAU Inventory",
			match -> "DAUInventory".equals(match.getRule().getMetaData().get("ValidationMode"))),
	OutputConfigurationUsage(
			"Output MDL Requirements Configuration",
			match -> "ValidConfiguration".equals(match.getRule().getMetaData().get("ValidationMode"))),
	InputConfigurationUsage(
			"Input MDLRoot Configuration",
			match -> "ValidConfiguration".equals(match.getRule().getMetaData().get("ValidationMode")));

	public final String title;
	public final AgendaFilter filter;

	ValidationScenario(@Nonnull String title, @Nonnull AgendaFilter filter) {
		this.title = title;
		this.filter = filter;
	}
}
