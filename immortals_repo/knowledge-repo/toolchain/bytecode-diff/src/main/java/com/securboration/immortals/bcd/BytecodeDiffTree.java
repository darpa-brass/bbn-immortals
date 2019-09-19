package com.securboration.immortals.bcd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class BytecodeDiffTree {
    
    public static void main(String[] args) throws Exception {
        args = new String[]{//TODO
                new File("C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\cp-eval-service\\eval-out\\immortals-cp3.1-client-1.0.0.jar").getCanonicalPath(),
                new File("C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\cp-eval-service\\eval-out\\immortals-cp3.1-client-1.0.0MODIFIED.jar").getCanonicalPath()
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
    
    private static class FigureVector{
        private final List<String> keys = new ArrayList<>();
        private final List<List<Object>> values = new ArrayList<>();
        
        private FigureVector(final String...keys){
            for(String key:keys){
                this.keys.add(key);
                this.values.add(new ArrayList<>());
            }
        }
        
        private void add(Object...vs){
            if(vs.length != keys.size()){
                throw new RuntimeException("expected to get " + keys.size() + " but got " + vs.length);
            }
//            System.out.println("keys = " + keys);
//            System.out.println("vals = " + Arrays.asList(vs));
            
            for(int i=0;i<keys.size();i++){
                this.values.get(i).add(vs[i]);
            }
        }
        
        private String toVector(final String indent){
            final StringBuilder sb = new StringBuilder();
            for(int i=0;i<keys.size();i++){
                final String key = keys.get(i);
                final List<Object> values = this.values.get(i);
                
//                labels=[ "Eve", "Cain", "Seth", "Enos", "Noam", "Abel", "Awan", "Enoch", "Azura"],
//                parents=["",    "Eve",  "Eve",  "Seth", "Seth", "Eve",  "Eve",  "Awan",  "Eve" ],
//                values=[  65,    14,     12,     10,     2,      6,      6,      4,       4],

                
                sb.append(indent);
                sb.append(String.format("%s=[ ",key));
                for(int j=0;j<values.size();j++){
                    if(j > 0){
                        sb.append(",");
                    }
                    
                    final Object o = values.get(j);
                    
                    if(o instanceof String){
                        sb.append(String.format("\"%s\"", o));
                    } else {
                        sb.append(String.format("%s", o));
                    }
                }
                sb.append(String.format("],\n"));
            }
            return sb.toString();
        }
    }
    
    private static class PackageHistogram{
        private final Set<String> initialKeys = new LinkedHashSet<>();
        private final Map<String,Long>  histogram = new TreeMap<>();
        
        private final int maxDepth;
        
        private PackageHistogram(final int maxDepth){
            this.maxDepth = maxDepth;
        }
        
        private void increment(
                final String key,
                final long increment
                ){
            Long current = histogram.get(key);
            if(current == null){
                current = 0L;
            }
            
            histogram.put(key, current+increment);
        }
        
        private void visit(final String[] keys, final long value){
            //e.g., com securboration pkg ClassName MethodNameDesc
            
            StringBuilder sb = new StringBuilder();
            sb.append("app");
            increment(sb.toString(),value);
            
            for(int i=0;i<maxDepth && i<keys.length;i++){
                sb.append("/");
                sb.append(keys[i]);
                
                increment(sb.toString(),value);
            }
            
            initialKeys.add(sb.toString());
        }
        
        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            
            for(String key:histogram.keySet()){
                String[] parts = key.split("/");
                if(parts.length == 1){
                    parts = new String[]{"",parts[0]};
                }
                
//                final StringBuilder parentPart = new StringBuilder();
//                for(int i=0;i<=parts.length-2;i++){
//                    if(i > 0){
//                        parentPart.append("/");
//                    }
//                    parentPart.append(parts[i]);
//                }
                
                final String parentPart = parts[parts.length-2];
                final String thisPart = parts[parts.length-1];
                
                sb.append(String.format("%s\t%s\t%d\n", thisPart, parentPart, histogram.get(key)));
            }
            
            return sb.toString();
        }
        
        
        public String figureVector(){
            final FigureVector v = new FigureVector("ids","labels","parents","values");
            
            final double size = histogram.get("app");
            for(String key:histogram.keySet()){
                String[] parts = key.split("/");
                if(parts.length == 1){
                    parts = new String[]{"",parts[0]};
                }
                
                final String parentPart = key.contains("/") ? key.substring(0,key.lastIndexOf("/")) : "";
                final String thisPart = key.contains("/") ? key.substring(key.lastIndexOf("/")+1) : key;
                
                final long thisValue = histogram.get(key);
                
//                final double ratio = (1d*thisValue) / size;
//                
//                //TODO: this filters nicely but don't always want it
//                if(ratio < 0.001){
//                    continue;
//                }
                
                v.add(key,thisPart,parentPart,thisValue);
            }
            
            return v.toVector("    ");
        }
        
        public String deltaHistogram(
                final PackageHistogram prior,
                final String suffix,
                final Set<String> alreadyIncluded
                ){
            final List<String> packages = new ArrayList<>();
            final List<Long> deltas = new ArrayList<>();
            
            for(String key:histogram.keySet()){
                if(alreadyIncluded != null && alreadyIncluded.contains(key)){
                    continue;
                }
                
                if(suffix != null && !key.endsWith(suffix)){
                    continue;
                }
                
                if(alreadyIncluded != null){
                    alreadyIncluded.add(key);
                }
                
                long thisValue = histogram.get(key);
                Long otherValue = prior.histogram.get(key);
                boolean added = false;
                if(otherValue == null){
                    otherValue = 0L;
                    added = true;
                }
                
                final long delta = thisValue - otherValue;
                
                if(delta == 0L && !added){
                    //not interesting
                    continue;
                }
                
//                if(delta < 0){
//                    continue;
//                }
                
                if(!key.contains(".")){
                    continue;
                }
                
                packages.add(key);
                deltas.add(delta);
            }
            
            final StringBuilder sb = new StringBuilder();
            
            //x=['giraffes', 'orangutans', 'monkeys'], y=[20, 14, 23]
            
            sb.append("x=[");
            for(int i=0;i<packages.size();i++){
                final String pkg = packages.get(i);
                
                if(i>0){
                    sb.append(",");
                }
                sb.append("'");
                sb.append(pkg);
                sb.append("'");
            }
            sb.append("], y=[");
            for(int i=0;i<deltas.size();i++){
                final long delta = deltas.get(i);
                
                if(i>0){
                    sb.append(",");
                }
                sb.append(delta);
            }
            sb.append("])");
            
            return sb.toString();
        }
        
        public String deltaFromPrior(final PackageHistogram prior){
            final FigureVector v = new FigureVector("ids","labels","parents","values");
            
            for(String key:histogram.keySet()){
                String[] parts = key.split("/");
                final String parentPart = parts.length == 1 ? "" : key.substring(0, key.lastIndexOf("/"));
                final String thisPart = parts[parts.length-1];
                
                long thisValue = histogram.get(key);
                Long otherValue = prior.histogram.get(key);
                boolean added = false;
                if(otherValue == null){
                    otherValue = 0L;
                    added = true;
                }
                
                final long delta = thisValue - otherValue;
                
                if(delta == 0L && !added){
                    //not interesting
                    continue;
                }
                
                if(delta < 0){
                    continue;
                }
                
                v.add(key,thisPart,parentPart,thisValue);
                
//                sb.append(String.format("%s\t%s\t%d\t%s\t%1.4f\n", thisPart, parentPart, Math.abs(delta), key, deltaRatio));
            }
            
            return v.toVector("    ");
        }
        
        
        public String donutDiff(
                final PackageHistogram prior
                ){
            final Set<String> all = new LinkedHashSet<>();
            all.addAll(prior.histogram.keySet());
            all.addAll(histogram.keySet());
            
            final Set<String> allInitialKeys = new LinkedHashSet<>();
            allInitialKeys.addAll(prior.initialKeys);
            allInitialKeys.addAll(initialKeys);
            
            all.retainAll(allInitialKeys);
            
            final Set<String> unchanged = new LinkedHashSet<>();
            final Set<String> modified = new LinkedHashSet<>();
            final Set<String> deleted = new LinkedHashSet<>();
            final Set<String> added = new LinkedHashSet<>();
            
            for(String key:all){
                if(prior.histogram.containsKey(key)){
                    //something that existed before
                    
                    if(!histogram.containsKey(key)){
                        //it was deleted
                        deleted.add(key);
                    } else {
                        //it exists in both versions
                        final long oldSize = prior.histogram.get(key);
                        final long newSize = histogram.get(key);
                        
                        if(oldSize == newSize){
                            unchanged.add(key);
                        } else {
                            modified.add(key);
                        }
                    }
                } else {
                    //it was added
                    added.add(key);
                }
            }
            
            unchanged.retainAll(allInitialKeys);
            modified.retainAll(allInitialKeys);
            deleted.retainAll(allInitialKeys);
            added.retainAll(allInitialKeys);
            
            final PackageHistogram newHistogram = new PackageHistogram(Integer.MAX_VALUE);
            
            for(String s:unchanged){
                final String name = s.substring(4);//app/
                final String newName = "unchanged/" + name;
                newHistogram.visit(newName.split("/"), histogram.get(s));
            }
            
            for(String s:added){
                final String name = s.substring(4);//app/
                final String newName = "added/" + name;
                newHistogram.visit(newName.split("/"), histogram.get(s));
            }
            
            for(String s:deleted){
                final String name = s.substring(4);//app/
                final String newName = "deleted/" + name;
                newHistogram.visit(newName.split("/"), prior.histogram.get(s));
            }
            
            for(String s:modified){
                final String name = s.substring(4);//app/
                final String newName = "modified/" + name;
                newHistogram.visit(newName.split("/"), histogram.get(s));
            }
            
            System.out.printf("%d elements were unchanged: %s\n", unchanged.size(), new ArrayList<>(unchanged).subList(0, Math.min(20,unchanged.size())));
            System.out.printf("%d elements were modified: %s\n", modified.size(), new ArrayList<>(modified).subList(0, Math.min(20,modified.size())));
            System.out.printf("%d elements were deleted: %s\n", deleted.size(), new ArrayList<>(deleted).subList(0, Math.min(20,deleted.size())));
            System.out.printf("%d elements were added: %s\n", added.size(), new ArrayList<>(added).subList(0, Math.min(20,added.size())));
            
            return newHistogram.figureVector();
        }
        
    }
    
    public static String diffJars(
            final File leftJar, 
            final File rightJar
            ) throws IOException{
        final IndexingVisitor l = traverse(leftJar);
        final IndexingVisitor r = traverse(rightJar);
        
        final String structure = r.histogram.donutDiff(l.histogram);
//        final String diff = r.histogram.deltaFromPrior(l.histogram);
        
        final Set<String> included = new LinkedHashSet<>();
        final String diffClasses = r.histogram.deltaHistogram(l.histogram,".class",included);
        final String diffOther = r.histogram.deltaHistogram(l.histogram,null,included);//everything else
        
        final String plotlyPy = PlotlyHelper.getPlotlypy(structure, diffClasses, diffOther);
        
//        System.out.println(r.histogram.donutDiff(l.histogram));//TODO
        
//        System.out.println(plotlyPy);
        
        
        if(false){//TODO
            System.out.println("LEFT:\n");
            System.out.println(l.coarseHistogram.toString());
            System.out.println("RIGHT:\n");
            System.out.println(r.coarseHistogram.toString());
            
            System.out.println("FULL:\n");
            System.out.println(r.histogram.figureVector());
            
            
            
            System.out.println("RIGHT(2):\n");
            System.out.println(r.coarseHistogram.figureVector());
            
            System.out.println("DIFF:\n");
            System.out.println(r.histogram.deltaFromPrior(l.histogram));
//            System.out.println("DIFF2:\n");
//            System.out.println(r.histogram.deltaFromPriorNormalized(l.histogram));
            System.out.println("DIFF_h:\n");
//            System.out.println(r.histogram.deltaHistogram(l.histogram));
        }
        
        return plotlyPy;
    }
    
    private static IndexingVisitor traverse(
            final File input 
            ) throws IOException{

        final byte[] data = FileUtils.readFileToByteArray(input);
        
        IndexingVisitor v = new IndexingVisitor(input);
        
        ClasspathTraverser.traverse(data, v);
        
        return v;
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
    
    
    
    private static class IndexingVisitor implements IClasspathVisitor {
        private final File src;
        
        private final Map<String,byte[]> classpath = new LinkedHashMap<>();
        
        private long byteCount = 0L;
        private long classCount = 0L;
        private long elementCount = 0L;
        
        private final PackageHistogram histogram = new PackageHistogram(Integer.MAX_VALUE);
        private final PackageHistogram coarseHistogram = new PackageHistogram(4);
        
        public IndexingVisitor(final File src){
            this.src = src;
        }
        
        private String[] decomposeClasspathName(final String classpathName){
            return classpathName.split("/");
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
            
            {//update histogram
                histogram.visit(
                    decomposeClasspathName(classpathName), 
                    classpathData.length
                    );
                
                coarseHistogram.visit(
                    decomposeClasspathName(classpathName), 
                    classpathData.length
                    );
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
    
    private static class PlotlyHelper{
        public static String getPlotlypy(
                final String structure,
                final String diffHclasses,
                final String diffHother
                ){
            
            return template
                    .replace("${STRUCTURE_TRACE}", structure)
                    .replace("${DELTA_HISTOGRAM_CLASSES}", diffHclasses)
                    .replace("${DELTA_HISTOGRAM_OTHER}", diffHother)
                    ;
        }
        
        private static final String template = 
                "#NOTE: THIS FILE HAS BEEN AUTO-GENERATED FROM A TEMPLATE\r\n" + 
                "#pip install plotly\r\n" + 
                "#pip install plotly-orca\r\n" + 
                "\r\n" + 
                "import plotly.offline as py\r\n" + 
                "import plotly.graph_objs as go\r\n" + 
                "from plotly.subplots import make_subplots\r\n" + 
                "\r\n" + 
                "structureTrace = go.Sunburst(\r\n" + 
                "${STRUCTURE_TRACE}\r\n" + 
                "    branchvalues=\"total\",\r\n" + 
                "    outsidetextfont = {\"size\": 20, \"color\": \"#377eb8\"},\r\n" + 
                "    marker = {\"line\": {\"width\": 2}},\r\n" + 
                "    maxdepth = 6\r\n" + 
                ")\r\n" + 
                "\r\n" + 
                "deltaHistogramClasses = go.Bar(name='.class', ${DELTA_HISTOGRAM_CLASSES}\r\n" +
                "deltaHistogramOther = go.Bar(name='other', ${DELTA_HISTOGRAM_OTHER}\r\n" + 
                "\r\n" + 
                "fig = make_subplots(\r\n" + 
                "    rows=1, cols=2, \r\n" + 
                "    specs=[[{\"type\": \"sunburst\"}, {\"type\": \"bar\"}]],\r\n" + 
                "    subplot_titles=('application structure after repair','application structure deltas (in bytes)')\r\n" + 
                ")\r\n" + 
                "\r\n" + 
                "fig.add_trace(\r\n" + 
                "    structureTrace,\r\n" + 
                "    row=1, col=1\r\n" + 
                ")\r\n" + 
                "\r\n" + 
                "fig.add_trace(\r\n" + 
                "    deltaHistogramClasses,\r\n" + 
                "    row=1, col=2\r\n" + 
                ")\r\n" + 
                "\r\n" + 
                "fig.add_trace(\r\n" + 
                "    deltaHistogramOther,\r\n" + 
                "    row=1, col=2\r\n" + 
                ")\r\n" + 
                "\r\n" + 
                "fig.update_layout(barmode='group')\r\n" + 
                "fig.update_xaxes(showticklabels=False)\r\n" + 
                "\r\n" + 
                "py.offline.plot(fig, filename = 'bytecodeDiff.html', auto_open=False, config={\r\n" + 
                "    'scrollZoom': True,\r\n" + 
                "    'displayModeBar': True,\r\n" + 
                "    'editable': False\r\n" + 
                "})\r\n" + 
                "\r\n" + 
                "";
    }

}


