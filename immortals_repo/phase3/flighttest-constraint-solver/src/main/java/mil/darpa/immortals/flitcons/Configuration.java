package mil.darpa.immortals.flitcons;

import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Configuration {

	public static transient Configuration instance;


	public static Configuration getInstance() {
		if (instance == null) {
			instance = Utils.getGson().fromJson(
					new InputStreamReader(
							Configuration.class.getClassLoader().getResourceAsStream("configuration.json")
					),
					Configuration.class);
		}
		return instance;
	}

	/**
	 * Known types of object that will be encountered
	 */
	public static class KnownTypes {
		/**
		 * MDL DAU types we are aware of.
		 */
		public Set<String> dauTypes = new HashSet<>();

		/**
		 * Module functionality types we are aware of.
		 */
		public Set<String> moduleFunctionalityTypes = new HashSet<>();

		/**
		 * Port functionality types we are aware of.
		 */
		public Set<String> portFunctionalityTypes = new HashSet<>();
	}

	public static class Calculation {
		public String targetValueIdentifier;
		public Set<String> substitutionValues;
		public String equation;
	}

	public static class AdaptationConfiguration {
		Map<String, Calculation> calculations;
	}


	/**
	 * This class pertains to data that must be gathered and handed to the DSL for determining replacement DAUs
	 */
	public static class PropertyCollectionInstructions {

		/**
		 * Within identified DAUs, any elements that match the key values will have any properties contained in the
		 * corresponding set collected for analysis
		 */
		public Map<String, Set<String>> collectedPrimaryProperties;

		/**
		 * Within noted external nodes of interest, any elements that match the key values will have any properties
		 * contained in the corresponding set collected for analysis
		 */
		public Map<String, Set<String>> collectedExternalProperties;

		/**
		 * Data that is collected for debugging but will be removed prior to being send to the DSL
		 */
		public Map<String, Set<String>> collectedDebugProperties;

		/**
		 * Item types contained in this list will default to true if they have no value set
		 */
		public Set<String> valuesToDefaultToTrue;
	}

	/**
	 * {@link KnownTypes} that we are aware of and fully support
	 */
	public KnownTypes supported;


	/**
	 * If true, an exception will be raised if we encounter an unexpected MDL DAU type. Otherwise, it will flow
	 * through freely to the DSL
	 */
	public boolean haltOnUnsupportedDauType;

	/**
	 * If true, an exception will be raised if we encounter unexpected module functionality. Otherwise, it will flow
	 * through freely to the DSL
	 */
	public boolean haltOnUnsupportedModuleFunctionality;

	/**
	 * If true, an exception will be raised if we encounter unexpected port functionality. Otherwise, it will flow
	 * through freely to the DSL
	 */
	public boolean haltOnUnsupportedPortFunctionality;


	/**
	 * Rules used to govern trimming down the data collected through the graph for much easier conversion into something
	 * for the DSL.
	 */
	public static class TransformationInstructions {
		/**
		 * Any MDL types in this set will have their attributes and children linked to the parent and then be removed
		 * from the graph
		 */
		public Set<String> shortNodesToParent;

		/**
		 * Any MDL types and their children in this set will be iremoved prior to DSL conversion, thus being "ignored"
		 * by the DSL.
		 */
		public Set<String> ignoreNodes;

		/**
		 * Any parent (key) node types that do not have the corresponding chain of children will be ignored.
		 */
		public Map<String, List<String>> ignoreParentsWithoutProperties;

		/**
		 * Any MDL Types that should be tagged with a completely unique identifier
		 */
		public Set<String> taggedNodes;

		public Map<String, Map<String, Set<String>>> combineSquashedChildNodeAttributes;
	}

	public static class ValidationConfiguration {
		public Map<String, Set<String>> defaultPropertyList;
		public List<String> debugIdentificationValues;
	}

	/**
	 * Data related to preparing for dataCollectionInstructions.
	 */
	public PropertyCollectionInstructions dataCollectionInstructions;

	/**
	 * Data related to the DSL Transformation
	 */
	public TransformationInstructions transformation;

	public ValidationConfiguration validation;

	public AdaptationConfiguration adaptation;
}
