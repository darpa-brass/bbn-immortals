package mil.darpa.immortals.flitcons.mdl;

import mil.darpa.immortals.flitcons.Configuration;
import org.kie.api.runtime.rule.AgendaFilter;

import javax.annotation.Nonnull;

public enum ValidationScenario {
	InputConfigurationRequirements(
			"Input MDLRoot Requirements",
			match -> "FaultyConfiguration".equals(match.getRule().getMetaData().get("ValidationMode"))),
	DauInventory(
			"Input DAU Inventory Requirements",
			match -> "DAUInventory".equals(match.getRule().getMetaData().get("ValidationMode"))),
	OutputConfigurationUsage(
			"Output MDLRoot Usage",
			match -> "ValidConfiguration".equals(match.getRule().getMetaData().get("ValidationMode"))),
	InputConfigurationUsage(
			"Input MDLRoot Usage",
			match -> "ValidConfiguration".equals(match.getRule().getMetaData().get("ValidationMode")));

	public final String title;
	public final AgendaFilter filter;

	ValidationScenario(@Nonnull String title, @Nonnull AgendaFilter filter) {
		this.title = title;
		this.filter = filter;
	}
}
