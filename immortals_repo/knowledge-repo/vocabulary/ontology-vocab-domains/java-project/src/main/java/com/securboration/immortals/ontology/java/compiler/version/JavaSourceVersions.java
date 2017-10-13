package com.securboration.immortals.ontology.java.compiler.version;

import com.securboration.immortals.ontology.bytecode.BytecodeVersion;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;

@Ignore
public class JavaSourceVersions {
    
    /*
    Java SE 9 = 53 (0x35 hex)
    Java SE 8 = 52 (0x34 hex),
    Java SE 7 = 51 (0x33 hex),
    Java SE 6.0 = 50 (0x32 hex),
    Java SE 5.0 = 49 (0x31 hex),
    JDK 1.4 = 48 (0x30 hex),
    JDK 1.3 = 47 (0x2F hex),
    JDK 1.2 = 46 (0x2E hex),
    JDK 1.1 = 45 (0x2D hex).
    */
    
    private static final BytecodeVersion jdkBeta = getVersion(null,null,"JDK Beta");
    private static final BytecodeVersion jdk1_0 = getVersion("45",null,"JDK 1.0");
    private static final BytecodeVersion jdk1_1 = getVersion("45",null,"JDK 1.1");
    private static final BytecodeVersion j2se1_2 = getVersion("46",null,"J2SE 1.2");
    private static final BytecodeVersion j2se1_3 = getVersion("47",null,"J2SE 1.3");
    private static final BytecodeVersion j2se1_4 = getVersion("48",null,"J2SE 1.4");
    private static final BytecodeVersion j2se5_0 = getVersion("49",null,"J2SE 5.0");
    private static final BytecodeVersion j2se6_0 = getVersion("50",null,"J2SE 6.0");
    private static final BytecodeVersion jse7 = getVersion("51",null,"Java SE 7");
    private static final BytecodeVersion jse8 = getVersion("52",null,"Java SE 8");
    private static final BytecodeVersion jse9 = getVersion("53",null,"Java SE 9");
    
    private static BytecodeVersion getVersion(
            String majorVersionTag,
            String minorVersionTag,
            String platformVersionTag
            ){
        BytecodeVersion v = new BytecodeVersion();
        v.setMajorVersionTag(majorVersionTag);
        v.setMinorVersionTag(minorVersionTag);
        v.setPlatformVersionTag(platformVersionTag);
        return v;
    }
    
    @ConceptInstance
    public static class JavaSourceVersionJSE9 extends JavaSourceVersion{
        public JavaSourceVersionJSE9(){
            this.setBackwardCompatibleWith(new JavaSourceVersion[]{
                    new JavaSourceVersionJSE8(),
                    new JavaSourceVersionJSE7(),
                    new JavaSourceVersionJ2SE6_0(),
                    new JavaSourceVersionJ2SE5_0(),
                    new JavaSourceVersionJ2SE1_4(),
                    new JavaSourceVersionJ2SE1_3(),
                    new JavaSourceVersionJ2SE1_2(),
                    new JavaSourceVersionJdk1_1(),
                    new JavaSourceVersionJdk1_0(),
                    new JavaSourceVersionJdkBeta()
                    });
            this.setTargetBytecodeVersion(jse9);
        }
    }
    
    @ConceptInstance
    public static class JavaSourceVersionJSE8 extends JavaSourceVersion{
        public JavaSourceVersionJSE8(){
            this.setBackwardCompatibleWith(new JavaSourceVersion[]{
                    new JavaSourceVersionJSE7(),
                    new JavaSourceVersionJ2SE6_0(),
                    new JavaSourceVersionJ2SE5_0(),
                    new JavaSourceVersionJ2SE1_4(),
                    new JavaSourceVersionJ2SE1_3(),
                    new JavaSourceVersionJ2SE1_2(),
                    new JavaSourceVersionJdk1_1(),
                    new JavaSourceVersionJdk1_0(),
                    new JavaSourceVersionJdkBeta()
                    });
            this.setTargetBytecodeVersion(jse8);
        }
    }
    
    @ConceptInstance
    public static class JavaSourceVersionJSE7 extends JavaSourceVersion{
        public JavaSourceVersionJSE7(){
            this.setBackwardCompatibleWith(new JavaSourceVersion[]{
                    new JavaSourceVersionJ2SE6_0(),
                    new JavaSourceVersionJ2SE5_0(),
                    new JavaSourceVersionJ2SE1_4(),
                    new JavaSourceVersionJ2SE1_3(),
                    new JavaSourceVersionJ2SE1_2(),
                    new JavaSourceVersionJdk1_1(),
                    new JavaSourceVersionJdk1_0(),
                    new JavaSourceVersionJdkBeta()
                    });
            this.setTargetBytecodeVersion(jse7);
        }
    }
    
    @ConceptInstance
    public static class JavaSourceVersionJ2SE6_0 extends JavaSourceVersion{
        public JavaSourceVersionJ2SE6_0(){
            this.setBackwardCompatibleWith(new JavaSourceVersion[]{
                    new JavaSourceVersionJ2SE5_0(),
                    new JavaSourceVersionJ2SE1_4(),
                    new JavaSourceVersionJ2SE1_3(),
                    new JavaSourceVersionJ2SE1_2(),
                    new JavaSourceVersionJdk1_1(),
                    new JavaSourceVersionJdk1_0(),
                    new JavaSourceVersionJdkBeta()
                    });
            this.setTargetBytecodeVersion(j2se6_0);
        }
    }
    
    @ConceptInstance
    public static class JavaSourceVersionJ2SE5_0 extends JavaSourceVersion{
        public JavaSourceVersionJ2SE5_0(){
            this.setBackwardCompatibleWith(new JavaSourceVersion[]{
                    new JavaSourceVersionJ2SE1_4(),
                    new JavaSourceVersionJ2SE1_3(),
                    new JavaSourceVersionJ2SE1_2(),
                    new JavaSourceVersionJdk1_1(),
                    new JavaSourceVersionJdk1_0(),
                    new JavaSourceVersionJdkBeta()
                    });
            this.setTargetBytecodeVersion(j2se5_0);
        }
    }
    
    @ConceptInstance
    public static class JavaSourceVersionJ2SE1_4 extends JavaSourceVersion{
        public JavaSourceVersionJ2SE1_4(){
            this.setBackwardCompatibleWith(new JavaSourceVersion[]{
                    new JavaSourceVersionJ2SE1_3(),
                    new JavaSourceVersionJ2SE1_2(),
                    new JavaSourceVersionJdk1_1(),
                    new JavaSourceVersionJdk1_0(),
                    new JavaSourceVersionJdkBeta()
                    });
            this.setTargetBytecodeVersion(j2se1_4);
        }
    }
    
    @ConceptInstance
    public static class JavaSourceVersionJ2SE1_3 extends JavaSourceVersion{
        public JavaSourceVersionJ2SE1_3(){
            this.setBackwardCompatibleWith(new JavaSourceVersion[]{
                    new JavaSourceVersionJ2SE1_2(),
                    new JavaSourceVersionJdk1_1(),
                    new JavaSourceVersionJdk1_0(),
                    new JavaSourceVersionJdkBeta()
                    });
            this.setTargetBytecodeVersion(j2se1_3);
        }
    }
    
    @ConceptInstance
    public static class JavaSourceVersionJ2SE1_2 extends JavaSourceVersion{
        public JavaSourceVersionJ2SE1_2(){
            this.setBackwardCompatibleWith(new JavaSourceVersion[]{
                    new JavaSourceVersionJdk1_1(),
                    new JavaSourceVersionJdk1_0(),
                    new JavaSourceVersionJdkBeta()
                    });
            this.setTargetBytecodeVersion(j2se1_2);
        }
    }
    
    @ConceptInstance
    public static class JavaSourceVersionJdk1_1 extends JavaSourceVersion{
        public JavaSourceVersionJdk1_1(){
            this.setBackwardCompatibleWith(new JavaSourceVersion[]{
                    new JavaSourceVersionJdk1_0(),
                    new JavaSourceVersionJdkBeta()
                    });
            this.setTargetBytecodeVersion(jdk1_1);
        }
    }
    
    @ConceptInstance
    public static class JavaSourceVersionJdk1_0 extends JavaSourceVersion{
        public JavaSourceVersionJdk1_0(){
            this.setBackwardCompatibleWith(new JavaSourceVersion[]{
                    new JavaSourceVersionJdkBeta()
                    });
            this.setTargetBytecodeVersion(jdk1_0);
        }
    }
    
    @ConceptInstance
    public static class JavaSourceVersionJdkBeta extends JavaSourceVersion{
        public JavaSourceVersionJdkBeta(){
            this.setBackwardCompatibleWith(null);
            this.setTargetBytecodeVersion(jdkBeta);
        }
    }

}
