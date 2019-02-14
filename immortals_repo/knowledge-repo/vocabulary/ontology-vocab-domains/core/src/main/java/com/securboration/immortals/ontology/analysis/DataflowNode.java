package com.securboration.immortals.ontology.analysis;

import com.securboration.immortals.ontology.core.Resource;

/**
 * A node in a dataflow graph representing a point at which data is
 * communicated. This can, for example, be a method call or a DFU's functional
 * aspect, depending upon the granularity of analysis.
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A node in a dataflow graph representing a point at which data is" +
    " communicated. This can, for example, be a method call or a DFU's" +
    " functional aspect, depending upon the granularity of analysis. " +
    " @author jstaples ")
public class DataflowNode extends DataflowGraphComponent {
    
    /**
     * A template for the resource on which the node exists. E.g.,
     * SoftwareComponent1 contains an invocation of Method9, which is a dataflow
     * node. SoftwareComponent1 resides on some AndroidDevice instance, which is
     * represented here in template form.
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "A template for the resource on which the node exists. E.g.," +
        " SoftwareComponent1 contains an invocation of Method9, which is a" +
        " dataflow node. SoftwareComponent1 resides on some AndroidDevice" +
        " instance, which is represented here in template form.")
    private Resource resourceTemplate;
    
    /**
     * A template for the ecosystem in which the node exists. E.g.,
     * SoftwareComponent1 contains an invocation of Method9, which is a dataflow
     * node. SoftwareComponent1 resides on some AndroidDevice instance, which in
     * turn communicates with some Server instance. Here we represent an
     * ecosystem containing the Server and AndroidDevice templates.
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "A template for the ecosystem in which the node exists. E.g.," +
        " SoftwareComponent1 contains an invocation of Method9, which is a" +
        " dataflow node. SoftwareComponent1 resides on some AndroidDevice" +
        " instance, which in turn communicates with some Server instance." +
        " Here we represent an ecosystem containing the Server and" +
        " AndroidDevice templates.")
    private Resource contextTemplate;

    private Class<? extends Resource> abstractResourceTemplate;

    public Resource getContextTemplate() {
        return contextTemplate;
    }

    public void setContextTemplate(Resource contextTemplate) {
        this.contextTemplate = contextTemplate;
    }

    public Resource getResourceTemplate() {
        return resourceTemplate;
    }

    public void setResourceTemplate(Resource resourceTemplate) {
        this.resourceTemplate = resourceTemplate;
    }


    public Class<? extends Resource> getAbstractResourceTemplate() {
        return abstractResourceTemplate;
    }

    public void setAbstractResourceTemplate(Class<? extends Resource> abstractResourceTemplate) {
        this.abstractResourceTemplate = abstractResourceTemplate;
    }
}
