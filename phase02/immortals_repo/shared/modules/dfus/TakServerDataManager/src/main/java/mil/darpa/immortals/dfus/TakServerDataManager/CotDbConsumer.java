package mil.darpa.immortals.dfus.TakServerDataManager;

import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import mil.darpa.immortals.datatypes.cot.Event;

/**
 * Created by awellman@bbn.com on 8/7/17.
 * Modified by psamouel@bbn.com to ue the new DataManager
 */
public class CotDbConsumer implements ConsumingPipe<Event> {
    
    public CotDbConsumer() {}
    
    @Override
    public void consume(Event input) {
        try {
        		DataManager dm = new DataManager();
            dm.insertEvent(input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void flushPipe() {
        // Nothing to do
    }

    @Override
    public void closePipe() {
        // Nothing to do
    }
}
