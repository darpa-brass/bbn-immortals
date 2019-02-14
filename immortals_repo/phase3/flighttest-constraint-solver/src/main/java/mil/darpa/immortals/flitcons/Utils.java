package mil.darpa.immortals.flitcons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 1/11/18.
 */
public class Utils {

	public static final Gson gson;
	public static final Gson nonHtmlEscapingGson;

	static {
		GsonBuilder builder = new GsonBuilder();
		gson = builder.setPrettyPrinting().create();

		builder = new GsonBuilder();
		nonHtmlEscapingGson = builder.setPrettyPrinting().disableHtmlEscaping().create();
	}

	public static String repeat(String str, int count) {
		return new String(new char[count]).replace("\0", str);
	}

	public static List<String> stripJsonComments(List<String> jsonLines) {
		List<String> rval = new LinkedList<>();

		for (String line : jsonLines) {
			if (!line.trim().startsWith("//")) {
				rval.add(line);
			}
		}
		return rval;
	}
}
