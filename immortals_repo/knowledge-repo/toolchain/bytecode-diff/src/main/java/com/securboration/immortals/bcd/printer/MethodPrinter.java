package com.securboration.immortals.bcd.printer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.TraceMethodVisitor;

import com.securboration.immortals.bcd.util.BytecodeUtils;

import soot.G;
import soot.SootClass;
import soot.SootMethod;

public class MethodPrinter extends ClassVisitor
{
  private final LabelMappableTextifier printer;
  
  private final ByteArrayOutputStream output = new ByteArrayOutputStream();
  private final PrintWriter writer = new PrintWriter(output);
  
  private final String methodName;
  private final String methodDesc;
  private final Integer instructionToPrint;
  
  public static List<String> printClassLines(ClassNode classNode){
      
      final String text = printClass(classNode);
      
      List<String> lines = new ArrayList<>();
      for(String line:text.split("\\r?\\n")){
          lines.add(line);
      }
      
      return lines;
  }
  
  public static String printClass(ClassNode classNode)
  {
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    PrintWriter printer = new PrintWriter(b);
    printer.printf("%s\n", classNode.name);
    
    ClassWriter writer = new ClassWriter(0);
    TraceClassVisitor visitor = new TraceClassVisitor(writer,printer);
    classNode.accept(visitor);
    
    printer.flush();
    printer.close();
    
    return new String(b.toByteArray());
  }
  

//  public static void main(String[] args) throws Exception {
////      final File jar = new File("C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\cp-eval-service\\eval-out\\ess\\ess\\client\\target\\immortals-cp3.1-client-1.0.0MODIFIED.jar");
////      final String classpath = Scene.v().defaultClassPath() + File.pathSeparator + jar.getCanonicalPath();
////      System.out.println(classpath);
//////      
//////      Options.v().set_output_format(Options.output_format_jimple);
//////      Options.v().set_soot_classpath(classpath);
//////      
//////      Scene.v().loadNecessaryClasses();
//////      
//////      final SootClass c = Scene.v().forceResolve(
//////          "com.securboration.client.MessageListenerClient",
//////          SootClass.BODIES
//////          );
////      
////      
////      
////      
////      for(SootMethod m:c.getMethods()){
////          System.out.println(m.getName() + " " + m.getSignature());
//////          if(!m.hasActiveBody()){
//////              continue;
//////          }
//////          
////          
////          
////          //<com.securboration.client.MessageListenerClient: nodeToString(Lorg/w3c/dom/Node;)Ljava/lang/String;>
////          
////          final String bcs = m.getBytecodeSignature();
////          
////          final String desc = bcs.substring(bcs.indexOf(": ") + 2, bcs.length()-1);
////          
////          System.out.println(m.getBytecodeSignature());
////          System.out.println(desc);
////          final Type asmType = Type.getMethodType(desc);
////          System.out.printf(
////              "\t%s (a %s)\n", 
////              m.getName(), 
////              m.retrieveActiveBody().getClass().getName()
////              );
////          
//////          java.io.PrintWriter out = openBodyFile(b, baseName);
////          
//////          soot.Printer.v().setOption(Printer.USE_ABBREVIATIONS);
////          
////      }
//  }
  
  public static String printJimple(
          final File jar, 
          final ClassNode cn,
          final MethodNode mn
          ) throws IOException{
      final SootClass sc = BytecodeUtils.getSootClass(jar, cn.name.replace("/", "."));
      final SootMethod sm = BytecodeUtils.getSootMethod(sc, mn);
      
//      System.out.println(jar.getName());
//      System.out.println(sc.hashCode());
      
      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      try(PrintWriter writer = new PrintWriter(out)){
          soot.Printer.v().printTo(
              sm.retrieveActiveBody(), 
              writer
              );
      }
      
      {//TODO: prevent the class from being cached
//          Scene.v().removeClass(sc);
//          Scene.v().releaseActiveHierarchy();
//          Scene.v().releaseFastHierarchy();
//          
//          Scene.v().releaseCallGraph();
//          Scene.v().releaseClientAccessibilityOracle();
//          Scene.v().releasePointsToAnalysis();
//          Scene.v().releaseReachableMethods();
//          Scene.v().releaseSideEffectAnalysis();
//          
//          Chain<SootClass> c = Scene.v().getClasses();
//          for(SootClass sootc:new ArrayList<>(c)){
//              if(sootc.getName().startsWith("java")){
//                  continue;
//              }
//              System.out.printf("\tremoving %s %d\n", sootc.getName(), sootc.hashCode());//TODO
//              Scene.v().removeClass(sootc);
//          }
          
          G.reset();
      }
      
      return out.toString(StandardCharsets.UTF_8.name());
  }
  
  public static String print(
      MethodNode mn
      )
  {
    return print(mn,(AbstractInsnNode)null);
  }
  
  public static String print(
      MethodNode mn,
      AbstractInsnNode i
      )
  {
    return print(mn,i == null ? null : mn.instructions.indexOf(i));
  }
  
  private static String print(
      MethodNode mn,
      Integer instructionIndex
      )
  {
    ClassNode c = new ClassNode();
    {
      c.name = "dummyclass";
      c.methods = new ArrayList<MethodNode>();
      c.methods.add(mn);
    }
    
    LabelMappableTextifier labelAwareTextifier = 
        new LabelMappableTextifier();
    
    MethodPrinter visitor = 
        new MethodPrinter(
            mn.name,
            mn.desc,
            instructionIndex,
            labelAwareTextifier);
    c.accept(visitor);
    
    return visitor.output.toString().trim();
  }
  
  /**
   * 
   * @param methodName null = wildcard
   * @param methodDesc null = wildcard
   * @param instructionIndex null = wildcard
   */
  private MethodPrinter(
      String methodName,
      String methodDesc,
      Integer instructionIndex
      )
  {
    this(methodName,methodDesc,instructionIndex,new LabelMappableTextifier());
  }
  
  private MethodPrinter(
      String methodName,
      String methodDesc,
      Integer instructionIndex, 
      LabelMappableTextifier printer
      )
  {
    super(Opcodes.ASM5);
    
    this.methodName = methodName;
    this.methodDesc = methodDesc;
    this.instructionToPrint = instructionIndex;
    
    this.printer =  printer;
  }
  
  
  @Override
  public MethodVisitor visitMethod(
      final int access, 
      final String name, 
      final String desc, 
      final String signature, 
      final String[] exceptions
      )
  {
    final boolean nameMatches = 
        (name == null)
        ||
        (this.methodName.equals(name));
    
    final boolean descMatches = 
        (desc == null)
        ||
        (this.methodDesc.equals(desc));
    
    if(nameMatches && descMatches)
    {
      if(instructionToPrint != null)
      {
        //print only one instruction in the method
        return new MethodInstructionPrinter(
            printer,
            instructionToPrint);
      }
      else
      {
        //print everything in the method
        return new TraceMethodVisitor(printer);
      }
    }
    
    return new MethodVisitor(Opcodes.ASM5){};
  }
  
  @Override
  public void visitEnd()
  {
    this.printer.print(writer);
    this.writer.flush();
  }

}
