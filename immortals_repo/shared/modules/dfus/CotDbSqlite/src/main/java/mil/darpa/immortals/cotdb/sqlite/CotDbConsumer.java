package mil.darpa.immortals.cotdb.sqlite;

import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import mil.darpa.immortals.datatypes.cot.Event;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by awellman@bbn.com on 8/7/17.
 */
public class CotDbConsumer implements ConsumingPipe<Event> {
    
    private static CotDB cotDb;
    
    public CotDbConsumer() {
        if (cotDb == null) {
            CotDB db = cotDb.getInstance();
            if (db == null) {
                try {
                    db = CotDB.initializeInstance();
                    cotDb = db;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    @Override
    public void consume(Event input) {
        try {
            cotDb.insertEvent(input);
        } catch (SQLException e) {
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
