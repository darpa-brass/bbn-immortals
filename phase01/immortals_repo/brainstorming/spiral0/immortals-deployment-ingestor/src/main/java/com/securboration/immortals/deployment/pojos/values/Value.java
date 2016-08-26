package com.securboration.immortals.deployment.pojos.values;

import com.securboration.immortals.deployment.pojos.Copyable;

/**
 * A value can be one of three things:
 * 1) a simple type like a String or integer
 * 2) a complex type that recursively points to other values
 * 3) an array of values
 * 
 * @author jstaples
 *
 */
public abstract class Value implements Copyable<Value> {
}
