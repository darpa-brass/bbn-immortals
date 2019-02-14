package mil.darpa.immortals.das.sourcecomposer;

import com.google.gson.JsonObject;
import mil.darpa.immortals.configuration.GsonHelper;
import mil.darpa.immortals.das.configuration.DfuCompositionConfiguration;
import mil.darpa.immortals.das.configuration.EnvironmentConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Combines the shortcuts made for CP2 ease of implementation with the more detailed information
 * <p>
 * Created by awellman@bbn.com on 10/21/16.
 */
public class Phase1Hacks {

    static DfuCompositionConfiguration constructCP2ControlPointComposition(
            String originalDependencyString, String originalClassPackageIdentifier, String augmenterDependencyString,
            String augmenterClassPackageIdentifier, Map<String, String> parameters, String sessionIdentifier)
            throws CompositionException {
        try {

            InputStream is = EnvironmentConfiguration.class.getResourceAsStream("/CP2CompositionTemplate.json");
            JsonObject configuration = GsonHelper.getInstance().fromInputStream(is, JsonObject.class);

            configuration.getAsJsonObject("originalDfu").getAsJsonObject("consumingPipeSpecification")
                    .addProperty("dependencyString", originalDependencyString);

            configuration.getAsJsonObject("originalDfu").getAsJsonObject("consumingPipeSpecification")
                    .addProperty("classPackage", originalClassPackageIdentifier);


            configuration.getAsJsonArray("dfuCompositionSequence").get(0).getAsJsonObject()
                    .getAsJsonObject("consumingPipeSpecification")
                    .addProperty("dependencyString", originalDependencyString);

            configuration.getAsJsonArray("dfuCompositionSequence").get(0).getAsJsonObject()
                    .getAsJsonObject("consumingPipeSpecification")
                    .addProperty("classPackage", originalClassPackageIdentifier);


            configuration.addProperty("sessionIdentifier", sessionIdentifier);

            HashMap<String, Object> override = new HashMap<>();

            Constructor constructor;

            try {
                constructor = Class.forName(originalClassPackageIdentifier).getConstructors()[0];
            } catch (ClassNotFoundException e) {
                throw new CompositionException.InvalidClaspathSpecifiedException(originalClassPackageIdentifier);
            }
            Parameter[] cParameters = constructor.getParameters();

            for (int i = 0; i < cParameters.length; i++) {

                JsonObject constructorParameter = new JsonObject();
                constructorParameter.addProperty("providedByApplication", true);
                constructorParameter.addProperty("classType", cParameters[i].getParameterizedType().toString());
                configuration.getAsJsonObject("originalDfu")
                        .getAsJsonObject("consumingPipeSpecification")
                        .getAsJsonArray("constructorParameters").add(constructorParameter);

                configuration.getAsJsonArray("dfuCompositionSequence")
                        .get(0).getAsJsonObject().getAsJsonObject("consumingPipeSpecification")
                        .getAsJsonArray("constructorParameters").add(constructorParameter);
            }

            try {
                constructor = Class.forName(augmenterClassPackageIdentifier).getConstructors()[0];
            } catch (ClassNotFoundException e) {
                throw new CompositionException.InvalidClaspathSpecifiedException(augmenterClassPackageIdentifier);
            }
            cParameters = constructor.getParameters();

            configuration.getAsJsonArray("dfuCompositionSequence").get(1)
                    .getAsJsonObject().getAsJsonObject("consumingPipeSpecification")
                    .addProperty("dependencyString", augmenterDependencyString);

            configuration.getAsJsonArray("dfuCompositionSequence").get(1)
                    .getAsJsonObject().getAsJsonObject("consumingPipeSpecification")
                    .addProperty("classPackage", augmenterClassPackageIdentifier);


            for (int i = 0; i < cParameters.length; i++) {
                if (i < cParameters.length - 1) {
                    Parameter p = cParameters[i];
                    try {
                        String annotationUri = (String) p.getAnnotations()[0].getClass().getField("SEMANTIC_URI").get(null);

                        if (!parameters.containsKey(annotationUri)) {
                            throw new CompositionException.SubstitutionUndefinedParameterException("method", p.getName(), annotationUri);
                        }


                        JsonObject constructorParameter = new JsonObject();

                        constructorParameter.addProperty("providedByApplication", false);
                        constructorParameter.addProperty("classType", cParameters[i].getParameterizedType().toString());
                        constructorParameter.addProperty("value", parameters.get(annotationUri));

                        configuration.getAsJsonArray("dfuCompositionSequence").get(1).getAsJsonObject()
                                .getAsJsonObject("consumingPipeSpecification")
                                .getAsJsonArray("constructorParameters").add(constructorParameter);
                    } catch (NoSuchFieldException e) {
                        throw new CompositionException.ConsumingPipeUnannotatedParameterException(constructor.getName());
                    }

                } else {
                    JsonObject constructorParameter = new JsonObject();
                    constructorParameter.addProperty("providedByApplication", true);
                    constructorParameter.addProperty("classType", cParameters[i].getParameterizedType().toString());

                    configuration.getAsJsonArray("dfuCompositionSequence").get(1).getAsJsonObject()
                            .getAsJsonObject("consumingPipeSpecification")
                            .getAsJsonArray("constructorParameters").add(constructorParameter);
                }
            }

            DfuCompositionConfiguration.ShallowDfuCompositionConfiguration sdfu = GsonHelper.getInstance().fromJsonObject(configuration, DfuCompositionConfiguration.ShallowDfuCompositionConfiguration.class);
            return sdfu.toDfuCompositionConfiguration();

        } catch (IOException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public static DfuCompositionConfiguration constructCP2ControlPointComposition(
            String originalDependencyString,
            String originalClassPackageIdentifier,
            String augmenterDependencyString,
            String augmenterClassPackageIdentifier,
            Map<String, String> parameters) throws CompositionException {
        return constructCP2ControlPointComposition(
                originalDependencyString,
                originalClassPackageIdentifier,
                augmenterDependencyString,
                augmenterClassPackageIdentifier,
                parameters,
                "I" + UUID.randomUUID().toString().
                        replaceAll("-", "").substring(0, 12));
    }
}
