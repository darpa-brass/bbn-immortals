package mil.darpa.immortals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper files to aid in proper (de)serialization of values dependent on other values
 * <p>
 * Specifically, directory structures can be difficult to deal with if you want them to be dynamic and absolute.
 * <p>
 * These methods allow the reading in of override filepaths as static values that can then then be used to initialize
 * instance "default" subpaths to remap entire directory structures with absolute filepaths.
 * <p>
 * See {@link GlobalsConfig} for an example.
 * <p>
 * Created by awellman@bbn.com on 12/21/17.
 */
public class StaticHelper {

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static JsonObject rootJsonConfig;

    static {
        try {
            // Attempt to load an override file if the environment variable points ot one
            String override_path = System.getenv("IMMORTALS_OVERRIDE_FILE");
            rootJsonConfig =
                    override_path == null ? null :
                            gson.fromJson(new FileReader(new File(override_path)), JsonObject.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads value stored in the specified resource filepath as a string
     *
     * @param resourceIdentifier The identifier for the resource (including the leading slash)
     * @return The value of the resource file
     */
    static String readResourceValue(String resourceIdentifier) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    StaticHelper.class.getResourceAsStream(resourceIdentifier)));

            String rval = br.readLine();
            br.close();
            return rval;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Resolves a value given the JSON configuration path (if it exists) and a default value
     *
     * @param valuePath    A period-separated path of the value.
     *                     For example assuming the configuration file may be something like the following:
     *                     {"foo": {"bar": "ed"}}
     *                     To get the potential override value of "bar" the valuePath would be "foo.bar".
     * @param defaultValue The default value if no override has been found
     * @return The resolved value of the field
     */
    static String resolveStringValue(String valuePath, String defaultValue) {
        if (rootJsonConfig == null) {
            return defaultValue;
        }

        JsonObject currentObject = null;
        int idx = 0;

        String[] values = valuePath.split("\\.");

        do {
            if (rootJsonConfig.has(values[idx])) {
                currentObject = rootJsonConfig.getAsJsonObject(values[idx]);
            }

            idx++;
        } while (currentObject != null && (idx + 1) < values.length);

        if (currentObject != null && currentObject.has(values[idx])) {
            return currentObject.get(values[idx]).getAsString();
        } else {
            return defaultValue;
        }
    }

    /**
     * Similar to {@link #resolveStringValue(String, String)}, but throws a {@link RuntimeException} if the determined
     * filepath does not exist
     *
     * @param valuePath       A period-separated path of the value.
     *                        For example assuming the configuration file may be something like the following:
     *                        {"foo": {"bar": "ed"}}
     * @param defaultFilepath The default filepath
     * @return A resolved existing directory
     */
    static Path resolveExistingDirectoryValue(String valuePath, String defaultFilepath) {
        Path p = Paths.get(resolveStringValue(valuePath, defaultFilepath));

        if (!Files.exists(p)) {
            throw new RuntimeException(valuePath + " '" + p.toString() + "' must exist!");
        }
        return p.toAbsolutePath();
    }

    /**
     * Similar to {@link #resolveStringValue(String, String)}, but throws a {@link RuntimeException} if the determined
     * filepath does not exist
     *
     * @param valuePath       A period-separated path of the value.
     *                        For example assuming the configuration file may be something like the following:
     *                        {"foo": {"bar": "ed"}}
     * @param defaultFilepath The default filepath
     * @return A resolved existing directory
     */
    static Path resolveExistingDirectoryValue(String valuePath, Path defaultFilepath) {
        return resolveExistingDirectoryValue(valuePath, defaultFilepath.toAbsolutePath().toString());
    }

    /**
     * Similar to {@link #resolveStringValue(String, String)}, but creates the determined filepath, throwing
     * a {@link RuntimeException} if the file cannot be created
     *
     * @param valuePath       A period-separated path of the value.
     *                        For example assuming the configuration file may be something like the following:
     *                        {"foo": {"bar": "ed"}}
     * @param defaultFilepath The default filepath
     * @return A resolved created directory
     */
    static Path resolveNewDirectoryValue(String valuePath, String defaultFilepath) {
        Path p = Paths.get(resolveStringValue(valuePath, defaultFilepath));

        if (!p.getParent().toFile().exists()) {
            throw new RuntimeException("The parent directory of '" + p.toString() + "' must exist!");
        }

        if (!Files.exists(p)) {
            try {
                Files.createDirectory(p);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return p.toAbsolutePath();
    }

    /**
     * Similar to {@link #resolveStringValue(String, String)}, but creates the determined filepath, throwing
     * a {@link RuntimeException} if the file cannot be created
     *
     * @param valuePath       A period-separated path of the value.
     *                        For example assuming the configuration file may be something like the following:
     *                        {"foo": {"bar": "ed"}}
     * @param defaultFilepath The default filepath
     * @return A resolved created directory
     */
    static Path resolveNewDirectoryValue(String valuePath, Path defaultFilepath) {
        return resolveNewDirectoryValue(valuePath, defaultFilepath.toAbsolutePath().toString());
    }

    /**
     * Shortcut function for making a directory. The parent directory must exist (This is intentional to prevent
     * accidental garbage paths
     *
     * @param dirPath The filepath to create
     * @return The input Path
     */
    static Path mkdir(@Nonnull Path dirPath) {
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectory(dirPath);
                return dirPath;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dirPath.toAbsolutePath();
    }
}
