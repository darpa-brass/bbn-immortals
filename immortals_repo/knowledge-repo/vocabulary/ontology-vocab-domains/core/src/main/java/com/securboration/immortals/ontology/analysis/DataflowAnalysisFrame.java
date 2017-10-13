package com.securboration.immortals.ontology.analysis;

import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.property.Property;

/**
 * An analysis frame is a recursive structure useful for analyzing dataflow. It
 * abstracts data and its properties in a manner amenable to analysis.
 * <p>
 * A decent analogy of this construct are IP packets, which contain header
 * metadata (analogous to properties) and a payload (analogous to the abstract
 * datatype). Much in the same way that an IP packet may contain a TCP datagram,
 * an analysis frame may describe a compressed and then encrypted image.
 * 
 * @author Securboration
 *
 */
public class DataflowAnalysisFrame {
	
	/**
	 * The child of an analysis frame, if any
	 */
	private DataflowAnalysisFrame analysisFrameChild;
	
	/**
	 * The datatype of a specific analysis frame
	 */
	private Class<? extends DataType> analysisFrameDataType;
	
	/**
	 * The properties of a specific analysis frame
	 */
	private Property[] frameProperties;

	public DataflowAnalysisFrame getAnalysisFrameChild() {
		return analysisFrameChild;
	}

	public void setAnalysisFrameChild(DataflowAnalysisFrame analysisFrameChild) {
		this.analysisFrameChild = analysisFrameChild;
	}

	public Class<? extends DataType> getAnalysisFrameDataType() {
		return analysisFrameDataType;
	}

	public void setAnalysisFrameDataType(Class<? extends DataType> analysisFrameDataType) {
		this.analysisFrameDataType = analysisFrameDataType;
	}

	public Property[] getFrameProperties() {
		return frameProperties;
	}

	public void setFrameProperties(Property[] frameProperties) {
		this.frameProperties = frameProperties;
	}
	
	

}
