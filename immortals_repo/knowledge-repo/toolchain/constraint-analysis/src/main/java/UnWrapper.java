import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.property.Property;

import java.util.List;

public class UnWrapper {

    private List<Property> propertiesToBeRemoved;

    private Class<? extends DataType> observedDataType;

    public List<Property> getPropertiesToBeRemoved() {
        return propertiesToBeRemoved;
    }

    public void setPropertiesToBeRemoved(List<Property> propertiesToBeRemoved) {
        this.propertiesToBeRemoved = propertiesToBeRemoved;
    }

    public Class<? extends DataType> getObservedDataType() {
        return observedDataType;
    }

    public void setObservedDataType(Class<? extends DataType> observedDataType) {
        this.observedDataType = observedDataType;
    }
}
