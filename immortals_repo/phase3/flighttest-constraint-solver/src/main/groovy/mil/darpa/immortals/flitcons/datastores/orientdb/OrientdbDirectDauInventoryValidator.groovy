package mil.darpa.immortals.flitcons.datastores.orientdb

import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.Edge
import com.tinkerpop.blueprints.Graph
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.OrientVertex
import mil.darpa.immortals.flitcons.Configuration
import mil.darpa.immortals.flitcons.QueryHelper
import mil.darpa.immortals.flitcons.datastores.LogData
import mil.darpa.immortals.flitcons.datastores.LogDataType
import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.annotation.Nonnull

class OrientdbDirectDauInventoryValidator extends QueryHelper {

	private static Logger logger = LoggerFactory.getLogger(OrientdbDirectDauInventoryValidator.class);
	private final List<LogData> resultLog = new LinkedList<>()

	OrientdbDirectDauInventoryValidator() {
		super()
	}

	private void validatePropsExist(Vertex ov, List<String> parentStack, String... requiredProperties) {

		List<String> infoStrings = new LinkedList<>()
		List<String> errorStrings = new LinkedList<>()

		for (String requiredProperty : requiredProperties) {
			if (ov.properties.containsKey(requiredProperty)) {
				String value = ov.properties.get(requiredProperty)
				infoStrings.add("Contains property '" + requiredProperty + "' with value of '" + value + "'")
			} else {
				errorStrings.add("MISSING PROPERTY '" + requiredProperty + "'")
			}
		}

		Collections.sort(infoStrings)
		Collections.sort(errorStrings)
		for (String infoString : infoStrings) {
			resultLog.add(new LogData(LogDataType.INFO, parentStack, infoString))
		}
		for (String errorString : errorStrings) {
			resultLog.add(new LogData(LogDataType.ERROR, parentStack, errorString))
		}
	}

	private void validateChildPropsExist(OrientVertex ov, List<String> parentStack, String childClassName, boolean errorOnNoChildClass, String... requiredProperties) {
		List<Edge> edges = ov.getEdges(Direction.IN).toList()
		for (Edge e : edges) {
			Vertex eov = e.getVertex(Direction.OUT)
			if (eov.getProperty("@class") == childClassName) {
				List<String> parentStack2 = new LinkedList<>(parentStack)
				parentStack2.add(eov.toString())
				validatePropsExist(eov, parentStack2, requiredProperties)
				return
			}
		}
		if (errorOnNoChildClass) {
			resultLog.add(new LogData(LogDataType.ERROR, parentStack, "Does not have a child of type '" + childClassName + "'!"))
		}
	}

	private boolean validateKnownGenericPropertyValue(List<String> parentStack, OrientVertex ov, String propertyName, Set<String> supportedValues, Set<String> ignoredValues) {
		String propertyValue = null
		Vertex genericParameter = null

		List<Edge> edges = ov.getEdges(Direction.IN).toList()
		for (Edge e : edges) {
			Vertex eov = e.getVertex(Direction.OUT)
			if (eov.getProperty("@class") == "GenericParameter") {
				genericParameter = eov
				propertyValue = eov.properties.get(propertyName)
				break
			}
		}

		if (genericParameter == null) {
			resultLog.add(new LogData(LogDataType.ERROR, parentStack, "Does not have a child of type 'GenericParameter'!"))
			return true
		} else {
			parentStack = new LinkedList<>(parentStack)
			parentStack.add(genericParameter.toString())

			if (propertyValue == null) {
				resultLog.add(new LogData(LogDataType.ERROR, parentStack, "MISSING PROPERTY '" + propertyName + "'!"))
				return true

			} else if (supportedValues.contains(propertyValue)) {
				resultLog.add(new LogData(LogDataType.INFO, parentStack, "Contains property '" + propertyName + "' with supported value of '" + propertyValue + "'"))
				return true

			} else if (ignoredValues.contains(propertyValue)) {
				resultLog.add(new LogData(LogDataType.INFO, parentStack, "Contains property '" + propertyName + "' with ignored value of '" + propertyValue + "'"))
				return false

			} else {
				resultLog.add(new LogData(LogDataType.ERROR, parentStack, "Contains property '" + propertyName + "' with unexpected value of '" + propertyValue + "'"))
				return false
			}
		}
	}

	List<LogData> validateDauInventory(@Nonnull File resultsFile) {
		logger.info("Validating Dau Inventory within OrientDB...")

		Configuration config = Configuration.instance
		Set<String> supportedDauTypes = config.supported.dauTypes
		Set<String> ignoredDauTypes = config.ignored.dauTypes

		Set<String> supportedModuleFunctionalityTypes = config.supported.moduleFunctionalityTypes
		Set<String> ignoredModuleFunctionalityTypes = config.ignored.moduleFunctionalityTypes
		Set<String> supportedPortFunctionalityTypes = config.supported.portFunctionalityTypes
		Set<String> ignoredPortFunctionalityTypes = config.ignored.portFunctionalityTypes

		Map<String, Set<List<String>>> dauManufacturerModelMap = new HashMap<>()

		String currentDauIdentifier
		String currentModuleIdentifier
		String currentPortIdentifier

		List<String> stack = new LinkedList<>()

		Graph g = testFlightConfigurationGraph

		g.V.getInventoryDaus.as("daus").sideEffect { OrientVertex it ->
			if (it.properties.containsKey("Manufacturer") && it.properties.containsKey("Model")) {
				currentDauIdentifier = it.toString() + ' (Manufacturer="' + it.properties.get("Manufacturer") + '", Model="' + it.properties.get("Model") + '")'
			} else {
				currentDauIdentifier = it.toString()
			}

			stack = new LinkedList<>()
			stack.add(currentDauIdentifier)

			validatePropsExist(it, stack, "Manufacturer", "Model")

			String manufacturerModel = "('" + it.properties.get("Manufacturer") + "', '" + it.properties.get("Model") + "')"

			if (dauManufacturerModelMap.containsKey(manufacturerModel)) {
				Set<List<String>> conflictingStacks = dauManufacturerModelMap.get(manufacturerModel)

				String errorMsg = "Multiple DAUs found with the manufacturer and model pair " + manufacturerModel + "!"
				resultLog.add(new LogData(LogDataType.ERROR, stack, errorMsg))

				// If only one exists, it means this is the first conflict, so also add an error message for that
				// initial one that wasn't aware of a conflict
				if (conflictingStacks.size() == 1) {
					resultLog.add(new LogData(LogDataType.ERROR, conflictingStacks[0], errorMsg))
				}
				conflictingStacks.add(stack)

			} else {
				Set<List<String>> stacks = new HashSet<>()
				dauManufacturerModelMap.put(manufacturerModel, stacks)
				stacks.add(stack)
			}

			String dauClass = it.getProperty("@class")
			if (supportedDauTypes.contains(dauClass)) {

				resultLog.add(new LogData(LogDataType.INFO, stack, "Is supported type '" + dauClass + "'"))

			} else if (ignoredDauTypes.contains(dauClass)) {
				resultLog.add(new LogData(LogDataType.INFO, stack, "Is ignored type '" + dauClass + "'"))

			} else {
				resultLog.add(new LogData(LogDataType.ERROR, stack, "Is unknown type '" + dauClass + "'"))
			}

			validateChildPropsExist(it, stack, "GenericParameter", true, "BBNDauMonetaryCost", "BBNDauOpportunityCost")
		}

		.get("InternalStructure").get("Modules").get("Module").as("modules")
				.sideEffect {
			if (it.properties.containsKey("Manufacturer") && it.properties.containsKey("Model")) {
				currentModuleIdentifier = it.toString() + ' (Manufacturer="' + it.properties.get("Manufacturer") + '", Model="' + it.properties.get("Model") + '")'
			} else {
				currentModuleIdentifier = it.toString()
			}
			stack = new LinkedList<>()
			stack.add(currentDauIdentifier)
			stack.add(currentModuleIdentifier)
		}

		.filter { OrientVertex it ->
			return validateKnownGenericPropertyValue(stack, it, "BBNModuleFunctionality", supportedModuleFunctionalityTypes, ignoredModuleFunctionalityTypes)
		}

		.sideEffect { OrientVertex it ->
			validatePropsExist(it, stack, "Manufacturer", "Model")
		}

		.get("Ports").get("Port").as("ports").filter { OrientVertex it ->
			return validateKnownGenericPropertyValue([currentDauIdentifier, currentModuleIdentifier, it.toString()], it, "BBNPortFunctionality", supportedPortFunctionalityTypes, ignoredPortFunctionalityTypes)

		}
		.sideEffect { OrientVertex it ->
			if (it.properties.containsKey("Manufacturer") && it.properties.containsKey("Model")) {
				currentPortIdentifier = it.toString() + ' (Manufacturer="' + it.properties.get("Manufacturer") + '", Model="' + it.properties.get("Model") + '")'
			} else {
				currentPortIdentifier = it.toString()
			}

			stack = new LinkedList<>()
			stack.add(currentDauIdentifier)
			stack.add(currentModuleIdentifier)
			stack.add(currentPortIdentifier)

			validateChildPropsExist(it, stack, "GenericParameter", false, "DataLength", "SampleRate")
		}

		.enablePath.iterate()

		FileUtils.writeLines(resultsFile, LogData.createDisplayableLogData(resultLog))

		if (resultLog.any { it.logType == LogDataType.ERROR }) {
			logger.error("ERRORS DETECTED IN ORIENTDB DAU INVENTORY!!!\nPlease examine the file '" + resultsFile + "' for more details, specifically the lines prefixed with an 'E' for errors!");
			List<String> displayLines = LogData.createDisplayableLogData(resultLog)
			for (l in displayLines) {
				println(l)
			}
		} else {
			println("NO ERRORS DETECTED IN ORIENTDB DAU INVENTORY.")
		}

		return resultLog
	}
}
