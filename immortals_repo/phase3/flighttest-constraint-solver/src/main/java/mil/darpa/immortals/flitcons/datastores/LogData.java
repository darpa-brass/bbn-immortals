package mil.darpa.immortals.flitcons.datastores;

import mil.darpa.immortals.flitcons.Utils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;

public class LogData {

	public final LogDataType logType;
	final LinkedList<String> parentStack;
	public final String message;

	public LogData(@Nonnull LogDataType logType, List<String> parentStack, @Nonnull String message) {
		this.logType = logType;
		this.parentStack = new LinkedList<>(parentStack);
		this.message = message;
	}

	public static List<String> createDisplayableLogData(@Nonnull List<LogData> logDataList) {
		List<String> rval = new LinkedList<>();
		logDataList = createSortedLogDataList(logDataList);

		List<String> currentParentList = new LinkedList<>();

		for (LogData logData : logDataList) {
			int maxIdx = logData.parentStack.size() - 1;

			if (currentParentList.size() >= maxIdx + 1) {
				currentParentList = currentParentList.subList(0, maxIdx + 1);
			}

			for (int i = 0; i < logData.parentStack.size(); i++) {
				if (currentParentList.size() < i + 1) {
					rval.add(Utils.repeat("\t", i) + logData.parentStack.get(i));
					currentParentList.add(logData.parentStack.get(i));
				} else {
					if (!currentParentList.get(i).equals(logData.parentStack.get(i))) {
						rval.add(Utils.repeat("\t", i) + logData.parentStack.get(i));
						currentParentList.set(i, logData.parentStack.get(i));
					}
				}
			}

			rval.add((logData.logType == LogDataType.ERROR ? "E" : "") + Utils.repeat("\t", maxIdx + 1) + logData.message);
		}
		return rval;
	}

	public static List<LogData> createSortedLogDataList(@Nonnull List<LogData> logDataList) {
		List<LogData> sortedLogDataList = new LinkedList<>();

		TreeMap<String, TreeSet<String>> pathMessageList = new TreeMap<>();
		HashMap<String, LogData> logDataMap = new HashMap<>();

		for (LogData logData : logDataList) {
			String pathString = String.join(".", logData.parentStack);
			TreeSet<String> messageSet = pathMessageList.computeIfAbsent(pathString, k -> new TreeSet<>());
			messageSet.add(logData.message);
			logDataMap.put(pathString + logData.message, logData);
		}

		for (String pathString : pathMessageList.keySet()) {
			for (String message : pathMessageList.get(pathString).descendingSet()) {
				sortedLogDataList.add(logDataMap.get(pathString + message));
			}
		}
		return sortedLogDataList;
	}

	public static void saveLogDataToFile(@Nonnull List<LogData> logData, @Nonnull File outputFile) {

	}

	public boolean hasErrors(@Nonnull List<LogData> logData) {
		return logData.stream().anyMatch(t -> t.logType == LogDataType.ERROR);
	}

}
