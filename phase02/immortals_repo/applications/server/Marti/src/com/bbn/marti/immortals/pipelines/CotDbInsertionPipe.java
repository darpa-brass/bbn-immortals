package com.bbn.marti.immortals.pipelines;

import com.bbn.cot.CotEventContainer;
import com.bbn.marti.service.MartiMain;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import mil.darpa.immortals.datatypes.cot.CotHelper;
import mil.darpa.immortals.datatypes.cot.Event;
import mil.darpa.immortals.dfus.TakServerDataManager.CotDbConsumer;

import javax.xml.bind.JAXBException;

/**
 * Created by awellman@bbn.com on 4/5/18.
 */
public class CotDbInsertionPipe implements ConsumingPipe<CotEventContainer> {
    
    // TODO: this is hacky
//    public static boolean ignoreDb = false;

    private final ConsumingPipe<CotEventContainer> next;
    private final CotDbConsumer cotDbConsumer;


    public CotDbInsertionPipe(ConsumingPipe<CotEventContainer> next) {
        this.next = next;

        // If the database is enabled, add it as an endpoint
//        if (!ignoreDb && MartiMain.getConfig().postGreSqlConfig.enabled) {
            cotDbConsumer = new CotDbConsumer();
//        } else {
//            cotDbConsumer = null;
//        }
    }

    @Override
    public void consume(CotEventContainer input) {
        if (cotDbConsumer != null) {
            try {
                Event e = CotHelper.unmarshalEvent(input.asXml());
                cotDbConsumer.consume(e);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
        next.consume(input);
    }

    @Override
    public void flushPipe() {

    }

    @Override
    public void closePipe() {

    }
}
