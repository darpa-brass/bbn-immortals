package mil.darpa.immortals.flitcons;

import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Configuration {

	public final static transient Configuration instance = Utils.gson.fromJson(
			new InputStreamReader(
					Configuration.class.getClassLoader().getResourceAsStream("configuration.json")
			),
			Configuration.class
	);

	/**
	 * Known types of object that will be encountered
	 */
	public class KnownTypes {
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


	/**
	 * {@link KnownTypes} that we are aware of and fully support
	 */
	public KnownTypes supported;


	/**
	 * {@link KnownTypes} We are aware of and are intentionally ignoring since they do not have any impact on
	 * determining and integrating a solution.
	 */
	public KnownTypes ignored;

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
	 * This class pertains to data that must be gathered and handed to the DSL for determining replacement DAUs
	 */
	public class Adaptation {

		/**
		 * Within identified DAUs, any elements that match the key values will have any properties contained in the
		 * corresponding set collected for analysis
		 */
		public Map<String, Set<String>> collectedDauProperties;

		/**
		 * Within noted external nodes of interest, any elements that match the key values will have any properties
		 * contained in the corresponding set collected for analysis
		 */
		public Map<String, Set<String>> indirectCollectedProperties;
	}

	public class DAUInventoryCollection {

		/**
		 * Within identified DAUs, any elements that match the key values will have any properties contained in the
		 * corresponding set collected for analysis
		 */
		public Map<String, Set<String>> collectedDauProperties;
	}

	/**
	 * Rules used to govern trimming down the data collected through the graph for much easier conversion into something
	 * for the DSL.
	 */
	public class DslTransformation {

		/**
		 * Any MDL types in this set will be removed prior to DSL conversion, with their collected properties being
		 * attached to their parent node.
		 */
		public Set<String> transplantPropertiesToParentNode;

		/**
		 * Any MDL types in this set will be removed prior to DSL conversion, with their parent and child becoming
		 * directly connected.
		 */
		public Set<String> shortNodesToParent;

		/**
		 * Any MDL types and their children in this set will be iremoved prior to DSL conversion, thus being "ignored"
		 * by the DSL.
		 */
		public Set<String> ignoreNodes;

		/**
		 * Any MDL Types that should be tagged with a completely unique identifier
		 */
		public Set<String> taggedNodes;
	}

	/**
	 * Data related to preparing for adaptation.
	 */
	public Adaptation adaptation;

	public DAUInventoryCollection dauInventoryCollection;

	/**
	 * Data related to the DSL Transformation
	 */
	public DslTransformation dslTransformation;
}
