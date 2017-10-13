package com.bbn.marti.dataservices;

import java.util.List;
import java.util.Map;

public class QueryResult {

	private int numberRows;
	private long executionTimeMilliseconds;
	private List<String> columnNames;
	private int truePositives;
	private int falseNegatives;
	private int falsePositives;
	private String hashValue;
	private List<Map<String, String>> results;
	private String finalSQL;
	
	public int getNumberRows() {
		return numberRows;
	}
	
	public void setNumberRows(int numberRows) {
		this.numberRows = numberRows;
	}
	
	public long getExecutionTimeMilliseconds() {
		return executionTimeMilliseconds;
	}
	
	public void setExecutionTimeMilliseconds(long executionTimeMilliseconds) {
		this.executionTimeMilliseconds = executionTimeMilliseconds;
	}
	
	public List<String> getColumnNames() {
		return columnNames;
	}
	
	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}
	
	public int getTruePositives() {
		return truePositives;
	}
	
	public void setTruePositives(int truePositives) {
		this.truePositives = truePositives;
	}
	
	public int getFalseNegatives() {
		return falseNegatives;
	}
	
	public void setFalseNegatives(int falseNegatives) {
		this.falseNegatives = falseNegatives;
	}
	
	public int getFalsePositives() {
		return falsePositives;
	}
	
	public void setFalsePositives(int falsePositives) {
		this.falsePositives = falsePositives;
	}
	
	public String getHashValue() {
		return hashValue;
	}

	public void setHashValue(String hashValue) {
		this.hashValue = hashValue;
	}

	public List<Map<String,String>> getResults() {
		return results;
	}

	public void setResults(List<Map<String,String>> results) {
		this.results = results;
	}

	public String getFinalSQL() {
		return finalSQL;
	}

	public void setFinalSQL(String finalSQL) {
		this.finalSQL = finalSQL;
	}
	
}
