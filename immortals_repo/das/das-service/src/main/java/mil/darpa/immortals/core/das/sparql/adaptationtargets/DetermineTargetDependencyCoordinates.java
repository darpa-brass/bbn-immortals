package mil.darpa.immortals.core.das.sparql.adaptationtargets;

import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetInterface;
import mil.darpa.immortals.core.das.sparql.SparqlQuery;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import org.apache.jena.query.ResultSet;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class DetermineTargetDependencyCoordinates extends SparqlQuery {

    public static List<String> select(@Nonnull DasAdaptationContext dac, @Nonnull AdaptationTargetInterface buildInstance) {
        return select(dac, buildInstance.getTargetName());
   }

    public static List<String> select(@Nonnull DasAdaptationContext dac, @Nonnull String targetIdentifier) {
        String query = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
                "prefix IMMoRTALS_java_project: <http://darpa.mil/immortals/ontology/r2.0.0/java/project#> " +
                "prefix IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#> " +
                "SELECT (CONCAT(?groupId, \":\", ?artifactId, \":\", ?version) as ?formattedCoord) " +
                "WHERE { " +
                "  GRAPH <" + dac.getKnowldgeUri() + "> { " +
                "    ?project a IMMoRTALS_java_project:JavaProject . " +
                "    ?project IMMoRTALS:hasCoordinate ?projectCoordinate . " +
                "    ?projectCoordinate IMMoRTALS:hasArtifactId \"" + targetIdentifier + "\" . " +
                "    ?project IMMoRTALS:hasClasspaths ?classpaths . " +
                "    ?classpaths IMMoRTALS:hasElementHashValues ?classpathsHash . " +
                "    ?jarArtifact IMMoRTALS:hasHash ?classpathsHash . " +
                "    ?jarArtifact IMMoRTALS:hasCoordinate ?jarCoordinate . " +
                "    ?jarCoordinate a IMMoRTALS_bytecode:BytecodeArtifactCoordinate . " +
                "    ?jarCoordinate IMMoRTALS:hasArtifactId ?artifactId . " +
                "    ?jarCoordinate IMMoRTALS:hasGroupId ?groupId . " +
                "    ?jarCoordinate IMMoRTALS:hasVersion ?version ;" +
                "  } " +
                "}";

        ResultSet resultSet = getResultSet(query);

        List<String> results = new ArrayList<>();
        resultSet.forEachRemaining(t -> results.add(getLiteral(t, "formattedCoord")));

        return results;
    }
}

