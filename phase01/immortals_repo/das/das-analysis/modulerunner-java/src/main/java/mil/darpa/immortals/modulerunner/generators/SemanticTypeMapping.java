package mil.darpa.immortals.modulerunner.generators;

import mil.darpa.immortals.core.Semantics;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by awellman@bbn.com on 6/6/16.
 */
public enum SemanticTypeMapping {
    SerializableDataTargetStream(Semantics.Datatype_SerializableDataTargetStream, "GeneralText", "CotLocationMessage", "CotImageMessage", "ExceptionType", "GeneralLogMessage"),
    SerializableDataSourceStream(Semantics.Datatype_SerializableDataSourceStream, "GeneralText", "CotLocationMessage", "CotImageMessage", "ExceptionType", "GeneralLogMessage"),
    SerializableData(Semantics.Datatype_SerializableData, "GeneralText", "CotLocationMessage", "CotImageMessage", "ExceptionType", "GeneralLogMessage"),
    GeneralText("GeneralText"),
    CotLocationMessage("CotLocationMessage"),
    CotImageMessage("CotImageMessage"),
    ExceptionType("Exception"),
    GeneralLogMessage("GeneralLogMessage");

    private static Map<String, SemanticTypeMapping> uriMap = new HashMap<>();

    private final String uri;

    private List<String> subtypes;

    private SemanticTypeMapping(@Nonnull String uri, @Nonnull String... subtypes) {
        this.uri = uri;
        this.subtypes = Arrays.asList(subtypes);
    }

    @Nullable
    public static synchronized SemanticTypeMapping getByUri(@Nullable String uri, @Nonnull SemanticTypeMapping subtype) {
        return subtype;
//        if (uriMap.isEmpty()) {
//            for (SemanticTypeMapping type : values()) {
//                uriMap.put(type.getUri(), type);
//
//
//            }
//        }
//        SemanticTypeMapping requestedType = uriMap.get(uri);
//
//        if (requestedType == null) {
//            throw new RuntimeException("'" + uri + "' is not a mapped SemanticTypeMapping!!");
//        }
//        if (subtype == null) {
//            if (requestedType.subtypes.isEmpty()) {
//                return requestedType;
//            } else {
//                return valueOf(requestedType.subtypes.get(0));
//            }
//        } else {
//            if (requestedType.subtypes.contains(subtype)) {
//                return subtype;
//            } else {
//                throw new RuntimeException(requestedType + " does not have a valid subtype of " + subtype + "!");
//            }
//        }
    }

    public String getUri() {
        return this.uri;
    }
}
