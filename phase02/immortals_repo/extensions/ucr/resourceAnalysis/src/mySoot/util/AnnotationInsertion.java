package mySoot.util;

/**
 * Author: Yue Duan @ Syracuse University
 * This class is to insert annotation into a java class file and generate new class file
 */

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class AnnotationInsertion 
{
	private String fileName;
	private String annotationDesc;
	private String methodName;
	
	private String outputDir;
	
	/**
	 * Constructor
	 * @param fileName : class file name
	 * @param annotationDesc : annotation to be inserted
	 * @param method : method to be modified
	 */
	public AnnotationInsertion() {}
	
	
	/**
	 * set the information needed for insertion
	 * @param fileName : class file name
	 * @param annotationDesc : annotation to be inserted
	 * @param method : method to be modified
	 */
	public void setInfo(String fileName, String annotationDesc, String method, String outputDir)
	{
		this.annotationDesc = annotationDesc;
		this.methodName = method;
		this.fileName = fileName;
		this.outputDir = outputDir;
	}
	
	
	/**
	 * This function is responsible for performing annotation insertion. 
	 * It first reads the given class file, then traverse through each method and find the right one. 
	 * Finally it performs insertion by calling doInsert() and writes to a new class file.
	 */
	@SuppressWarnings("unchecked")
	public void performInsertion()
	{
		if(this.fileName == null || this.fileName.isEmpty())
		{
			System.err.println("no insertion information is provided!");
			return;
		}
		
		try 
		{
			FileInputStream in = new FileInputStream(fileName);

			ClassReader cr = new ClassReader(in);
	        ClassNode classNode = new ClassNode();
	        cr.accept(classNode, 0);
	        
	        for(Object mn : classNode.methods)
	        {
	        	MethodNode methodNode = (MethodNode) mn;
	            if(!methodNode.name.equals(this.methodName))
	            	continue;
	            
	            // check if the annotation is already there
	            boolean hasAnnotation=false;
	            if(methodNode.visibleAnnotations != null)
	            {
	                for(Object an: methodNode.visibleAnnotations)
	                {
	                	AnnotationNode annotationNode = (AnnotationNode) an;
	                    if(annotationNode.desc.equals(this.annotationDesc))
	                    {
	                        hasAnnotation = true;
	                        break;
	                    }
	                }
	            }
	            
	            if(!hasAnnotation)
	            {
	            	System.out.println("Insert: " + this.annotationDesc + " to " + this.methodName + " in file " + this.fileName);
	            	
	            	if(methodNode.visibleAnnotations == null)
	            		methodNode.visibleAnnotations = new  ArrayList<AnnotationNode>();
	            	
	            	doInsert(methodNode.visibleAnnotations);
	            }
	            
	        } //end for each methodNode
	        
	        //We are done now. so dump the class
	        ClassWriter cw = new ClassWriter(0);//ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);
	        classNode.accept(cw);
	        
	        String[] strs = this.fileName.split("/");
	        String newFileName = strs[strs.length - 1];
	        
	        File outDir = new File(this.outputDir);
	        
	        //System.out.println(this.outputDir);
	        //System.out.println(newFileName);
	        
	        outDir.mkdirs();
	        DataOutputStream dout = new DataOutputStream(new FileOutputStream(new File(outDir, newFileName)));
	        dout.write(cw.toByteArray());
	        
	        in.close();
	        dout.flush();
	        dout.close();
		}
		catch (Exception e) 
		{
			System.out.println("Exception occurred: " + e.getMessage());
		}
	}

	/*
	 * Perform real insertion in this function. 
	 */
    private void doInsert(List<AnnotationNode> visibleAnnotations) 
    {
    	AnnotationNode newAnnotation = new AnnotationNode(this.annotationDesc);
    	visibleAnnotations.add(newAnnotation);
	}

	public static void main(String[] args) throws IOException
    {
		String file = "/home/yduan/yueduan/bbnAnalysis/apks/ATAKLite/com/bbn/ataklite/CoTMessage.class";
		AnnotationInsertion ai = new AnnotationInsertion();
		
		ai.setInfo(file, "GPS", "<init>", "/home/yduan/yueduan/bbnAnalysis/bin/./../output/");
		ai.performInsertion();
    }

}