package com.securboration.immortals.wrapper;

import com.securboration.immortals.ontology.constraint.MethodAdaptation;
import com.securboration.immortals.ontology.functionality.ConfigurationBinding;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.alg.encryption.*;
import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.pattern.spec.CodeSpec;
import com.securboration.immortals.utility.GradleTaskHelper;
import com.securboration.immortals.utility.cipher.CipherInfo;
import soot.*;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.*;

import soot.util.Chain;

import java.util.*;

public class Wrapper {

    private SootClass wrappedClass;
    private SootClass wrapperClass;
    private String streamType;
    private List<FunctionalAspect> aspectsAdapted;
    private CipherInfo cipherInfo;

    protected Wrapper(SootClass _wrappedClass) {
        wrappedClass = _wrappedClass;
        aspectsAdapted = new ArrayList<>();
        cipherInfo = new CipherInfo();
    }

    protected SootMethod createCipherWrapperConstructorSimple(SootClass newClass, SootField oldClassField, SootClass cipherImplClass,
                                                              SootMethod initMethod, SootMethod wrapMethod, SootMethod getStreamImplMethod,
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

        AssignStmt assignConstructor = Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(thisLocal, oldClassField.makeRef()),
                constructorBody.getParameterLocal(0));
        units.add(assignConstructor);
        Local cipherImplLocal = Jimple.v().newLocal("cipherImpl", cipherImplClass.getType());
        constructorBody.getLocals().add(cipherImplLocal);
        AssignStmt cipherImplAssign = Jimple.v().newAssignStmt(cipherImplLocal, Jimple.v().newStaticInvokeExpr(initMethod.makeRef()));
        units.add(cipherImplAssign);

        Local streamImplLocal = Jimple.v().newLocal("streamImpl", streamType.getType());
        constructorBody.getLocals().add(streamImplLocal);

        AssignStmt streamImplAssign = Jimple.v().newAssignStmt(streamImplLocal, Jimple.v().newVirtualInvokeExpr(thisLocal, getStreamImplMethod.makeRef()));
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


    protected SootMethod createCipherWrapperConstructorComplex(SootClass newClass, SootField oldClassField, SootMethod oldClassConstructor,
                                                               SootMethod initMethod, SootField cipherField) {
        List<Type> parameterTypes = new ArrayList<>();
        parameterTypes.add(wrappedClass.getType());
        SootMethod newConstructor = new SootMethod("<init>", parameterTypes, VoidType.v(), Modifier.PUBLIC);
        newClass.addMethod(newConstructor);

        JimpleBody constructorBody = Jimple.v().newBody(newConstructor);
        constructorBody.insertIdentityStmts();
        newConstructor.setActiveBody(constructorBody);

        Chain units = constructorBody.getUnits();
        Local thisLocal = constructorBody.getThisLocal();

        units.add(getSuperCall(oldClassConstructor, thisLocal, newClass, constructorBody, units, constructorBody.getParameterLocal(0)));

        AssignStmt assignConstructor = Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(thisLocal, oldClassField.makeRef()),
                constructorBody.getParameterLocal(0));
        units.add(assignConstructor);

        Local tempCipherLocal = Jimple.v().newLocal("tempCipher", cipherField.getType());
        constructorBody.getLocals().add(tempCipherLocal);
        StaticInvokeExpr invokeCipherInit = Jimple.v().newStaticInvokeExpr(initMethod.makeRef());
        AssignStmt assignToTempCipher = Jimple.v().newAssignStmt(tempCipherLocal, invokeCipherInit);
        units.add(assignToTempCipher);

        AssignStmt cipherImplAssign = Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(thisLocal, cipherField.makeRef()),
                tempCipherLocal);
        units.add(cipherImplAssign);

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

        constructorBody.getTraps().add(Jimple.v().newTrap(Scene.v().getSootClass("java.lang.Exception"), assignToTempCipher, gotoStmt, exceptionIdStmt));

        units.add(retStmt);

        return newConstructor;
    }

    protected void expandConstructorSurface(SootMethod construct, SootField streamField, SootMethod streamGetter,
                                            SootMethod cipherInit, SootMethod streamWrap) {

        JimpleBody constructBody = (JimpleBody) construct.getActiveBody();
        Chain<Unit> units = constructBody.getUnits();
        Local thisLocal = constructBody.getThisLocal();

        Local cipherImplLocal = null;
        Chain<Local> locals = constructBody.getLocals();
        for (Local local : locals) {
            if (local.getType().equals(cipherInit.getReturnType())) {
                cipherImplLocal = local;
                break;
            }
        }

        Chain<Trap> traps = constructBody.getTraps();
        Trap mainTrap = traps.getFirst();

        Unit firstTrapUnit = mainTrap.getBeginUnit();

        Local streamImplLocal = Jimple.v().newLocal("streamImpl", streamField.getType());
        constructBody.getLocals().add(streamImplLocal);

        AssignStmt streamImplAssign = Jimple.v().newAssignStmt(streamImplLocal, Jimple.v().newVirtualInvokeExpr(thisLocal,
                streamGetter.makeRef()));
        units.insertAfter(streamImplAssign, firstTrapUnit);

        StaticInvokeExpr wrapExpression = Jimple.v().newStaticInvokeExpr(streamWrap.makeRef(),
                streamImplLocal, cipherImplLocal);

        AssignStmt wrappedStreamAssign = Jimple.v().newAssignStmt(streamImplLocal, wrapExpression);
        units.insertAfter(wrappedStreamAssign, streamImplAssign);

        AssignStmt wrappedStreamToFieldAssign = Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(thisLocal,
                streamField.makeRef()), streamImplLocal);
        units.insertAfter(wrappedStreamToFieldAssign, wrappedStreamAssign);
    }

    protected SootMethod createGetStreamImplMethod(SootClass wrapperClass) {

        List<Type> parameterTypes = new ArrayList<>();
        SootClass streamClass = Scene.v().getSootClass(this.streamType);
        List<SootClass> exceptionClasses = new ArrayList<>();
        SootClass exceptionClass = Scene.v().getSootClass("java.lang.Exception");
        exceptionClasses.add(exceptionClass);

        SootMethod getStreamImplMethod = new SootMethod("get" + streamClass.getShortName() +"Impl", parameterTypes, streamClass.getType(),
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

        SootMethod streamAugmentationMethod = new SootMethod("wrap" + streamClass.getShortName(),
                parameterTypes, streamClass.getType(), Modifier.PUBLIC | Modifier.STATIC, exceptionClasses);

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

    public static void createCipherInitMethodWithBody(SootClass augmentedClass, SootClass cipherImpl,
                                                      List<ConfigurationBinding> configurationVariables) throws InstantiationException, IllegalAccessException {
        List<Type> parameterTypes = new ArrayList<>();

        SootClass exceptionClass = Scene.v().getSootClass(
                "java.lang.Exception");
        List<SootClass> exceptionClasses = new ArrayList<>();
        exceptionClasses.add(exceptionClass);

        SootMethod initMethod = new SootMethod("initCipherImpl", parameterTypes, cipherImpl.getType(),
                Modifier.PUBLIC | Modifier.STATIC, exceptionClasses);
        augmentedClass.addMethod(initMethod);
        JimpleBody newBody = Jimple.v().newBody(initMethod);
        SootMethod cipherConstructor = cipherImpl.getMethod("void <init>()");
        SootMethod cipherConfigure = cipherImpl.getMethod(
                "void configure(java.lang.String,int,java.lang.String," +
                        "java.lang.String,java.lang.String,java.lang.String)");
        List<Value> paramsForConfig = new ArrayList<>();
        for (ConfigurationBinding configurationVariable : configurationVariables) {
            paramsForConfig.add(parseConfigVariable(configurationVariable));
        }
        Local cipherImplLocal = Jimple.v().newLocal("r1", cipherImpl.getType());
        newBody.getLocals().add(cipherImplLocal);
        initMethod.setActiveBody(newBody);
        newBody.insertIdentityStmts();
        newBody.getUnits().add(Jimple.v().newAssignStmt(cipherImplLocal, Jimple.v().newNewExpr(cipherImpl.getType())));
        newBody.getUnits().add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(cipherImplLocal, cipherConstructor.makeRef())));
        newBody.getUnits().add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(cipherImplLocal, cipherConfigure.makeRef(),
                paramsForConfig)));
        newBody.getUnits().add(Jimple.v().newReturnStmt(cipherImplLocal));

        newBody.validate();
    }

    private static Local generateFreshLocal(Body b, Type type){
        LocalGenerator lg = new LocalGenerator(b);
        return lg.generateLocal(type);
    }

    public static Value parseConfigVariable(ConfigurationBinding configurationBinding) throws IllegalAccessException, InstantiationException {
        Class<? extends DataType> configSemanticType = configurationBinding.getSemanticType();
        if (CipherKeyLength.class.isInstance(configSemanticType.newInstance())) {
            return IntConstant.v(Integer.parseInt(configurationBinding.getBinding()));
        } else {
            return StringConstant.v(configurationBinding.getBinding());
        }
    }

    public static SootMethod createCipherInitializationMethod(SootClass newClass, SootClass cipherImplClass) {

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
        if (concreteMethod.getName().contains("<init>")) {
            constructors.add(abstractMethod);
            return;
        }

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
            units.add(Jimple.v().newInvokeStmt(invokeExpr));
            units.add(Jimple.v().newReturnVoidStmt());
        } else {
            Local resultLocal = Jimple.v().newLocal("resultLocal", concreteMethod.getReturnType());
            methodBody.getLocals().add(resultLocal);

            units.add(Jimple.v().newAssignStmt(resultLocal, invokeExpr));
            units.add(Jimple.v().newReturnStmt(resultLocal));
        }
    }

    private SootMethodRef findSuperParamForComplexWrap(Type superParamType, SootClass wrapperClass) {
        SootMethodRef sootMethodRef = null;
        while (sootMethodRef == null) {
            for (SootMethod sootMethod : wrapperClass.getMethods()) {
                if (sootMethod.getName().contains("provider")) {
                    System.out.println("debugging");
                }
                if (sootMethod.getReturnType().equals(superParamType)) {
                    sootMethodRef = sootMethod.makeRef();
                }
            }
            if (wrapperClass.getSuperclass() != null) {
                wrapperClass = wrapperClass.getSuperclass();
            } else {
                return null;
            }
        }
        return sootMethodRef;
    }

    protected InvokeStmt getSuperCall(SootMethod oldClassConstructor, Local thisLocal, SootClass newClass, JimpleBody methodBody,
                                      Chain units, Local delegateLocal) {

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

                //TODO -> This is really bad... come up with more intelligent mechanism soon plz
                if (newClass.getName().contains("WrapperSocketChannel")) {
                    SootMethodRef methodRef = findSuperParamForComplexWrap(type, newClass);
                    VirtualInvokeExpr virtualInvokeExpr = Jimple.v().newVirtualInvokeExpr(delegateLocal, methodRef);
                    Local providerLocal = Jimple.v().newLocal("provider", methodRef.returnType());
                    methodBody.getLocals().add(providerLocal);
                    units.add(Jimple.v().newAssignStmt(providerLocal, virtualInvokeExpr));
                    SpecialInvokeExpr specialInvokeExpr = Jimple.v().newSpecialInvokeExpr(thisLocal, Scene.v().makeConstructorRef(newClass.getSuperclass(),
                            superConstruct.getParameterTypes()), providerLocal);
                    return Jimple.v().newInvokeStmt(specialInvokeExpr);
                }
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

    public void introduceMediationMethods(List<CodeSpec> codeSpecs, GradleTaskHelper taskHelper) {

        for (CodeSpec codeSpec : codeSpecs) {

            if (taskHelper.checkForPreviousAugmentation(codeSpec)) {
                continue;
            }

            String methodSignature = codeSpec.getMethodSignature();

            String parametersString = methodSignature.substring(methodSignature.indexOf("(") + 1, methodSignature.indexOf(")"));
            String[] parametersArray = parametersString.split(",");

            List<Type> parameterTypes = new ArrayList<>();
            if (!parametersString.equals("")) {
                for (String parameter : parametersArray) {
                    parseType(parameter.trim(), parameterTypes);
                }
            }

            List<SootClass> exceptionClasses = new ArrayList<>();
            SootMethod newMediationMethod = new SootMethod(methodSignature.substring(0, methodSignature.indexOf("(")),
                    parameterTypes, getMediationMethodReturnType(codeSpec), Modifier.PUBLIC, exceptionClasses);

            this.wrapperClass.addMethod(newMediationMethod);
            JimpleBody wrapBody = Jimple.v().newBody(newMediationMethod);
            wrapBody.insertIdentityStmts();
            newMediationMethod.setActiveBody(wrapBody);

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

            MethodAdaptation mediationAdaptation = new MethodAdaptation();
            mediationAdaptation.setSignature(codeSpec.getMethodSignature());
            taskHelper.getMethodAdaptations().add(mediationAdaptation);
        }
    }

    private void parseType(String parameter, List<Type> parameterTypes) {
        switch (parameter) {
            case "int":
                parameterTypes.add(IntType.v());
                break;
            case "byte":
                parameterTypes.add(ByteType.v());
                break;
            case "boolean":
                parameterTypes.add(BooleanType.v());
                break;
            default:
                if (parameter.charAt(0) == 'L') {
                    parameter = parameter.substring(1);
                }
                SootClass paramClass = Scene.v().loadClassAndSupport(parameter.replace("/", "."));
                parameterTypes.add(paramClass.getType());
        }
    }

    private Type getMediationMethodReturnType(CodeSpec codeSpec) {
        String returnType = codeSpec.getMethodSignature().substring(codeSpec.getMethodSignature().lastIndexOf(")") + 1);
        switch (returnType.charAt(0)) {
            case 'Z':
                return BooleanType.v();
            case 'B':
                return ByteType.v();
            case 'C':
                return CharType.v();
            case 'S':
                return ShortType.v();
            case 'I':
                return IntType.v();
            case 'J':
                return LongType.v();
            case 'F':
                return FloatType.v();
            case 'D':
                return DoubleType.v();
            case 'L':
                returnType = returnType.substring(1);
                break;
            case 'V':
                return VoidType.v();
            default:
                break;
        }

        SootClass returnTypeClass = Scene.v().loadClassAndSupport(returnType.replace("/", "."));
        return returnTypeClass.getType();
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

    public SootClass getWrapperClass() {
        return wrapperClass;
    }

    public String getStreamType() {
        return streamType;
    }

    public void setStreamType(String streamType) {
        this.streamType = streamType;
    }

    public List<FunctionalAspect> getAspectsAdapted() {
        return aspectsAdapted;
    }

    public void setAspectsAdapted(List<FunctionalAspect> aspectsAdapted) {
        this.aspectsAdapted = aspectsAdapted;
    }

    public CipherInfo getCipherInfo() {
        return cipherInfo;
    }

    public void setCipherInfo(CipherInfo cipherInfo) {
        this.cipherInfo = cipherInfo;
    }
}
