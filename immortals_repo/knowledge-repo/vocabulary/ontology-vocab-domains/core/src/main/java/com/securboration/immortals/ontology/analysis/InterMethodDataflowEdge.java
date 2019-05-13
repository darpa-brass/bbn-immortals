package com.securboration.immortals.ontology.analysis;

import java.util.Objects;

public class InterMethodDataflowEdge extends DataflowEdge {

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof InterMethodDataflowEdge)) {
            return false;
        }

        InterMethodDataflowEdge methodEdge = (InterMethodDataflowEdge) o;
        if (getProducer() == null && methodEdge.getProducer() != null) {
            return false;
        }
        if (getConsumer() == null && methodEdge.getConsumer() != null) {
            return false;
        }

        return Objects.equals(getDataTypeCommunicated(), methodEdge.getDataTypeCommunicated()) &&
                Objects.equals(getProducer(), methodEdge.getProducer()) &&
                Objects.equals(getConsumer(), methodEdge.getConsumer());

    }

    @Override
    public int hashCode() {
        return Objects.hash(getDataTypeCommunicated(), getProducer(), getConsumer());
    }

}
