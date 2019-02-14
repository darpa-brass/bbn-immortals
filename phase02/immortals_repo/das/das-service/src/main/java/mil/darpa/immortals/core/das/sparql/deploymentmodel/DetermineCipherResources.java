package mil.darpa.immortals.core.das.sparql.deploymentmodel;

import mil.darpa.immortals.core.das.sparql.SparqlQuery;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by awellman@bbn.com on 6/28/18.
 */
public class DetermineCipherResources extends SparqlQuery {

    public static class CipherResourceDetails {
        public final String adaptationTarget;
        public final boolean hasAESNI;
        public final boolean hasUCS;

        public CipherResourceDetails(@Nonnull String adaptationTarget, boolean hasAESNI, boolean hasUCS) {
            this.adaptationTarget = adaptationTarget;
            this.hasAESNI = hasAESNI;
            this.hasUCS = hasUCS;
        }
    }

    public static Map<String, CipherResourceDetails> select(DasAdaptationContext dac) {
        String query =
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
                        "PREFIX IMMoRTALS_functionality: <http://darpa.mil/immortals/ontology/r2.0.0/functionality#> " +
                        "PREFIX IMMoRTALS_cp_jvm: <http://darpa.mil/immortals/ontology/r2.0.0/cp/jvm#> " +
                        "PREFIX IMMoRTALS_gmei: <http://darpa.mil/immortals/ontology/r2.0.0/gmei#> " +
                        "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX owl:   <http://www.w3.org/2002/07/owl#> " +
                        "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> " +
                        "PREFIX IMMoRTALS_cp2: <http://darpa.mil/immortals/ontology/r2.0.0/cp2#> " +
                        "PREFIX IMMoRTALS_functionality_datatype: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/datatype#> " +
                        "PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> " +
                        "PREFIX IMMoRTALS_com_securboration_dontcommit: <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/dontcommit#> " +
                        "PREFIX IMMoRTALS_resources_compute: <http://darpa.mil/immortals/ontology/r2.0.0/resources/compute#> " +
                        "PREFIX IMMoRTALS_property_impact: <http://darpa.mil/immortals/ontology/r2.0.0/property/impact#> " +
                        "PREFIX IMMoRTALS_functionality_aspects: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/aspects#> " +
                        "PREFIX IMMoRTALS_mil_darpa_immortals_ontology: <http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#> " +
                        "SELECT ?targetIdentifier ?hasUCS ?hasAESNI " +
                        "WHERE { " +
                        "      GRAPH <" + dac.getKnowldgeUri() + "> { " +
                        "    ?deploymentmodel a IMMoRTALS_gmei:DeploymentModel . " +
                        "    ?aDeploymentModel IMMoRTALS:hasSessionIdentifier \"" + dac.getAdaptationIdentifer() + "\" .  " +
                        "    ?deploymentmodel IMMoRTALS:hasAvailableResources ?availableResources . " +
                        "    { " +
                        "      { " +
                        "        ?availableResources IMMoRTALS_mil_darpa_immortals_ontology:hasArtifactIdentifier ?targetIdentifier . " +
                        "        ?availableResources a IMMoRTALS_cp2:Analysis.Atak.AtakPhone . " +
                        "        ?availableResources IMMoRTALS:hasResources ?atakPlatformResources . " +
                        "        ?atakPlatformResources a IMMoRTALS_cp_jvm:AndroidRuntimeEnvironment . " +
                        "        ?atakPlatformResources IMMoRTALS:hasUnlimitedCryptoStrengh ?hasUCS . " +
                        "        ?availableResources IMMoRTALS:hasResources ?atakCpuResources . " +
                        "        ?atakCpuResources a IMMoRTALS_resources_compute:Cpu . " +
                        "        BIND( " +
                        "          EXISTS { " +
                        "            ?atakCpuResources IMMoRTALS:hasInstructionSetArchitectureSupport  " +
                        "                  IMMoRTALS_resources_compute:InstructionSets.AES_NI " +
                        "          } " +
                        "          as ?hasAESNI " +
                        "        ) " +
                        "      } UNION { " +
                        "        ?availableResources IMMoRTALS_mil_darpa_immortals_ontology:hasArtifactIdentifier ?targetIdentifier . " +
                        "        ?availableResources a IMMoRTALS_cp2:Analysis.Marti.MartiServer . " +
                        "        ?availableResources IMMoRTALS:hasResources ?martiPlatformResources . " +
                        "        ?martiPlatformResources a IMMoRTALS_cp_jvm:JavaRuntimeEnvironment . " +
                        "        ?martiPlatformResources IMMoRTALS:hasUnlimitedCryptoStrengh ?hasUCS . " +
                        "        ?availableResources IMMoRTALS:hasResources ?martiCpuResources . " +
                        "        ?martiCpuResources a IMMoRTALS_resources_compute:Cpu . " +
                        "        BIND( " +
                        "          EXISTS { " +
                        "            ?martiCpuResources IMMoRTALS:hasInstructionSetArchitectureSupport  " +
                        "                  IMMoRTALS_resources_compute:InstructionSets.AES_NI " +
                        "          } " +
                        "          as ?hasAESNI " +
                        "        ) " +
                        "      } " +
                        "    } " +
                        "  } " +
                        "} ";

        ResultSet resultSet = getResultSet(query);

        Map<String, CipherResourceDetails> rval = new HashMap<>();

        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();

            String identifier = qs.getLiteral("targetIdentifier").toString();
            boolean hasAESNI = qs.getLiteral("hasAESNI").getBoolean();
            boolean hasUCS = qs.getLiteral("hasUCS").getBoolean();
            rval.put(identifier, new CipherResourceDetails(identifier, hasAESNI, hasUCS));
        }

        return rval;
    }
}
