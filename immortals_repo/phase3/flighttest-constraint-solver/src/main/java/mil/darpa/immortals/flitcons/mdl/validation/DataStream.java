package mil.darpa.immortals.flitcons.mdl.validation;

import mil.darpa.immortals.flitcons.datatypes.hierarchical.DuplicateInterface;

import javax.annotation.Nonnull;

public class DataStream implements DuplicateInterface<DataStream> {

	public final String id;

	public DataStream(@Nonnull String id) {
		this.id = id;
	}

	@Override
	public DataStream duplicate() {
		// Duplication not necessary since all values are final
		return this;
	}
}
