package mil.darpa.immortals.core.das;

import com.google.gson.*;
import mil.darpa.immortals.core.AbstractGsonHelper;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildInstance;

import java.lang.reflect.Type;

/**
 * Created by awellman@bbn.com on 6/18/18.
 */
public class DasGsonHelper extends AbstractGsonHelper {


    public static class AdaptationTargetBuildInstanceSerializer implements JsonSerializer<AdaptationTargetBuildInstance> {

        @Override
        public JsonElement serialize(AdaptationTargetBuildInstance src, Type typeOfSrc, JsonSerializationContext context) {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("deploymentInstanceIdentifier", src.getInstanceIdentifier());
            jsonObject.addProperty("executablePath", src.getExecutablePath().toAbsolutePath().toString());

            jsonObject.addProperty("deploymentTarget", src.getDeploymentTarget().name());
            jsonObject.addProperty("packageIdentifier", src.getExecutionPackageIdentifier());
            jsonObject.addProperty("mainMethod", src.getExecutionMainMethod());
            jsonObject.addProperty("settleTimeMS", src.getExecutionStartSettleTimeMS());

            // TODO: Handle adding deploymentFileMapType, deploymentPath, and validators

            return jsonObject;
        }
    }

    private static Gson gson;

    public synchronized static Gson getGson() {
        if (gson == null) {
            gson = getGsonBuilder()
                    .registerTypeAdapter(AdaptationTargetBuildInstance.class, new AdaptationTargetBuildInstanceSerializer())
                    .create();
        }
        return gson;
    }
}
