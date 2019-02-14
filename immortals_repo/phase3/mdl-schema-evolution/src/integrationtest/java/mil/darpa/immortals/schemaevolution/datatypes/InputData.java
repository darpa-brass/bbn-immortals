package mil.darpa.immortals.schemaevolution.datatypes;

import javax.annotation.Nonnull;

/**
 * Mock Input Data for SwRI - Scenario 5
 */
public class InputData {

	/**
	 * The initial MDL version. Always Required
	 */
	public final KnownMdlSchemaVersions initialMdlVersion;

	/**
	 * The updated MDL version. Required if {@link InputData#updatedMdlSchema} is null.
	 */
	public final KnownMdlSchemaVersions updatedMdlVersion;

	/**
	 * The updated MDL schema. Required if {@link InputData#updatedMdlVersion} is null.
	 *
	 * TODO: Confirm with SwRI the new schema will be less than 4MB (or we cannot use Json. The current one is 500KB, so we should be safe)
	 */
	public final String updatedMdlSchema;

	public InputData(@Nonnull KnownMdlSchemaVersions initialMdlVersion, @Nonnull KnownMdlSchemaVersions updatedMdlVersion) {
		this.initialMdlVersion = initialMdlVersion;
		this.updatedMdlVersion = updatedMdlVersion;
		this.updatedMdlSchema = null;
	}

	public InputData(@Nonnull KnownMdlSchemaVersions initialMdlVersion, @Nonnull String updatedMdlSchema) {
		this.initialMdlVersion = initialMdlVersion;
		this.updatedMdlVersion = null;
		this.updatedMdlSchema = updatedMdlSchema;
	}
}
