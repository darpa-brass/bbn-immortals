package mil.darpa.immortals.core.das.sparql.adaptationtargets;

import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetInterface;
import mil.darpa.immortals.core.das.sparql.SparqlQuery;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 3/28/18.
 */
public class DetermineProvidedFunctionalAspects extends SparqlQuery {
    public static Set<String> select(DasAdaptationContext dac, @Nonnull String fullArtifactCoordinates) {
        String[] coordinatesSplit = fullArtifactCoordinates.split(":");
        String publishGroupId = coordinatesSplit[0];
        String publishArtifactId = coordinatesSplit[1];
        String publishVersion = coordinatesSplit[2];
        return select(dac, publishGroupId, publishArtifactId, publishVersion);
    }

    public static Set<String> select(DasAdaptationContext dac, @Nonnull String publishGroupId, @Nonnull String publishArtifactId, @Nonnull String publishVersion) {
        Set<String> providedFunctionality = new HashSet<>();

        String query =
                "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
                        "prefix IMMoRTALS_mil_darpa_immortals_ontology: <http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#> " +
                        "prefix IMMoRTALS_java_project: <http://darpa.mil/immortals/ontology/r2.0.0/java/project#> " +
                        "prefix IMMoRTALS_gmei: <http://darpa.mil/immortals/ontology/r2.0.0/gmei#>  " +
                        "prefix IMMoRTALS_resources: <http://darpa.mil/immortals/ontology/r2.0.0/resources#> " +
                        "prefix IMMoRTALS_java_testing_instance: <http://darpa.mil/immortals/ontology/r2.0.0/java/testing/instance#> " +
                        "SELECT ?functionalAspect " +
                        "WHERE { " +
                        "    GRAPH <" + dac.getKnowldgeUri() + "> { " +
                        "    ?javaProject a IMMoRTALS_java_project:JavaProject . " +
                        "    ?javaProject IMMoRTALS:hasCoordinate ?aCoordinate . " +
                        "    ?aCoordinate IMMoRTALS:hasGroupId  \"" + publishGroupId + "\" . " +
                        "    ?aCoordinate IMMoRTALS:hasArtifactId \"" + publishArtifactId + "\" . " +
                        "    ?aCoordinate IMMoRTALS:hasVersion \"" + publishVersion + "\" . " +
                        "    ?javaProject IMMoRTALS:hasCompiledSourceHash ?aCompiledSourceHash . " +
                        "    ?aDfuInstance IMMoRTALS:hasClassPointer ?aCompiledSourceHash . " +
                        "    ?aDfuInstance IMMoRTALS:hasFunctionalAspects ?aFunctionalAspectInstance . " +
                        "    ?aFunctionalAspectInstance IMMoRTALS:hasAbstractAspect ?functionalAspect . " +
                        "  }  " +
                        "} ";


        ResultSet resultSet = getResultSet(query);

        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            providedFunctionality.add(qs.getResource("functionalAspect").toString());
        }
        return providedFunctionality;
    }

    public static Set<String> select(DasAdaptationContext dac, AdaptationTargetInterface adaptationTarget) {
        return select(dac, adaptationTarget.getPublishGroupId(), adaptationTarget.getPublishArtifactId(), adaptationTarget.getPublishVersion());
    }
}
