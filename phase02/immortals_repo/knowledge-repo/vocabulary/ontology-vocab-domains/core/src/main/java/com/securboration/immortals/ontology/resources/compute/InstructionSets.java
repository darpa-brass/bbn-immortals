package com.securboration.immortals.ontology.resources.compute;

import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;

@Ignore
public class InstructionSets {
    
    @ConceptInstance
    public static class X86 extends InstructionSet{
        public X86(){this.setHumanReadableDescription("Intel's fully backward compatible microprocessor instruction set originating with the 8086");}
    }
    
    @ConceptInstance
    public static class X64 extends X86{
        public X64(){this.setHumanReadableDescription("64-bit extensions to x86");}
    }
    
    @ConceptInstance
    public static class AES_NI extends X86{
        public AES_NI(){this.setHumanReadableDescription("Advanced Encryption Standard instructions");}
    }
    
    @ConceptInstance
    public static class FPU extends X86{
        public FPU(){this.setHumanReadableDescription("x87 Floating-point-unit (FPU) instructions");}
    }
    
    @ConceptInstance
    public static class MMX extends X86{
        public MMX(){this.setHumanReadableDescription("MMX SIMD instructions");}
    }
    
    @ConceptInstance
    public static class MMX_EXT extends X86{
        public MMX_EXT(){this.setHumanReadableDescription("extended MMX SIMD instructions");}
    }
    
    @ConceptInstance
    public static class SSE extends X86{
        public SSE(){this.setHumanReadableDescription("streaming SIMD extensions (SSE) instructions (70 instructions)");}
    }
    
    @ConceptInstance
    public static class SSE2 extends X86{
        public SSE2(){this.setHumanReadableDescription("streaming SIMD extensions 2 instructions (144 new instructions)");}
    }
    
    @ConceptInstance
    public static class SSE3 extends X86{
        public SSE3(){this.setHumanReadableDescription("streaming SIMD extensions 3 instructions (13 new instructions)");}
    }
    
    public static class SSSE3 extends X86{
        public SSSE3(){this.setHumanReadableDescription("supplemental streaming SIMD extensions (16 instructions)");}
    }
    
    @ConceptInstance
    public static class SSE4_1 extends X86{
        public SSE4_1(){this.setHumanReadableDescription("streaming SIMD extensions 4, Penryn subset (47 instructions)");}
    }
    
    @ConceptInstance
    public static class SSE4_2 extends X86{
        public SSE4_2(){this.setHumanReadableDescription("streaming SIMD extensions 4, Nehalem subset (7 instructions)");}
    }
    
    @ConceptInstance
    public static class SSE4 extends X86{
        public SSE4(){this.setHumanReadableDescription("All streaming SIMD extensions 4 instructions (both SSE4.1 and SSE4.2)");}
    }
    
    @ConceptInstance
    public static class SSE4A extends X86{
        public SSE4A(){this.setHumanReadableDescription("streaming SIMD extensions 4a (AMD)");}
    }
    
    @ConceptInstance
    public static class SSE5 extends X86{
        public SSE5(){this.setHumanReadableDescription("streaming SIMD extensions 5 (170 instructions)");}
    }
    
    @ConceptInstance
    public static class XSAVE extends X86{
        public XSAVE(){this.setHumanReadableDescription("XSAVE instructions");}
    }
    
    @ConceptInstance
    public static class AVX extends X86{
        public AVX(){this.setHumanReadableDescription("advanced vector extensions instructions");}
    }
    
    @ConceptInstance
    public static class FMA extends X86{
        public FMA(){this.setHumanReadableDescription("fused multiply-add instructions");}
    }
    
    @ConceptInstance
    public static class CLMUL extends X86{
        public CLMUL(){this.setHumanReadableDescription("Carry-less mtiply (PCLMULQDQ) instruction");}
    }
    
    @ConceptInstance
    public static class Cyrix extends X86{
        public Cyrix(){this.setHumanReadableDescription("Cyrix-specific instructions");}
    }
    
    @ConceptInstance
    public static class AMD extends X86{
        public AMD(){this.setHumanReadableDescription("AMD-specific instructions (older than K6)");}
    }
    
    @ConceptInstance
    public static class SMM extends X86{
        public SMM(){this.setHumanReadableDescription("System management mode instructions");}
    }
    
    @ConceptInstance
    public static class SVM extends X86{
        public SVM(){this.setHumanReadableDescription("Secure virtual machine instructions");}
    }
    
    @ConceptInstance
    public static class PadLock extends X86{
        public PadLock(){this.setHumanReadableDescription("VIA PadLock instructions");}
    }

}
