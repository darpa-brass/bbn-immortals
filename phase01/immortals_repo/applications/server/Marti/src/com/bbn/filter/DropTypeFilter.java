/**
 * <b>AFRL provides this Software to you on an "AS IS" basis, without warranty of any kind. AFRL HEREBY EXPRESSLY
 * DISCLAIMS ALL WARRANTIES OR CONDITIONS, EITHER EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. You are solely responsible for
 * determining the appropriateness of using this Software and assume all risks associated with the use of this Software,
 * including but not limited to the risks of program errors, damage to or loss of data, programs or equipment, and
 * unavailability or interruption of operations. </b>
 *
 * @author AFRL Phoenix In-House Development Team
 */

package com.bbn.filter;

import com.bbn.cot.CotEventContainer;
import org.apache.log4j.Logger;

import java.util.HashMap;

public class DropTypeFilter implements Filter<CotEventContainer> {
    private static Logger log = Logger.getLogger(DropTypeFilter.class);
    long threshold = 30 * 1000;
    HashMap<String, Long> seenList = new HashMap<String, Long>();
    private String type;


    public DropTypeFilter(String type) {
        this.type = type;
    }

    @Override
    public CotEventContainer filter(CotEventContainer c) {
        if (type.compareTo(c.getType()) != 0) {
            //log.error("didn't match type");
            return c;
        }
        if (seenList.containsKey(c.getUid())) {
            long ctime = System.currentTimeMillis();
            if ((seenList.get(c.getUid()) + threshold) < ctime) {
                //log.error("it's been 30 sec!");
                seenList.put(c.getUid(), ctime);
                return c;
            } else {
                //log.error("too soon!");
                return null;
            }
        } else {
            //log.error("never seen before");
            seenList.put(c.getUid(), System.currentTimeMillis());
            return null;
        }
    }
}