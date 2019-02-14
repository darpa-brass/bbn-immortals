package mil.darpa.immortals.flitcons.datastores;

import mil.darpa.immortals.flitcons.Configuration;
import mil.darpa.immortals.flitcons.datatypes.HierarchicalData;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.*;

public abstract class DauInventoryValidator {

	private static Logger logger = LoggerFactory.getLogger(DauInventoryValidator.class);

	private static Configuration config = Configuration.instance;
	private static Set<String> supportedDauTypes = config.supported.dauTypes;
	private static Set<String> ignoredDauTypes = config.ignored.dauTypes;

	private static Set<String> supportedModuleFunctionalityTypes = config.supported.moduleFunctionalityTypes;
	private static Set<String> ignoredModuleFunctionalityTypes = config.ignored.moduleFunctionalityTypes;
	private static Set<String> supportedPortFunctionalityTypes = config.supported.portFunctionalityTypes;
	private static Set<String> ignoredPortFunctionalityTypes = config.ignored.portFunctionalityTypes;

	private final List<LogData> resultLog = new LinkedList<>();

	private boolean showFullPath = false;

//	public void setShowFullPath(boolean value) {
//		showFullPath = value;
//	}



	public DauInventoryValidator() {

	}

	public abstract Map<HierarchicalData, Set<HierarchicalData>> getTestInventoryDaus();

	public abstract String getSourceIdentifier();

	public List<LogData> validateDaus(@Nonnull Map<HierarchicalData, Set<HierarchicalData>> dauNodeChildrenMap, @Nonnull File resultsFile) {
		try {
			Map<String, Set<List<String>>> dauManufacturerModelMap = new HashMap<>();

			String currentDauIdentifier;
			String currentModuleIdentifier;
			String currentPortIdentifier;

			List<String> stack;

			for (HierarchicalData dauNode : dauNodeChildrenMap.keySet()) {
				currentDauIdentifier = dauNode.getIdentifier();

				if (showFullPath) {
					stack = dauNode.getPath();
				} else {
					stack = new LinkedList<>();
					stack.add(currentDauIdentifier);
				}

				validatePropsExist(dauNode, stack, "Manufacturer", "Model");

				String manufacturerModel = "('" + dauNode.getAttributes().get("Manufacturer") + "', '" + dauNode.getAttributes().get("Model") + "')";

				if (dauManufacturerModelMap.containsKey(manufacturerModel)) {
					Set<List<String>> conflictingStacks = dauManufacturerModelMap.get(manufacturerModel);

					String errorMsg = "Multiple DAUs found with the manufacturer and model pair " + manufacturerModel + "!";
					resultLog.add(new LogData(LogDataType.ERROR, stack, errorMsg));

					// If only one exists, it means this is the first conflict, so also add an error message for that
					// initial one that wasn't aware of a conflict
					if (conflictingStacks.size() == 1) {
						resultLog.add(new LogData(LogDataType.ERROR, conflictingStacks.iterator().next(), errorMsg));
					}
					conflictingStacks.add(stack);

				} else {
					Set<List<String>> stacks = new HashSet<>();
					dauManufacturerModelMap.put(manufacturerModel, stacks);
					stacks.add(stack);
				}

				String dauClass = dauNode.nodeClass;
				if (supportedDauTypes.contains(dauClass)) {

					resultLog.add(new LogData(LogDataType.INFO, stack, "Is supported type '" + dauClass + "'"));

				} else if (ignoredDauTypes.contains(dauClass)) {
					resultLog.add(new LogData(LogDataType.INFO, stack, "Is ignored type '" + dauClass + "'"));

				} else {
					resultLog.add(new LogData(LogDataType.ERROR, stack, "Is unknown type '" + dauClass + "'"));
				}

				validateGenericPropsExist(dauNode, stack, true, "BBNDauMonetaryCost", "BBNDauOpportunityCost");

				for (HierarchicalData internalStructure : dauNode.getChildNodeMap().get("InternalStructure")) {
					for (HierarchicalData modules : internalStructure.getChildNodeMap().get("Modules")) {
						for (HierarchicalData module : modules.getChildNodeMap().get("Module")) {
							currentModuleIdentifier = module.getIdentifier();

							if (showFullPath) {
								stack = module.getPath();
							} else {
								stack = new LinkedList<>();
								stack.add(currentDauIdentifier);
								stack.add(currentModuleIdentifier);
							}

							if (validateKnownGenericPropertyValue(stack, module, "BBNModuleFunctionality", supportedModuleFunctionalityTypes, ignoredModuleFunctionalityTypes)) {
								validatePropsExist(module, stack, "Manufacturer", "Model");

								for (HierarchicalData ports : module.getChildNodeMap().get("Ports")) {
									for (HierarchicalData port : ports.getChildNodeMap().get("Port")) {
										currentPortIdentifier = port.getIdentifier();

										if (showFullPath) {
											stack = port.getPath();
										} else {
											stack = new LinkedList<>();
											stack.add(currentDauIdentifier);
											stack.add(currentModuleIdentifier);
											stack.add(currentPortIdentifier);
										}

										if (validateKnownGenericPropertyValue(stack, port, "BBNPortFunctionality", supportedPortFunctionalityTypes, ignoredPortFunctionalityTypes)) {
											validateGenericPropsExist(port, stack, false, "DataLength", "SampleRate");
										}
									}
								}
							}
						}
					}
				}
			}


			FileUtils.writeLines(resultsFile, LogData.createDisplayableLogData(resultLog));

//		if (resultLog.any { it.logType == LogDataType.ERROR }) {
//			logger.error("ERRORS DETECTED IN ORIENTDB DAU INVENTORY!!!\nPlease examine the file '" + resultsFile + "' for more details, specifically the lines prefixed with an 'E' for errors!");
//			DauInventoryValidator.printLogDataList(resultLog, true)
//		} else {
//			println("NO ERRORS DETECTED IN ORIENTDB DAU INVENTORY.")
//		}


			if (resultLog.stream().anyMatch(t -> t.logType == LogDataType.ERROR)) {
			logger.error("ERRORS DETECTED IN ORIENTDB DAU INVENTORY!!!\nPlease examine the file '" + resultsFile + "' for more details, specifically the lines prefixed with an 'E' for errors!");
//				System.out.println("################ ERRORS ################");
//				DauInventoryValidator.printLogDataList(resultLog, true);
			} else {
				System.out.println("NO ERRORS DETECTED .");
			}

			return resultLog;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void validatePropsExist(HierarchicalData ov, List<String> parentStack, String... requiredProperties) {
		List<String> infoStrings = new LinkedList<>();
		List<String> errorStrings = new LinkedList<>();

		for (String requiredProperty : requiredProperties) {
			if (ov.getAttributes().containsKey(requiredProperty)) {
				String value = (String) ov.getAttributes().get(requiredProperty);
				infoStrings.add("Contains property '" + requiredProperty + "' with value of '" + value + "'");
			} else {
				errorStrings.add("MISSING PROPERTY '" + requiredProperty + "'");
			}
		}

		Collections.sort(infoStrings);
		Collections.sort(errorStrings);
		for (String infoString : infoStrings) {
			resultLog.add(new LogData(LogDataType.INFO, parentStack, infoString));
		}
		for (String errorString : errorStrings) {
			resultLog.add(new LogData(LogDataType.ERROR, parentStack, errorString));
		}
	}

	private void validateGenericPropsExist(HierarchicalData ov, List<String> parentStack, boolean errorOnNoChildClass, String... requiredProperties) {
		Set<HierarchicalData> childSet = ov.getChildNodeMap().get("GenericParameter");

		if (childSet == null) {
			if (errorOnNoChildClass) {
				resultLog.add(new LogData(LogDataType.ERROR, parentStack, "Does not have a child of type 'GenericParameter'!"));
			}
			return;
		}

		for (HierarchicalData child : childSet) {
			List<String> parentStack2 = new LinkedList<>(parentStack);
			parentStack2.add(child.getIdentifier());
			validatePropsExist(child, parentStack2, requiredProperties);
		}

	}

	private boolean validateKnownGenericPropertyValue(List<String> parentStack, HierarchicalData ov, String propertyName, Set<String> supportedValues, Set<String> ignoredValues) {
		String propertyValue = null;
		HierarchicalData genericParameter = null;

		Set<HierarchicalData> childSet = ov.getChildNodeMap().get("GenericParameter");

		if (childSet != null && childSet.size() > 0) {
			genericParameter = childSet.iterator().next();
			propertyValue = (String) genericParameter.getAttributes().get(propertyName);
		}

		if (genericParameter == null) {
			resultLog.add(new LogData(LogDataType.ERROR, parentStack, "Does not have a child of type 'GenericParameter'!"));
			return true;
		} else {
			parentStack = new LinkedList<>(parentStack);
			parentStack.add(genericParameter.getIdentifier());

			if (propertyValue == null) {
				resultLog.add(new LogData(LogDataType.ERROR, parentStack, "MISSING PROPERTY '" + propertyName + "'!"));
				return true;

			} else if (supportedValues.contains(propertyValue)) {
				resultLog.add(new LogData(LogDataType.INFO, parentStack, "Contains property '" + propertyName + "' with supported value of '" + propertyValue + "'"));
				return true;

			} else if (ignoredValues.contains(propertyValue)) {
				resultLog.add(new LogData(LogDataType.INFO, parentStack, "Contains property '" + propertyName + "' with ignored value of '" + propertyValue + "'"));
				return false;

			} else {
				resultLog.add(new LogData(LogDataType.ERROR, parentStack, "Contains property '" + propertyName + "' with unexpected value of '" + propertyValue + "'"));
				return false;
			}
		}
	}


}
