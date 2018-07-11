package mil.darpa.immortals.core.das.adaptationmodules.crossappdependencies;

import mil.darpa.immortals.config.ImmortalsConfig;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 6/30/18.
 */
public enum Prefix {
    IMMoRTALS("http://darpa.mil/immortals/ontology/r2.0.0#"),
    IMMoRTALS_functionality_aspects("http://darpa.mil/immortals/ontology/r2.0.0/functionality/aspects#"),
    IMMoRTALS_functionality("http://darpa.mil/immortals/ontology/r2.0.0/functionality#"),
    IMMoRTALS_functionality_alg_encryption("http://darpa.mil/immortals/ontology/r2.0.0/functionality/alg/encryption#"),
    IMMoRTALS_dfu_instance("http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#"),
    // TODO: This value is also defined in the config! Bad bad duplication!
    IMMoRTALS_mil_darpa_immortals_ontology("http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#");

    public final String uri;

    Prefix(@Nonnull String uri) {
        this.uri = uri;
    }

    public String toString() {
        return this.uri;
    }
}
