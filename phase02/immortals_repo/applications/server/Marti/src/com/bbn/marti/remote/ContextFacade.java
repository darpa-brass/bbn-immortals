package com.bbn.marti.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;

/**
 * Abstraction to simplify getting data from the Java Naming and Directory Interface (JNDI).
 * This class takes care of the complexities of JNDI lookup for you: mainly exception handling
 * and safe logging of errors.
 * <p>
 * To use it, instantiate a ContextFacade, passing it the javax.naming.Context interface you
 * want to query. Then use the lookup methods to safely obtain values from the underlying Context.
 * <p>
 * Example:
 * <p>
 * <code>javax.naming.Context ctx = new InitialContext();</code>
 * <code>ContextFacade facade = new ContextFacade(ctx);</code>
 * <code>String foo = facade.getString("foo", "default");</code>
 *
 * @author agronosk
 */
public class ContextFacade {
    protected static final Logger logger = LoggerFactory.getLogger(ContextFacade.class);

    private final Context ctx;

    public ContextFacade(Context ctx) {
        if (ctx == null) {
            logger.warn("Constructing a ContextFacade for a null Context. This won't end well.");
        }
        this.ctx = ctx;
    }

    /**
     * Gets the value of a named integer from the underlying Context, without throwing exceptions.
     *
     * @param name         Name of the Integer you want to look up. Caller is responsible for ensuring it is safe for logs.
     * @param defaultValue Value you want if the lookup is not successful; <code>null</code> is acceptable.
     * @return the value of the named Integer, or <code>defaultValue</code> if the lookup failed for any reason
     */
    public Integer lookupInteger(String name, Integer defaultValue) {
        Integer intValue = null;
        Object rawValue = null;
        try {
            rawValue = this.lookup(name);
            intValue = (Integer) rawValue;
        } catch (ClassCastException ex) {
            logger.error("Failed to cast JNDI reference to Integer", ex);
        }

        if (intValue == null) {
            intValue = defaultValue;
        }
        return intValue;
    }

    /**
     * Gets the value of a named String from the underlying Context, without throwing exceptions.
     *
     * @param name         Name of the String you want to look up. Caller is responsible for ensuring it is safe for logs.
     * @param defaultValue Value you want if the lookup is not successful; <code>null</code> is acceptable.
     * @return the value of the named String, or <code>defaultValue</code> if the lookup failed for any reason
     */
    public String lookupString(String name, String defaultValue) {
        String stringValue = null;
        Object rawValue = null;
        try {
            rawValue = this.lookup(name);
            stringValue = (String) rawValue;
        } catch (ClassCastException ex) {
            logger.error("Failed to cast JNDI reference to String", ex);
            stringValue = null;
        }

        if (stringValue == null) {
            stringValue = defaultValue;
        }
        return stringValue;
    }

    /**
     * Private method that encapsulates the common behavior (and exception handling) of the actual JNDI lookup.
     *
     * @param name name of the resource.  Caller is responsible for ensuring it is safe for logs.
     * @return value of the resource, or <code>null<code> if the lookup was unsuccessful
     */
    private Object lookup(String name) {
        Object value = null;
        try {
            value = this.ctx.lookup(name);
        } catch (NamingException e) {
            logger.warn("Failed to look up JNDI name " + name);
        }

        return value;
    }

}
