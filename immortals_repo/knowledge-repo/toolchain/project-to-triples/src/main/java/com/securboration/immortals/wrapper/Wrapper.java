package com.securboration.immortals.wrapper;

import soot.*;
import soot.dava.internal.javaRep.DIntConstant;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.*;
import soot.jimple.internal.JCaughtExceptionRef;
import soot.jimple.spark.ondemand.pautil.SootUtil;
import soot.util.Chain;
import sun.security.pkcs.SigningCertificateInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Wrapper {
    
    private SootClass wrappedClass;
    private SootClass wrapperClass;
    
    protected Wrapper(SootClass _wrappedClass) {
        wrappedClass = _wrappedClass;
    }
    
    protected void createCipherWrapperConstructor(SootClass newClass, SootField newField, SootClass cipherImplClass,
                                                  SootMethod oldClassConstructor, SootMethod initMethod) {
        
        List<Type> parameterTypes = new ArrayList<>();
        parameterTypes.add(wrappedClass.getType());
        SootMethod newConstructor = new SootMethod("<init>", parameterTypes, VoidType.v(), Modifier.PUBLIC);
        newClass.addMethod(newConstructor);

        JimpleBody constructorBody = Jimple.v().newBody(newConstructor);
        constructorBody.insertIdentityStmts();
        newConstructor.setActiveBody(constructorBody);

        Chain units = constructorBody.getUnits();
        Local thisLocal = constructorBody.getThisLocal();
        
        units.add(getSuperCall(oldClassConstructor, thisLocal, newClass));
        
        AssignStmt assignConstructor = Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(thisLocal, newField.makeRef()),
                constructorBody.getParameterLocal(0));
        units.add(assignConstructor);
        Local cipherImplLocal = Jimple.v().newLocal("cipherImpl", cipherImplClass.getType());
        constructorBody.getLocals().add(cipherImplLocal);
        
        AssignStmt cipherImplAssign = Jimple.v().newAssignStmt(cipherImplLocal, Jimple.v().newStaticInvokeExpr(initMethod.makeRef(), NullConstant.v()));
        units.add(cipherImplAssign);
        
        units.add(Jimple.v().newReturnVoidStmt());
    }
    
    protected SootMethod createCipherInitializationMethod(SootClass newClass, SootClass cipherImplClass) {
        
        List<Type> parameterTypes = new ArrayList<>();
        parameterTypes.add(cipherImplClass.getType());
        
        SootMethod initMethod = new SootMethod("initCipherImpl", parameterTypes, cipherImplClass.getType(),
                Modifier.PUBLIC | Modifier.STATIC);
        newClass.addMethod(initMethod);
        JimpleBody initBody = Jimple.v().newBody(initMethod);
        initBody.insertIdentityStmts();
        initMethod.setActiveBody(initBody);
        
        SootClass exceptionClass = Scene.v().getSootClass(
                "java.lang.RuntimeException");
        
        RefType exceptionType = RefType.v(exceptionClass);
        SootMethod exceptionInitMethod = exceptionClass.getMethod("void <init>(java.lang.String)");

        Local exceptionLocal = Jimple.v().newLocal("exceptionLocal", exceptionType);
        initBody.getLocals().add(exceptionLocal);
        
        initBody.getUnits().add(Jimple.v().newAssignStmt(exceptionLocal, Jimple.v().newNewExpr(exceptionType)));
        initBody.getUnits().add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(exceptionLocal,
                exceptionInitMethod.makeRef(), StringConstant.v("This method needs to be written by the user with " +
                        "configurations of their choosing."))));
        initBody.getUnits().add(Jimple.v().newThrowStmt(exceptionLocal));
        
        return initMethod;
    }

    protected void copyMethod(SootMethod methodToBeCopied, SootClass classToBeCopiedTo) {

        SootMethod newMethod = new SootMethod(methodToBeCopied.getName(),
                methodToBeCopied.getParameterTypes(), methodToBeCopied.getReturnType(),
                methodToBeCopied.getModifiers(), methodToBeCopied.getExceptions());
        classToBeCopiedTo.addMethod(newMethod);

        JimpleBody body = Jimple.v().newBody(newMethod);
        body.importBodyContentsFrom(methodToBeCopied.retrieveActiveBody());
        newMethod.setActiveBody(body);
    }

    protected void synthesizeMethods(SootClass newClass, SootMethod abstractMethod, String oldClassFieldName,
                                     List<SootMethod> constructors) {
        int modifiers = abstractMethod.getModifiers();

        if (Modifier.toString(modifiers).contains("abstract")) {
            modifiers = modifiers & ~Modifier.ABSTRACT;
        }

        SootMethod concreteMethod = new SootMethod(abstractMethod.getName(), abstractMethod.getParameterTypes(),
                abstractMethod.getReturnType(), modifiers);
        concreteMethod.setExceptions(abstractMethod.getExceptions());
        JimpleBody methodBody = Jimple.v().newBody(concreteMethod);
        newClass.addMethod(concreteMethod);
        methodBody.insertIdentityStmts();
        concreteMethod.setActiveBody(methodBody);

        Chain units = methodBody.getUnits();

        Local thisLocal = methodBody.getThisLocal();
        Local tempOldClassLocal = Jimple.v().newLocal("tempOldClassLocal", wrappedClass.getType());
        methodBody.getLocals().add(tempOldClassLocal);
        AssignStmt assignToOldClassLocal = Jimple.v().newAssignStmt(tempOldClassLocal, Jimple.v().newInstanceFieldRef(thisLocal,
                newClass.getField(oldClassFieldName,wrappedClass.getType()).makeRef()));
        units.add(assignToOldClassLocal);

        InvokeExpr invokeExpr = null;
        if (concreteMethod.getParameterCount() == 0) {
            invokeExpr = Jimple.v().newVirtualInvokeExpr(tempOldClassLocal, concreteMethod.makeRef());
        } else if (concreteMethod.getParameterCount() == 1) {
            invokeExpr = Jimple.v().newVirtualInvokeExpr(tempOldClassLocal, concreteMethod.makeRef(), methodBody.getParameterLocal(0));
        } else {
            invokeExpr = Jimple.v().newVirtualInvokeExpr(tempOldClassLocal, concreteMethod.makeRef(), methodBody.getParameterLocals());
        }

        if (concreteMethod.getReturnType().equals(VoidType.v())) {
            if (concreteMethod.getName().contains("<init>")) {
                constructors.add(abstractMethod);
                units.insertBefore(getSuperCall(abstractMethod, thisLocal, newClass), assignToOldClassLocal);
            } else {
                units.add(Jimple.v().newInvokeStmt(invokeExpr));
            }
            units.add(Jimple.v().newReturnVoidStmt());
        } else {
            Local resultLocal = Jimple.v().newLocal("resultLocal", concreteMethod.getReturnType());
            methodBody.getLocals().add(resultLocal);

            units.add(Jimple.v().newAssignStmt(resultLocal, invokeExpr));
            units.add(Jimple.v().newReturnStmt(resultLocal));
        }
    }
    
    protected InvokeStmt getSuperCall(SootMethod oldClassConstructor, Local thisLocal, SootClass newClass) {
        
        SootMethod superConstruct = getCorrespondingSuperConstruct(oldClassConstructor.getDeclaringClass(),
                oldClassConstructor.getParameterTypes());
        
        if (superConstruct == null) {
            System.out.println("Corresponding constructor was not found!");
        }
        
        List<Value> params = new ArrayList<>();
        for (Type type : superConstruct.getParameterTypes()) {
            if (type.equals(ShortType.v())) {
                params.add(IntConstant.v(0));
            } else if (type.equals(ByteType.v())) {
                params.add(IntConstant.v(0));
            } else if (type.equals(BooleanType.v())) {
                params.add(IntConstant.v(0));
            } else if (type.equals(CharType.v())) {
                params.add(IntConstant.v(0));
            } else if (type.equals(IntType.v())) {
                params.add(IntConstant.v(-1));
            } else {
                params.add(NullConstant.v());
            }
        }
        
        SpecialInvokeExpr callSuperConstruct;
        if (params.isEmpty()) {
            callSuperConstruct = Jimple.v().newSpecialInvokeExpr(thisLocal, Scene.v().makeConstructorRef(newClass.getSuperclass(),
                    superConstruct.getParameterTypes()));
        } else {
            callSuperConstruct = Jimple.v().newSpecialInvokeExpr(thisLocal, Scene.v().makeConstructorRef(newClass.getSuperclass(),
                    superConstruct.getParameterTypes()), params);
        }
        
        return Jimple.v().newInvokeStmt(callSuperConstruct);
    }
    
    protected SootMethod getCorrespondingSuperConstruct(SootClass superClass, List<Type> paramTypes) {
        
        for (SootMethod method : superClass.getMethods()) {
            if (method.getName().contains("<init>") &&
                    method.getParameterTypes().equals(paramTypes)) {
                return method;
            }
        }
        return null;
    }

    public SootClass getWrappedClass() {
        return wrappedClass;
    }
    
    public void setWrapperClass(SootClass wrapperClass) {
        this.wrapperClass = wrapperClass;
    }
    
    protected SootClass getWrapperClass() {
        return wrapperClass;
    }
}
