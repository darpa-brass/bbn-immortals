package com.securboration.immortals.wrapper;

import soot.*;
import soot.jimple.*;

import soot.util.Chain;

import java.util.*;

public class Wrapper {
    
    private SootClass wrappedClass;
    private SootClass wrapperClass;
    private String streamType;
    
    protected Wrapper(SootClass _wrappedClass) {
        wrappedClass = _wrappedClass;
    }
    
    protected SootMethod createCipherWrapperConstructor(SootClass newClass, SootField oldClassField, SootClass cipherImplClass,
                                                  SootMethod oldClassConstructor, SootMethod initMethod,
                                                  SootMethod wrapMethod, SootMethod getStreamImplMethod,
                                                  SootField streamImplField) {
        
        List<Type> parameterTypes = new ArrayList<>();
        parameterTypes.add(wrappedClass.getType());
        SootMethod newConstructor = new SootMethod("<init>", parameterTypes, VoidType.v(), Modifier.PUBLIC);
        newClass.addMethod(newConstructor);
        
        SootClass streamType = Scene.v().getSootClass(this.streamType);

        JimpleBody constructorBody = Jimple.v().newBody(newConstructor);
        constructorBody.insertIdentityStmts();
        newConstructor.setActiveBody(constructorBody);

        Chain units = constructorBody.getUnits();
        Local thisLocal = constructorBody.getThisLocal();
        
        units.add(getSuperCall(oldClassConstructor, thisLocal, newClass));
        
        AssignStmt assignConstructor = Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(thisLocal, oldClassField.makeRef()),
                constructorBody.getParameterLocal(0));
        units.add(assignConstructor);
        Local cipherImplLocal = Jimple.v().newLocal("cipherImpl", cipherImplClass.getType());
        constructorBody.getLocals().add(cipherImplLocal);
        AssignStmt cipherImplAssign = Jimple.v().newAssignStmt(cipherImplLocal, Jimple.v().newStaticInvokeExpr(initMethod.makeRef()));
        units.add(cipherImplAssign);
        
        Local streamImplLocal = Jimple.v().newLocal("streamImpl", streamType.getType());
        constructorBody.getLocals().add(streamImplLocal);

        AssignStmt streamImplAssign = Jimple.v().newAssignStmt(streamImplLocal, Jimple.v().newStaticInvokeExpr(getStreamImplMethod.makeRef()));
        units.add(streamImplAssign);
        
        StaticInvokeExpr wrapExpression = Jimple.v().newStaticInvokeExpr(wrapMethod.makeRef(), streamImplLocal, cipherImplLocal);
        
        AssignStmt wrappedStreamAssign = Jimple.v().newAssignStmt(streamImplLocal, wrapExpression);
        units.add(wrappedStreamAssign);
        
        AssignStmt wrappedStreamToFieldAssign = Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(thisLocal, streamImplField.makeRef()), streamImplLocal);
        units.add(wrappedStreamToFieldAssign);
        
        ReturnVoidStmt retStmt = Jimple.v().newReturnVoidStmt();
        GotoStmt gotoStmt = Jimple.v().newGotoStmt(retStmt);
        units.add(gotoStmt);
        
        Local l_r1 = Jimple.v().newLocal("r1", RefType.v("java.lang.Exception"));
        Local l_r3 = Jimple.v().newLocal("$r3", RefType.v("java.lang.Exception"));
        
        constructorBody.getLocals().add(l_r1);
        constructorBody.getLocals().add(l_r3);
        
        IdentityStmt exceptionIdStmt = Jimple.v().newIdentityStmt(l_r3, Jimple.v().newCaughtExceptionRef());
        units.add(exceptionIdStmt);
        units.add(Jimple.v().newAssignStmt(l_r1, l_r3));
        units.add(Jimple.v().newNopStmt());
        
        constructorBody.getTraps().add(Jimple.v().newTrap(Scene.v().getSootClass("java.lang.Exception"), cipherImplAssign, gotoStmt, exceptionIdStmt));
        
        units.add(retStmt);
        
        return newConstructor;
    }
    
    protected SootMethod createGetStreamImplMethod(SootClass wrapperClass) {

        List<Type> parameterTypes = new ArrayList<>();
        SootClass streamClass = Scene.v().getSootClass(this.streamType);

        List<SootClass> exceptionClasses = new ArrayList<>();
        SootClass exceptionClass = Scene.v().getSootClass("java.lang.Exception");
        exceptionClasses.add(exceptionClass);

        SootMethod getStreamImplMethod = new SootMethod("getStreamImpl", parameterTypes, streamClass.getType(),
                Modifier.PUBLIC, exceptionClasses);

        wrapperClass.addMethod(getStreamImplMethod);
        JimpleBody wrapBody = Jimple.v().newBody(getStreamImplMethod);
        wrapBody.insertIdentityStmts();
        getStreamImplMethod.setActiveBody(wrapBody);

        SootClass runtimeExceptionClass = Scene.v().getSootClass(
                "java.lang.RuntimeException");
        RefType exceptionType = RefType.v(runtimeExceptionClass);
        SootMethod exceptionInitMethod = runtimeExceptionClass.getMethod("void <init>(java.lang.String)");

        Local exceptionLocal = Jimple.v().newLocal("exceptionLocal", exceptionType);
        wrapBody.getLocals().add(exceptionLocal);

        wrapBody.getUnits().add(Jimple.v().newAssignStmt(exceptionLocal, Jimple.v().newNewExpr(exceptionType)));
        wrapBody.getUnits().add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(exceptionLocal,
                exceptionInitMethod.makeRef(), StringConstant.v("This method needs to be written by the user with " +
                        "configurations of their choosing."))));
        wrapBody.getUnits().add(Jimple.v().newThrowStmt(exceptionLocal));

        return getStreamImplMethod;
    }


    protected SootMethod createStreamAugmentationMethod(SootClass streamAugmentationClass, SootClass wrapperClass) {
        
        SootClass streamClass = Scene.v().getSootClass(this.streamType);
        
        List<Type> parameterTypes = new ArrayList<>();
        parameterTypes.add(streamClass.getType());
        parameterTypes.add(streamAugmentationClass.getType());

        List<SootClass> exceptionClasses = new ArrayList<>();
        SootClass exceptionClass = Scene.v().getSootClass("java.lang.Exception");
        exceptionClasses.add(exceptionClass);

        SootMethod streamAugmentationMethod = new SootMethod("wrap", parameterTypes, streamClass.getType(),
                Modifier.PUBLIC | Modifier.STATIC, exceptionClasses);
        
        wrapperClass.addMethod(streamAugmentationMethod);
        JimpleBody wrapBody = Jimple.v().newBody(streamAugmentationMethod);
        wrapBody.insertIdentityStmts();
        streamAugmentationMethod.setActiveBody(wrapBody);
        
        SootClass runtimeExceptionClass = Scene.v().getSootClass("java.lang.Exception");
        
        RefType exceptionType = RefType.v(runtimeExceptionClass);
        SootMethod exceptionInitMethod = runtimeExceptionClass.getMethod("void <init>(java.lang.String)");

        Local exceptionLocal = Jimple.v().newLocal("exceptionLocal", exceptionType);
        wrapBody.getLocals().add(exceptionLocal);
        wrapBody.getUnits().add(Jimple.v().newAssignStmt(exceptionLocal, Jimple.v().newNewExpr(exceptionType)));
        wrapBody.getUnits().add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(exceptionLocal,
                exceptionInitMethod.makeRef(), StringConstant.v("This method needs to be written by the user with " +
                        "configurations of their choosing."))));
        wrapBody.getUnits().add(Jimple.v().newThrowStmt(exceptionLocal));

        return streamAugmentationMethod;
    }


    protected SootMethod createCipherInitializationMethod(SootClass newClass, SootClass cipherImplClass) {
        
        List<Type> parameterTypes = new ArrayList<>();

        SootClass exceptionClass = Scene.v().getSootClass(
                "java.lang.Exception");
        List<SootClass> exceptionClasses = new ArrayList<>();
        exceptionClasses.add(exceptionClass);
        
        SootMethod initMethod = new SootMethod("initCipherImpl", parameterTypes, cipherImplClass.getType(),
                Modifier.PUBLIC | Modifier.STATIC, exceptionClasses);
        newClass.addMethod(initMethod);
        JimpleBody initBody = Jimple.v().newBody(initMethod);
        initBody.insertIdentityStmts();
        initMethod.setActiveBody(initBody);

        SootClass runtimeExceptionClass = Scene.v().getSootClass(
                "java.lang.RuntimeException");
        
        RefType exceptionType = RefType.v(runtimeExceptionClass);
        SootMethod exceptionInitMethod = runtimeExceptionClass.getMethod("void <init>(java.lang.String)");

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

    public String getStreamType() {
        return streamType;
    }

    public void setStreamType(String streamType) {
        this.streamType = streamType;
    }
}
