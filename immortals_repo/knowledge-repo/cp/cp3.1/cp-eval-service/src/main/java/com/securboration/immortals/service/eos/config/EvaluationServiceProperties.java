//package com.securboration.immortals.service.eos.config;
//
//import org.springframework.beans.factory.annotation.Value;
//
//import com.securboration.immortals.service.eos.api.types.EosType;
//
///**
// * Properties needed by IMMoRTALS services.
// * 
// * @see <a href=
// *      "https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html">
// *      spring property configuration documentation</a>
// * 
// * @author jstaples
// *
// */
//public class EvaluationServiceProperties extends EosType {
//    
//    public EvaluationServiceProperties() {
//        super();
//    }
//
//    public EvaluationServiceProperties(String s) {
//        super(s);
//    }
//
//    // keys //
//    public static final String TEMPLATE_PROJECT_DIR = "essTemplateDir";
//    
//    public static final String PATH_TO_INSTRUMENTATION_JAR = "pathToInstrumentationJar";
//    
//    public static final String JAVABASE = "javabase";
//    
//
//    // values //
//    @Value("${" + TEMPLATE_PROJECT_DIR + "}")
//    private String essTemplateDir;
//    
//    @Value("${" + PATH_TO_INSTRUMENTATION_JAR + "}")
//    private String pathToInstrumentationJar;
//    
//    @Value("${" + JAVABASE + ":" + defaultJavabase + "}")
//    private String javabase = defaultJavabase;
//
//
//    // accessors //
//    public String getEssTemplateDir() {
//        return essTemplateDir;
//    }
//
//    
//    public String getPathToInstrumentationJar() {
//        return pathToInstrumentationJar;
//    }
//    
//    
//    
//    
//    
//    //soot needs this to do its thing
//    private static final String defaultJavabase = 
//            "java.io,java.lang,java.lang.annotation,java.lang.invoke,java.lang.module,java.lang.ref,java.lang.reflect,java.math,java.net,java.net.spi,java.nio,java.nio.channels,java.nio.channels.spi,java.nio.charset,java.nio.charset.spi,java.nio.file,java.nio.file.attribute,java.nio.file.spi,java.security,java.security.acl,java.security.cert,java.security.interfaces,java.security.spec,java.text,java.text.spi,java.time,java.time.chrono,java.time.format,java.time.temporal,java.time.zone,java.util,java.util.concurrent,java.util.concurrent.atomic,java.util.concurrent.locks,java.util.function,java.util.jar,java.util.regex,java.util.spi,java.util.stream,java.util.zip,javax.crypto,javax.crypto.interfaces,javax.crypto.spec,javax.net,javax.net.ssl,javax.security.auth,javax.security.auth.callback,javax.security.auth.login,javax.security.auth.spi,javax.security.auth.x500,javax.security.cert";
//
//
//    
//    
//}
