package com.bbn.marti.service;

import com.bbn.marti.immortals.data.TcpInitializationData;
import com.bbn.marti.remote.CoreConfigInterface;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import org.apache.log4j.Logger;
import org.dom4j.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CoreConfig extends UnicastRemoteObject implements CoreConfigInterface {
    public static final String NETCFG_INPUT_PREFIX = "network.input.";
    public static final String QUEUE_CAPACITY_KEY = "buffer.queue.capacity";
    public static final String QUEUE_PRIORITYLEVELS_KEY = "buffer.queue.priority.levels";
    public static final String PG_URL_KEY = "PG_URL";
    public static final String PG_USERNAME_KEY = "PG_USERNAME";
    public static final String PG_PASSWORD_KEY = "PG_PASSWORD";
    public static final String PG_GEO_TABLE_NAME_KEY = "PG_GEO_TABLE_NAME";
    public static final String PG_IMAGE_TABLE_NAME_KEY = "PG_IMAGE_TABLE_NAME";
    public static final String PG_THUMBNAIL_TABLE_NAME_KEY = "PG_THUMBNAIL_TABLE_NAME";
    public static final String PG_TYPE_TABLE_NAME_KEY = "PG_TYPEDB_NAME_KEY";
    public static final String PG_DB_NAME_KEY = "PG_DB_NAME";
    public static final String PG_COL_NAME_MAPPINGS_KEY = "PG_COL_NAME_MAPPINGS";
//    static final String DEFAULT_CONFIG_FILE = "CoreConfig.xml";
    private static final long serialVersionUID = 1L;
//    public static String CONFIG_FILE = null;
    static CoreConfig instance = null;
    private static Logger log = Logger.getLogger(CoreConfig.class);
    HashMap<String, Object> map = new HashMap<String, Object>();
    LinkedList<CoreConfigChangeListener> listeners = new LinkedList<CoreConfigChangeListener>();

    public ReadTcpConfigurationData getNewReadTcpConfigurationData() {
        return new ReadTcpConfigurationData();
    }

    public static boolean DEBUG = false;
    
    protected CoreConfig() throws RemoteException {
    }

    public static CoreConfig getInstance() {
        if (instance == null) {
            try {
                instance = new CoreConfig();
                instance.init();
            } catch (RemoteException e) {
                // idk how this could possibly happen...
                e.printStackTrace();
            }
        }
        return instance;
    }

    /**
     * @return a COPY of the map of all current settings.
     */
    @Override
    public Map<String, Object> getAttributeMap() throws RemoteException {
        HashMap<String, Object> copy = null;
        synchronized (map) {
            copy = new HashMap<String, Object>(map);
        }
        return copy;
    }

    /**
     * Initializer for the PriorityQueueMonitor configuration
     */
    private void initPriorityQueueMonitor() {
        log.debug("Initializing CoreConfig for PriorityQueueMonitor...");

        // Queue capacity values in CoreConfig
        // BaseAttributeUpdateCallback queueCallback = new
        // BaseAttributeUpdateCallback()
        // { @Override
        // public void update(Map<String, Object> updatedAttributes)
        // {
        // log.debug(QUEUE_CAPACITY_KEY + " callback triggered");
        //
        // // Get the max capacity from CoreConfig
        // int defaultQueueCapacity = -1;
        // if(getAttribute(QUEUE_CAPACITY_KEY) instanceof Integer)
        // defaultQueueCapacity = (Integer) getAttribute(QUEUE_CAPACITY_KEY);
        // if(getAttribute(QUEUE_CAPACITY_KEY) instanceof Double)
        // defaultQueueCapacity = ((Double)
        // getAttribute(QUEUE_CAPACITY_KEY)).intValue();
        // log.debug("Maximum system queue capacity = " + defaultQueueCapacity);
        //
        // // Set the queue capacity via PriorityQueueMonitor
        // PriorityQueueMonitor.instance().setQueueCapacity(defaultQueueCapacity);
        // }
        // };
        // // Add callback
        // queueCallback.setAttributesOfInterest(Arrays.asList(QUEUE_CAPACITY_KEY));
        // addUpdateCallback(queueCallback);
        // queueCallback.update(null);
    }

    void init() {
        try {
            InputStream is = CoreConfig.class.getResourceAsStream("/CoreConfig.xml");
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            is.close();

            Document doc = DocumentHelper.parseText(new String(bytes));
            @SuppressWarnings("unchecked")
            List<Element> elemList = doc.getRootElement().elements();
            for (Element e : elemList) {
                processConfig("", e);
            }
            // processConfig("", doc.getRootElement());
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Could not load CoreConfig from resources!");
        }

        // Initialize the PriorityQueueMonitor
        // (this is a strange place to do it, but it needs to be done somewhere)
        initPriorityQueueMonitor();

    }

    // recurse down the xml tree and extract configuration elements
    // <config><network port=8050> would translate to a property name
    // config.network.port
    // with a value of 8050
    void processConfig(String prefix, Element parent) {
        String newName = prefix + parent.getName();
        if (!parent.getText().trim().isEmpty()) {
            this.setAttribute(newName, parent.getText());
            log.debug("CoreConfig property set: " + newName + ": "
                    + parent.getText());
        }

        if (parent.attributeValue("_name") != null) {
            newName += "." + parent.attributeValue("_name");
            parent.remove(parent.attribute("_name"));
        }

        @SuppressWarnings("unchecked")
        List<Attribute> attrList = parent.attributes();
        for (Attribute a : attrList) {
            String attrName = newName + "." + a.getName();
            if (a.getNodeType() != Element.ELEMENT_NODE) {
                Object o;
                Integer integer = null;
                try {
                    integer = Integer.parseInt(a.getText());
                } catch (Exception e) {
                }
                if (integer != null) {
                    o = integer;
                } else if (a.getText().compareToIgnoreCase("true") == 0
                        || a.getText().compareToIgnoreCase("false") == 0) {
                    o = Boolean.parseBoolean(a.getText());
                } else {
                    o = a.getText();
                }
                this.setAttribute(attrName, o);
                log.debug("CoreConfig property set: " + attrName + ": "
                        + a.getText());
            }

        }

        @SuppressWarnings("unchecked")
        List<Element> elemList = parent.elements();
        for (Element e : elemList) {
            processConfig(newName + ".", e);
        }

    }

    public void setAttribute(String attrName, Object v) {
        synchronized (map) {
            map.put(attrName, v);
        }
    }

    @Override
    public void setAttributeRemote(String attrName, Object v) throws RemoteException {
        setAttribute(attrName, v);
    }

    @Override
    public Object getAttributeRemote(String attrName) throws RemoteException {
        return getAttribute(attrName);
    }

    public String getAttributeString(String key, String defaultVal) {
        return getAttributeWithDefault(key, defaultVal);
    }

    public String getAttributeString(String key) {
        return getAttributeOfType(String.class, key);
    }

    public Integer getAttributeInteger(String key, Integer defaultVal) {
        return getAttributeWithDefault(key, defaultVal);
    }

    public Integer getAttributeInteger(String key) {
        return getAttributeOfType(Integer.class, key);
    }

    public <T> T getAttributeWithDefault(String key, T defaultVal) {
        T value = (T) getAttribute(key);

        if (value == null)
            value = defaultVal;

        return value;
    }

    public Boolean getAttributeBoolean(String key, Boolean defaultVal) {
        return getAttributeWithDefault(key, defaultVal);
    }

    public <T> T getAttributeOfType(Class<T> clazz, String key) {
        return (T) getAttribute(key);
    }

    public Boolean getAttributeBoolean(String key) {
        return getAttributeOfType(Boolean.class, key);
    }

    public Object getAttribute(String key) {
        synchronized (map) {
            return map.get(key);
        }
    }

    public Object removeAttribute(String key) {
        synchronized (map) {
            return map.remove(key);
        }
    }

    public List<String> getKeyListPrefixMatch(String prefix) {
        LinkedList<String> rval = new LinkedList<String>();
        synchronized (map) {
            for (String i : map.keySet()) {
                if (i.startsWith(prefix)) {
                    String v = i.substring(prefix.length(),
                            i.indexOf('.', prefix.length()));
                    // log.debug("looking for key: " + v + "; source string: " +
                    // i.substring(prefix.length()+1));
                    if (!rval.contains(v))
                        rval.push(v);
                }
            }
        }
        return rval;
    }

    public interface CoreConfigChangeListener {
        void callback(String key, Object newValue);
    }

    public class ReadTcpConfigurationData implements ConsumingPipe<Void> {

        private ConsumingPipe<TcpInitializationData> next;

        public void setNext(ConsumingPipe<TcpInitializationData> next) {
            this.next = next;
        }

        @Override
        public void consume(Void aVoid) {
            for (String i : getKeyListPrefixMatch(NETCFG_INPUT_PREFIX)) {
                log.info("Configuring network input: " + i);
                try {
                    String fullName = NETCFG_INPUT_PREFIX + i;
                    final String protocolName = getAttributeString(fullName
                            + ".protocol");
                    int port = getAttributeInteger(fullName + ".port");

                    if (protocolName.toLowerCase().equals("stcp")) {
                        TcpInitializationData initData = new TcpInitializationData(i, protocolName, InetAddress.getLocalHost().getLocalHost(), port);
                        next.consume(initData);
                    }

                } catch (UnknownHostException e) {
                    log.error("Configuration or setup of network input " + i
                            + " failed!");
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void flushPipe() {
            next.flushPipe();

        }

        @Override
        public void closePipe() {
            next.closePipe();
        }
    }

}
