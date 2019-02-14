package mil.darpa.immortals.core.das.adaptationmodules.crossappdependencies;

import mil.darpa.immortals.config.ImmortalsConfig;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 6/30/18.
 */
public enum Prop {
    HAS_CHOSEN_INSTANCE(Prefix.IMMoRTALS, "hasChosenInstance"),
    HAS_CONFIGURATION_BINDINGS(Prefix.IMMoRTALS, "hasConfigurationBindings"),
    HAS_BINDING(Prefix.IMMoRTALS, "hasBinding"),
    HAS_SEMANTIC_TYPE(Prefix.IMMoRTALS, "hasSemanticType"),
    BBN_HAS_ARTIFACT_IDENTIFIER(Prefix.IMMoRTALS_mil_darpa_immortals_ontology, "hasArtifactIdentifier");

    public final Prefix prefix;
    public final String path;
    public final String uri;

    Prop(@Nonnull Prefix prefix, @Nonnull String path) {
        this.prefix = prefix;
        this.path = path;
        this.uri = prefix.uri + path;
    }

    public String toString() {
        return this.uri;
    }
}
