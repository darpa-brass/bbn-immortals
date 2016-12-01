package com.securboration.immortals.ontology.resources;

import com.securboration.immortals.ontology.core.Resource;

/**
 * A resource present on a specific platform
 * 
 * E.g., a library (code dependency)
 *       a platform architecture (embedded linux, windows, ...)
 *       compute resources (CPU, GPU, ...)
 *       an external resource accessed via IO (network, disk, ...)
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A resource present on a specific platform  E.g., a library (code" +
    " dependency) a platform architecture (embedded linux, windows, ...)" +
    " compute resources (CPU, GPU, ...) an external resource accessed via" +
    " IO (network, disk, ...)  @author jstaples ")
public class PlatformResource extends Resource {

}
