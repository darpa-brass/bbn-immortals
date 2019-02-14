package mil.darpa.immortals.core.das.sparql.deploymentmodel;

import mil.darpa.immortals.core.das.sparql.SparqlQuery;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 6/28/18.
 */
public class DetermineCrossAppApplicability extends SparqlQuery {

    public static class CrossAppApplicabilityDetails {
        public final String dfuInstance;
        public final String dfuGroupId;
        public final String dfuArtifactId;
        public final String dfuVersion;
        public final Integer keyLength;
        public final String algorithm;
        public final String chainingMode;
        public final String paddingScheme;

        public CrossAppApplicabilityDetails(
                @Nonnull String dfuInstance, @Nonnull String dfuGroupId, @Nonnull String dfuArtifactId, @Nonnull String dfuVersion,
                @Nullable Integer keyLength, @Nullable String algorithm, @Nullable String chainingMode, @Nullable String paddingScheme) {
            this.dfuInstance = dfuInstance;
            this.dfuGroupId = dfuGroupId;
            this.dfuArtifactId = dfuArtifactId;
            this.dfuVersion = dfuVersion;
            this.keyLength = keyLength;
            this.algorithm = algorithm;
            this.chainingMode = chainingMode;
            this.paddingScheme = paddingScheme;
        }
    }


    public static List<CrossAppApplicabilityDetails> select(DasAdaptationContext dac) {
        // TODO: Add adaptationIdentifier

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
                        "PREFIX IMMoRTALS_functionality_alg_encryption: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/alg/encryption#> " +
                        "SELECT ?dfuInstance ?dfuGroupId ?dfuArtifactId ?dfuVersion ?keyLength ?algorithm ?chainingMode ?paddingScheme " +
                        "WHERE { " +
                        "    GRAPH <" + dac.getKnowldgeUri() + "> { " +
                        "    ?configureRequest " +
                        "      a IMMoRTALS_functionality_aspects:AspectConfigureRequest ; " +
                        "      IMMoRTALS:hasCandidateImpls ?dfuInstance ; " +
                        "      IMMoRTALS:hasMinimumConfigurationSolution ?minConfigSoln . " +
                        "    ?dfuInstance IMMoRTALS:hasClassPointer ?dfuClassPointer . " +
                        "    ?dfuProjectInstance " +
                        "      IMMoRTALS:hasCompiledSourceHash ?dfuClassPointer ; " +
                        "      IMMoRTALS:hasCoordinate ?dfuCoordinate . " +
                        "    ?dfuCoordinate " +
                        "      IMMoRTALS:hasArtifactId ?dfuArtifactId ; " +
                        "      IMMoRTALS:hasGroupId ?dfuGroupId ; " +
                        "      IMMoRTALS:hasVersion ?dfuVersion . " +
                        "    OPTIONAL { " +
                        "      ?minConfigSoln IMMoRTALS:hasConfigurationBindings ? ?keyLengthBinding . " +
                        "      ?keyLengthBinding IMMoRTALS:hasSemanticType IMMoRTALS_functionality_alg_encryption:CipherKeyLength ; " +
                        "        IMMoRTALS:hasBinding ?keyLength . " +
                        "    } " +
                        "    OPTIONAL { " +
                        "      ?minConfigSoln IMMoRTALS:hasConfigurationBindings ? ?algorithmBinding . " +
                        "      ?algorithmBinding IMMoRTALS:hasSemanticType IMMoRTALS_functionality_alg_encryption:CipherAlgorithm ; " +
                        "        IMMoRTALS:hasBinding ?algorithm . " +
                        "    } " +
                        "    OPTIONAL { " +
                        "      ?minConfigSoln IMMoRTALS:hasConfigurationBindings ? ?chainingModeBinding . " +
                        "      ?chainingModeBinding IMMoRTALS:hasSemanticType IMMoRTALS_functionality_alg_encryption:CipherChainingMode ; " +
                        "        IMMoRTALS:hasBinding ?chainingMode . " +
                        "    } " +
                        "    OPTIONAL { " +
                        "      ?minConfigSoln IMMoRTALS:hasConfigurationBindings ? ?paddingSchemeBinding . " +
                        "      ?blockSizeBinding IMMoRTALS:hasSemanticType IMMoRTALS_functionality_alg_encryption:PaddingScheme ; " +
                        "        IMMoRTALS:hasBinding ?paddingScheme . " +
                        "    } " +
                        "  } " +
                        "} ";

        ResultSet resultSet = getResultSet(query);

        List<CrossAppApplicabilityDetails> rval = new LinkedList<>();


        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();

            String dfuInstance = qs.getResource("dfuInstance").toString();
            String dfuGroupId = qs.getLiteral("dfuGroupId").toString();
            String dfuArtifactId = qs.getLiteral("dfuArtifactId").toString();
            String dfuVersion = qs.getLiteral("dfuVersion").toString();

            Literal keyLengthLiteral = qs.getLiteral("keyLength");
            Integer keyLength = keyLengthLiteral == null ? null : Integer.parseInt(keyLengthLiteral.toString());

            Literal algorithmLiteral = qs.getLiteral("algorithm");
            String algorithm = algorithmLiteral == null ? null : algorithmLiteral.toString();

            Literal chainingModeLiteral = qs.getLiteral("chainingMode");
            String chainingMode = chainingModeLiteral == null ? null : chainingModeLiteral.getString();

            Literal paddingSchemeLiteral = qs.getLiteral("paddingScheme");
            String paddingScheme = paddingSchemeLiteral == null ? null : paddingSchemeLiteral.toString();

            rval.add(new CrossAppApplicabilityDetails(
                    dfuInstance, dfuGroupId, dfuArtifactId, dfuVersion, keyLength, algorithm, chainingMode, paddingScheme
            ));

        }
        return rval;
    }
}
