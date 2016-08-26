/*******************************************************************************
 * Copyright (c) 2014, All rights reserved, Securboration Inc.
 * 
 * Securboration Inc. 
 * http://www.securboration.com/
 * 1050 W NASA Blvd, Melbourne FL, 32901
 * 
 * The source code or information contained in this file may be used only in 
 * applications directly related to the Robust Software Modeling Tool (RSMT), 
 * ONR contract N00014-14-1-0462.  All other rights reserved by Securboration.
 * 
 * This code is provided "as is" without warranty of any kind, either expressed 
 * or implied, including but not limited to the implied warranties of 
 * merchantability and/or fitness for a particular purpose.
 ******************************************************************************/
package com.securboration.immortals.maven.bytecode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import com.securboration.immortals.maven.PluginContext;

/**
 * When converting a higher-level class representations to a binary format,
 * ASM's default class writer occasionally attempts to load referenced classes
 * using the system classloader to resolve parent/child relationships. This can
 * lead to problems if we try to write a class before loading its parent or if
 * the class being analyzed isn't on the active classpath. Unfortunately, due to
 * the lazy mechanics of the classloading process, this is a common scenario.
 * 
 * This class provides a workaround by extending the ASM class writer to support
 * determining parent/child relationships without explicitly loading a class.
 * Instead of loading an actual Class<?> object from the classloader (which
 * bypasses the agent and opens the door for ClassRedefinition exceptions) and
 * then using reflection methods on that object to determine the hierarchy, this
 * class loads the class as a binary stream resource into an ASM model. From
 * this ASM model we can quickly construct the parent/child relationships.
 * 
 * @author jstaples
 *
 */
public class DynamicClassWriter extends ClassWriter {

    /**
     * Serializes a class abstraction to bytecode
     * 
     * @param cn
     *            the abstraction to serialize
     * @param c
     *            the build context to use
     * @return the serialized bytecode
     */
    public static byte[] getClassBytes(ClassNode cn, PluginContext c) {
        int flags = 0x0;
        if (cn.version > Opcodes.V1_5) {
            // if it's a Java 6 or newer class, frames are mandatory
            // with this flag set, COMPUTE_MAXS happens by default
            flags |= ClassWriter.COMPUTE_FRAMES;
        } else {
            // if it's a Java 5 or newer class, frames are optional but we still
            // need to know the max locals and max stack values
            flags |= ClassWriter.COMPUTE_MAXS;
        }

        ClassWriter cw = new DynamicClassWriter(flags, c);

        cn.accept(cw);

        return cw.toByteArray();
    }

    public DynamicClassWriter(int flags, PluginContext c) {
        super(flags);

        logger = c.getLog();
        classloader = c.getBuildPathClassloader();
    }

    private final ClassLoader classloader;

    private final Log logger;

    /*
     * WARNING: multiple threads may hit this simultaneously
     */
    private Map<String, String> childToParent = new ConcurrentHashMap<>();

    private Object mapLock = new Object();

    private InputStream getClassInputStream(String className) {
        final String resourceName = className + ".class";

        InputStream i = classloader.getResourceAsStream(resourceName);

        if (i != null) {
            return i;
        }

        if (i == null) {
            logger.error(String.format("unable to locate %s\n", className));
        }

        return i;
    }

    private ClassNode getClassNode(String className) {
        byte[] buffer = new byte[1024 * 1024 * 16];// 16MB

        try {
            final InputStream resourceStream = getClassInputStream(className);
            final int length = IOUtils.read(resourceStream, buffer);

            byte[] classBuffer = new byte[length];
            System.arraycopy(buffer, 0, classBuffer, 0, length);

            ClassReader cr = new ClassReader(classBuffer);
            ClassNode cn = new ClassNode();

            cr.accept(cn, 0);

            return cn;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void traverse(String currentClass) {
        if (childToParent.containsKey(currentClass)) {
            return;
        }

        ClassNode cn = getClassNode(currentClass);

        final String parentClass = cn.superName;

        if (parentClass != null) {
            childToParent.put(currentClass, parentClass);

            traverse(parentClass);
        }
    }

    /**
     * 
     * @param baseClassName
     *            the class to start with
     * @return a superclass traversal to Object starting with the indicated base
     *         class
     */
    public List<String> getTraversal(String baseClassName) {
        if (!childToParent.containsKey(baseClassName)) {
            traverse(baseClassName);
        }

        List<String> traversal = new ArrayList<String>();

        traversal.add(baseClassName);

        boolean stop = false;
        while (!stop) {
            String parent = childToParent.get(baseClassName);

            if (parent == null) {
                stop = true;
            } else {
                traversal.add(parent);
                baseClassName = parent;
            }
        }

        return traversal;
    }

    /**
     * 
     * @param type1
     *            internal class name
     * @param type2
     *            internal class name
     * @return the internal name of the class <b>deepest</b> in the class
     *         hierarchy that is a common ancestor of both type1 and type2
     */
    private String getCommonAncestor(final String type1, final String type2) {
        synchronized (mapLock) {
            final String objectName = Type.getInternalName(Object.class);

            if (type1.equals(objectName)) {
                return objectName;
            }

            if (type2.equals(objectName)) {
                return objectName;
            }

            List<String> type1Hierarchy = getTraversal(type1);
            Set<String> type2HierarchySet = new HashSet<>(getTraversal(type2));

            Integer matchIndex = null;

            for (int i = 0; i < type1Hierarchy.size(); i++) {
                String s = type1Hierarchy.get(i);

                if (type2HierarchySet.contains(s)) {
                    matchIndex = i;
                    break;
                }
            }

            if (matchIndex == null) {
                logger.error(String
                        .format("unable to find a common ancestor for %s and %s, "
                                + "assuming Object", type1, type2));

                // no match
                return objectName;
            } else {
                return type1Hierarchy.get(matchIndex);
            }
        }
    }

    @Override
    protected String getCommonSuperClass(final String type1,
            final String type2) {
        return getCommonAncestor(type1, type2);
    }
}
