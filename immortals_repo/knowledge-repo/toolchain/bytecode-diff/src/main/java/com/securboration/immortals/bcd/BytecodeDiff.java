package com.securboration.immortals.bcd;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import com.securboration.immortals.bcd.printer.MethodPrinter;
import com.securboration.immortals.bcd.util.BytecodeUtils;

public class BytecodeDiff {
    
    public static void main(String[] args) throws Exception {
        args = new String[]{//TODO
                new File("C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\cp-eval-service\\eval-out\\ess\\ess\\client\\target\\immortals-cp3.1-client-1.0.0.jar").getCanonicalPath(),
                new File("C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\cp-eval-service\\eval-out\\ess\\ess\\client\\target\\immortals-cp3.1-client-1.0.0MODIFIED.jar").getCanonicalPath()
        };//TODO
        
        if(args.length != 2){
            throw new RuntimeException(
                "expected exactly two args, left and right, that each " +
                "point to either a file containing a compiled java construct " +
                "(.class, .jar) or to a directory containing such files"
                );
        }
        
        final File left = new File(args[0]);//TODO
        final File right = new File(args[1]);//TODO
        
        System.out.println(diffJars(left,right));
    }
    
    public static String diffJars(
            final File leftJar, 
            final File rightJar
            ) throws IOException{
        final IndexingVisitor l = traverse(leftJar);
        final IndexingVisitor r = traverse(rightJar);
        
        return classpathDiff(l,r);
    }
    
    private static IndexingVisitor traverse(
            final File input 
            ) throws IOException{

        final byte[] data = FileUtils.readFileToByteArray(input);
        
        IndexingVisitor v = new IndexingVisitor(input);
        
        ClasspathTraverser.traverse(data, v);
        
        return v;
    }
    
    private static String classpathDiff(
            final IndexingVisitor left, 
            final IndexingVisitor right
            ) throws IOException {
        final Set<String>  onlyInLeft = new LinkedHashSet<>();{
            onlyInLeft.addAll(left.classpath.keySet());
            onlyInLeft.removeAll(right.classpath.keySet());
        }
        final Set<String> onlyInRight = new LinkedHashSet<>();{
            onlyInRight.addAll(right.classpath.keySet());
            onlyInRight.removeAll(left.classpath.keySet());
        }
        
        final StringBuilder sb = new StringBuilder();
        
        $a(sb, 0, "only in L:");
        for(final String cpe:onlyInLeft){
            final byte[] l = left.classpath.get(cpe);
            $a(
                sb,
                1,
                "%s was deleted in R (-%dB)",
                cpe,
                l.length
                );
            diff(sb,cpe,left.src,l,right.src,null);
        }
        
        $a(sb, 0, "only in R:");
        for(final String cpe:onlyInRight){
            final byte[] r = right.classpath.get(cpe);
            $a(
                sb,
                1,
                "%s was added in R (+%dB)",
                cpe,
                r.length
                );
            diff(sb,cpe,left.src,null,right.src,r);
        }
        
        $a(sb, 0, "in both:");
        final Set<String> inBoth = new LinkedHashSet<>();{
            inBoth.addAll(left.classpath.keySet());
            inBoth.retainAll(right.classpath.keySet());
        }
        
        for(final String cpe:inBoth){
            final byte[] l = left.classpath.get(cpe);
            final byte[] r = right.classpath.get(cpe);
            
            final String lHash = hash(l);
            final String rHash = hash(r);
            
            if(!lHash.equals(rHash)){
                
                $a(
                    sb,
                    1,
                    "%s was modified in R (L=%-6dB, R=%-6dB)",
                    cpe, 
                    l.length, 
                    r.length,
                    lHash,
                    rHash
                    );
                
//                sb.append(String.format(
//                    "\t%s was modified (L=%-6dB, R=%-6dB)(L=%s, R=%s)\n",
//                    cpe, 
//                    l.length, 
//                    r.length,
//                    lHash,
//                    rHash
//                    ));
                
                diff(sb,cpe,left.src,l,right.src,r);
            }
        }
        
        return sb.toString();
    }
    
    private static void $a(
            final StringBuilder sb, 
            final int tabDepth, 
            final String format,
            final Object...args
            ){
        $aInternal(sb,tabDepth,'\t',String.format(format, args));
    }
    
    private static void $aInternal(
            final StringBuilder sb, 
            final int tabDepth, 
            final char tab,
            final String s
            ){
        
        final StringBuilder lineTab = new StringBuilder();
        for(int i=0;i<tabDepth;i++){
            lineTab.append(tab);
        }
        
        final String tabString = lineTab.toString();
        
        sb.append(tabString + s.replace("\n", "\n" + tabString) + "\n");
    }
    
    private static boolean areDiffsInteresting(
            final String left, 
            final String right
            ){
        
        if(left.length() == right.length()){
            //heuristic: if the length is the same, 
            //            the bytecode is *probably* isomorphic
            return false;
        }
        
        try{
            final List<String> leftLines = Arrays.asList(TextWizard.lines(left));
            final List<String> rightLines = Arrays.asList(TextWizard.lines(right));
            
            Patch<String> patch = DiffUtils.diff(leftLines, rightLines);
            for(AbstractDelta<String> delta : patch.getDeltas()) {
                final List<String> src = delta.getSource().getLines();
                final List<String> dst = delta.getTarget().getLines();
                
                if(dst.isEmpty()){//heuristic 1
                    boolean interesting = false;
                    
                    for(String line:src){
                        line = line.trim();
                        
                        if(line.startsWith("L")){
                            continue;
                        }
                        
                        if(line.startsWith("LOCALVARIABLE")){
                            continue;
                        }
                        
                        if(line.startsWith("FRAME")){
                            continue;
                        }
                        
                        if(line.startsWith("LINENUMBER")){
                            continue;
                        }
                        
                        interesting = true;
                    }
                    return interesting;
                }
                
//                {//TODO
//                    System.out.println("left = " + left);
//                    System.out.println(src);
//                    
//                    System.out.println("right = " + right);
//                    System.out.println(dst);
//                    
//                    System.out.printf(String.format("deltas %d,%d= %s\n",left.length(),right.length(),delta));
//                    
//                    if(true)throw new RuntimeException("intentional");//TODO
//                }
                
                
            }
            
            return true;
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    
    private static void diff(
            final StringBuilder sb,
            final File lSrc,
            final ClassNode l,
            final File rSrc,
            final ClassNode r
            ) throws IOException{
        final Map<String,MethodNode> lMethods = decompose(l);
        final Map<String,MethodNode> rMethods = decompose(r);
        
        final Set<String> all = new LinkedHashSet<>();
        all.addAll(lMethods.keySet());
        all.addAll(rMethods.keySet());
        
        for(String methodName:all){
            if(lMethods.containsKey(methodName) && rMethods.containsKey(methodName)){
                //in both
                final MethodNode lMethod = lMethods.get(methodName);
                final MethodNode rMethod = rMethods.get(methodName);
                
                final String lString;
                final String rString;
                
                {//print using Jimple
                    rString = MethodPrinter.printJimple(rSrc,r,rMethod);
                    lString = MethodPrinter.printJimple(lSrc,l,lMethod);
                }
                
//                {//print using raw bytecode
//                    lString = "    " + MethodPrinter.print(lMethod);
//                    rString = "    " + MethodPrinter.print(rMethod);
//                }
                
                if(lString.equals(rString)){
                    continue;//no need to print diff, methods are unchanged
                } else {
                    
                    {
                        final String llString = MethodPrinter.print(lMethod);
                        final String rrString = MethodPrinter.print(rMethod);
                        
                        if(llString.equals(rrString)){
                            continue;
                        }
                        
                        if(!areDiffsInteresting(
                            TextWizard.textAfterSigil(lString.replace("$", ""),"\r\n\r\n","\n\n"),
                            TextWizard.textAfterSigil(rString.replace("$", ""),"\r\n\r\n","\n\n")
                            )){
                            continue;
                        }
                        
                        if(!areDiffsInteresting(llString,rrString)){
                            continue;
                        }
                        
//                        System.out.println(methodName);
//                        System.out.println("left " + llString);
//                        System.out.println("right " + rrString);
                    }
                }
                
                final String methodDiff = TextWizard.multiColumn(
                    lString,
                    rString,
                    100
                    );
                
                $a(
                    sb,
                    2,
                    "method %s was changed:",
                    methodName
                    );
                $a(
                    sb,
                    3,
                    "%s",
                    methodDiff
                    );
            } else if(lMethods.containsKey(methodName) && !rMethods.containsKey(methodName)){
                //only in L
                final MethodNode lMethod = lMethods.get(methodName);
                
                $a(
                    sb,
                    2,
                    "method %s was deleted in R",
                    methodName
                    );
            } else if(!lMethods.containsKey(methodName) && rMethods.containsKey(methodName)){
                //only in R
                final MethodNode rMethod = rMethods.get(methodName);
                final String rString = MethodPrinter.printJimple(rSrc,r,rMethod);
                
                final String methodDiff = TextWizard.singleColumn(
                    rString,
                    100
                    );
                
                $a(
                    sb,
                    2,
                    "method %s was added in R:",
                    methodName
                    );
                
                $a(
                    sb,
                    3,
                    "%s",
                    methodDiff
                    );
            } else {
                throw new RuntimeException("unhandled corner case for method " + methodName);
            }
        }
    }
    
    private static Map<String,MethodNode> decompose(ClassNode cn){
        Map<String,MethodNode> methods = new LinkedHashMap<>();
        if(cn == null || cn.methods == null){
            return methods;
        }
        
        for(MethodNode mn:cn.methods){
            methods.put(mn.name + " " + mn.desc, mn);
        }
        
        return methods;
    }
    
    private static void diff(
            final StringBuilder sb,
            final String classpathName,
            final File lSrc,
            final byte[] l, 
            final File rSrc,
            final byte[] r
            ) throws IOException{
        if(!classpathName.endsWith(".class")){
            
            final String lString = l == null ? "" : String.format(
                "[%dB binary content with hash %s]",
                l == null ? -1 : l.length,
                hash(l)
                );
            
            final String rString = r == null ? "" : String.format(
                "[%dB binary content with hash %s]",
                r == null ? -1 : r.length,
                hash(r)
                );
            
            final String diff = TextWizard.multiColumn(
                lString,
                rString,
                100
                );
            
            $a(
                sb,
                3,
                diff
                );
            
            return;
        }
        
        final ClassNode lCn = l == null ? null : BytecodeUtils.getClassNode(l);
        final ClassNode rCn = r == null ? null : BytecodeUtils.getClassNode(r);
        
        diff(sb,lSrc,lCn,rSrc,rCn);
        
        if(false){
            //build simple lists of the lines of the two testfiles
            final List<String> lLines = MethodPrinter.printClassLines(lCn);
            final List<String> rLines = MethodPrinter.printClassLines(rCn);
    
            try{
                Patch<String> patch = DiffUtils.diff(lLines, rLines);
                for(AbstractDelta<String> delta : patch.getDeltas()) {
                    sb.append(String.format("\t\t%s\n",delta));
                }
            } catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    private static String hash(final byte[] data) {
        if(data == null){
            return null;
        }
        
        try{
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(data);
            
            final StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    private static class IndexingVisitor implements IClasspathVisitor {
        private final File src;
        
        private final Map<String,byte[]> classpath = new LinkedHashMap<>();
        
        private long byteCount = 0L;
        private long classCount = 0L;
        private long elementCount = 0L;
        
        public IndexingVisitor(final File src){
            this.src = src;
        }

        @Override
        public void visitClasspathElement(
                String classpathName,
                byte[] classpathData
                ) {
            {//filters
                if(classpathName.endsWith(".jar")){
                    return;
                }
            }//end filters
            
            classpath.put(classpathName, classpathData);
            
            {//update counters
                if(classpathName.endsWith(".class")){
                    classCount++;
                }
                
                elementCount++;
                
                byteCount += classpathData.length;
            }
        }

        @Override
        public void beforeTraversal() {
            //TODO
        }

        @Override
        public void afterTraversal() {
            //TODO
            
            System.out.printf(
                "%d elements on classpath (%d classes, %d other), %dB total (JAR elements are decompressed before computing element sizes)\n", 
                elementCount,
                classCount,
                elementCount - classCount,
                byteCount
                );
//            for(String key:classpath.keySet()){
//                System.out.printf("\t%s (%dB)\n",key,classpath.get(key).length);
//            }
        }
    }
    
    
    private static class TextWizard{
        
        public static String textAfterSigil(
                final String s, 
                final String...sigils){
            
            String sigil = null;
            for(int i=0;i<sigils.length;i++){
                if(s.contains(sigils[i])){
                    sigil = sigils[i];
                    break;
                }
            }
            
            if(sigil == null){
                System.out.println("no found");
                
                return s;
            }
            
            final int index = s.indexOf(sigil);
            if(index < 0){
                return s;
            }
            
            return s.substring(index + sigil.length());
        }
        
        public static String multiColumn(
                final String l, 
                final String r,
                final int maxWidth
                ){
            final String[] lLines = lines(l);
            final String[] rLines = lines(r);
            
            final int maxLines = Math.max(lLines.length, rLines.length);
            
            final StringBuilder sb = new StringBuilder();
            
            final String formatString = String.format("%%-%ds", maxWidth);
            
            for(int i=0;i<maxLines;i++){
                final List<String> leftLines = new ArrayList<>();
                final List<String> rightLines = new ArrayList<>();
                
                {//L
                    final String leftLine = i >= lLines.length ? "" : lLines[i];
                    for(String s:multiLine(leftLine,maxWidth)){
                        leftLines.add(String.format(formatString, s));
                    }
                }
                
                {//R
                    final String rightLine = i >= rLines.length ? "" : rLines[i];
                    for(String s:multiLine(rightLine,maxWidth)){
                        rightLines.add(String.format(formatString, s));
                    }
                }
                
                final int maxLocalLines  = Math.max(leftLines.size(), rightLines.size());
                for(int j=0;j<maxLocalLines;j++){
                    final String leftLine = j >= leftLines.size() ? "" : leftLines.get(j);
                    final String rightLine = j >= rightLines.size() ? "" : rightLines.get(j);
                    
                    sb.append(String.format("%04x:  ", i));
                    sb.append(String.format(formatString, leftLine));
                    sb.append("   ");
                    sb.append(String.format(formatString, rightLine));
                    sb.append("\n");
                }
            }
            
            
            
            return sb.toString();
        }
        
        public static String singleColumn(
                final String l,
                final int maxWidth
                ){
            final String[] lLines = lines(l);
            
            final int maxLines = lLines.length;
            
            final StringBuilder sb = new StringBuilder();
            
            final String formatString = String.format("%%-%ds", maxWidth);
            
            for(int i=0;i<maxLines;i++){
                final List<String> leftLines = new ArrayList<>();
                
                {//L
                    final String leftLine = i >= lLines.length ? "" : lLines[i];
                    for(String s:multiLine(leftLine,maxWidth)){
                        leftLines.add(String.format(formatString, s));
                    }
                }
                
                final int maxLocalLines  = leftLines.size();
                for(int j=0;j<maxLocalLines;j++){
                    final String leftLine = j >= leftLines.size() ? "" : leftLines.get(j);
                    
                    sb.append(String.format("%04x:  ", i));
                    sb.append(String.format(formatString, leftLine));
                    sb.append("\n");
                }
            }
            
            
            
            return sb.toString();
        }
        
        private static List<String> multiLine(
                final String s, 
                final int maxWidth
                ){
            if(s.length() <= maxWidth){
                return Arrays.asList(s);
            }
            
            final List<String> lines = new ArrayList<>();
            int charIndex = 0;
            int chunkCount = 0;
            boolean stop = false;
            StringBuilder tmp = new StringBuilder();
            while(!stop){
                if(chunkCount == maxWidth){
                    lines.add(tmp.toString());
                    chunkCount = 0;
                    tmp = new StringBuilder();
                }
                
                tmp.append(s.charAt(charIndex));
                
                chunkCount++;
                charIndex++;
                if(charIndex == s.length()){
                    lines.add(tmp.toString());
                    stop = true;
                }
            }
            
            return lines;
        }
        
        private static String[] lines(final String s){
            return s.split("\\r?\\n");
        }
    }

}


