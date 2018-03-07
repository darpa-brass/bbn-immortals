package com.securboration.immortals.ontology.constraint;

import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.property.Property;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class UnWrapper {
    
    public UnWrapper() {}
    
    public UnWrapper(List<Property> _propertiesToBeRemoved,
                     Class<? extends DataType> observedDataType) {
        this.setPropertiesToBeRemoved(_propertiesToBeRemoved);
        this.setObservedDataType(observedDataType);
    }

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
    
    public static LinkedBlockingQueue<UnWrapper> deepCopy(LinkedBlockingQueue<UnWrapper> unWrappers) throws IllegalAccessException, InstantiationException {
        
        LinkedBlockingQueue<UnWrapper> cloneUnWrappers = new LinkedBlockingQueue<>();
        
        for (UnWrapper unWrapper : unWrappers) {
            List<Property> properties = new LinkedList<>(unWrapper.getPropertiesToBeRemoved());
            Class<? extends DataType> clazz = unWrapper.getObservedDataType().newInstance().getClass();
            
            cloneUnWrappers.add(new UnWrapper(properties,
                    clazz));
        }
        return cloneUnWrappers;
    }
}
