package com.securboration.schemavalidator.main;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securboration.schemavalidator.SchemaComplianceChecker;
import com.securboration.schemavalidator.SchemaComplianceChecker.BadnessReport;
import com.securboration.schemavalidator.SchemaComplianceChecker.BadnessReportElement;
import com.securboration.test.Document;
import com.securboration.test.DocumentSet;
import com.securboration.test.TranslationProblemDefinition;
import com.securboration.test.XsdtsClient;
import com.securboration.test.XsltTransformer;

public class Main {
    
    public static void main(String[] args) throws Exception {
        if(false){
            System.setProperty("translationProblemsDir", "C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\xsd-translation-service-test\\output\\translationProblemInstances");
            System.setProperty("clientUrl", "http://localhost:8090/xsdsts");
            
            System.setProperty("resultsDir", "./results/zzz");
            
            System.setProperty("translationProblemsPrefix", "MDL_v0_8_17-MDL_v0_8_19");
        }
        
        
        if(false){//TODO
            System.setProperty("translationProblemsDir", "C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\xsd-translation-service-test\\output\\translationProblemInstances");
            System.setProperty("clientUrl", "http://localhost:8090/xsdsts");
            
            System.setProperty("resultsDir", "./results/zzz");
            
        }//TODO
        
        
        
//        {//TODO
//            System.setProperty("srcSchema", "C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\xsd-translation-service-test\\zTest\\MDL_v0_8_17\\schema\\MDL_v0_8_17.xsd");
//            System.setProperty("dstSchema", "C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\xsd-translation-service-test\\zTest\\MDL_v0_8_19\\schema\\MDL_v0_8_19.xsd");
//            System.setProperty("srcDocs", "C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\xsd-translation-service-test\\zTest\\MDL_v0_8_17\\datasource");
//            System.setProperty("resultsDir", "./results/afterUpgrade2");
//            System.setProperty("clientUrl", "http://localhost:8090/xsdsts");
//        }//TODO
        
        
//      {//TODO
//          System.setProperty("srcSchema", "C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\xsd-translation-service-test\\zTest\\austin\\initial.xsd");
//          System.setProperty("dstSchema", "C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\xsd-translation-service-test\\zTest\\austin\\updated.xsd");
//          System.setProperty("srcDocs", "C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\xsd-translation-service-test\\zTest\\austin\\xml");
//          System.setProperty("resultsDir", "./results/testAustin");
//          System.setProperty("clientUrl", "http://localhost:8090/xsdsts");
//      }//TODO
        
        final String clientUrl = getProperty("clientUrl","base URL of a translation endpoint to test",null,false);
        final File resultsDir = getFileFromProperty("resultsDir","path to an output directory",new File("./results/" + System.currentTimeMillis()).getCanonicalPath(),true);
        
        final String testInstancesDir = getProperty("translationProblemsDir","path to a dir containing multiple translation problem instances to iterate through",null,true);
        
        final String testInstancesDirNamePrefix = getProperty("translationProblemsPrefix","prefix for names of translation problems",null,true);
        
        if(testInstancesDir != null){//we instead perform a batch-mode evaluation if this property is set
            main2(clientUrl,testInstancesDir,resultsDir,testInstancesDirNamePrefix);
            return;
        }
        
        //perform a one-off evaluation run
        
        final File srcDocsDir = getFileFromProperty("srcDocs","path to a dir containing dst-compliant XML docs",null,true);
        final File srcSchemaFile = getFileFromProperty("srcSchema","path to a src schema xsd",null,false);
        final File dstSchemaFile = getFileFromProperty("dstSchema","path to a src schema xsd",null,false);
        
        final MetricsTracker parent = new MetricsTracker("all translation problem instances");
        
        performTranslationTestInstance(
            parent,
            srcSchemaFile,
            dstSchemaFile,
            srcDocsDir,
            resultsDir,
            clientUrl
            );
        
        System.out.println("\n\n"+parent.toString());//TODO
    }
    
    private static void main2(
            final String clientUrl,
            final String testInstancesDir,
            final File resultsDir,
            final String testInstancesDirNamePrefix
            ) throws Exception{
        
        final File testInputDir = new File(testInstancesDir);
        
        final MetricsTracker parent = new MetricsTracker("all translation problem instances");
        
        for(File f:testInputDir.listFiles()){
            
            if(testInstancesDirNamePrefix != null && !f.getName().startsWith(testInstancesDirNamePrefix)){
                System.out.printf(
                    "skipping %s @ %s because it does not match prefix %s\n", 
                    f.getName(), 
                    f.getAbsolutePath(), 
                    testInstancesDirNamePrefix
                    );
                continue;
            }
            
            final File localResultsDir = new File(resultsDir,f.getName());
            
            if(!f.isDirectory()){
                continue;
            }
            
            System.out.printf("%s\n", f.getName());
            
            final File srcDocsDir = new File(f,"srcDocs");
            final File srcSchemaFile = detectMdlFile(new File(f,"srcSchema"));
            final File dstSchemaFile = detectMdlFile(new File(f,"dstSchema"));
            
            performTranslationTestInstance(
                parent,
                srcSchemaFile,
                dstSchemaFile,
                srcDocsDir,
                localResultsDir,
                clientUrl
                );
            
            System.out.println("\n\n"+parent.toString());
        }
        
        final File outputFile = new File(resultsDir,"metrics.dat");
        
        FileUtils.writeStringToFile(
            outputFile,
            parent.toString(), 
            StandardCharsets.UTF_8
            );
        
        System.out.printf(
            "dumped metrics.dat to %s\n", 
            outputFile.getAbsolutePath()
            );
    }
    
    private static File detectMdlFile(final File dir){
        for(File f:dir.listFiles()){
            if(f.isDirectory()){
                continue;
            }
            if(!f.isFile()){
                continue;
            }
            
            if(f.getName().startsWith("MDL_")){
                return f;
            }
        }
        
        throw new RuntimeException("no MDL xsd detected in " + dir.getAbsolutePath());
    }
    
    private static void performTranslationTestInstance(
            final MetricsTracker parentMetrics,
            final File srcSchemaFile, 
            final File dstSchemaFile, 
            final File srcDocsDir, 
            final File resultsDir,
            final String clientUrl
            ) throws Exception {
        parentMetrics.incrementValue("numTranslationProblemInstances");
        
        final MetricsTracker localMetrics = parentMetrics.getChild(
            String.format("%s-to-%s", srcSchemaFile.getName(), dstSchemaFile.getName())
            );
        
        final Report report = new Report();
        
        try{
            {
                report.$("%s\n\n",new Date().toString());
            }
            
            {//write config to report
                report.$("testing client %s\n", clientUrl);
                report.$("srcSchema =  %s\n", srcSchemaFile.getCanonicalPath());
                report.$("dstSchema =  %s\n", dstSchemaFile.getCanonicalPath());
                report.$("srcDocsDir = %s\n", srcDocsDir == null ? null : srcDocsDir.getCanonicalPath());
                report.$("resultsDir = %s\n", resultsDir.getCanonicalPath());
                report.$("\n\n");
            }
            
            final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            
            final Schema src = factory.newSchema(srcSchemaFile);
            final Schema dst = factory.newSchema(dstSchemaFile);
            
            report.$("checking src schema...\n");
            final Validator srcValidator = src.newValidator();
            report.$("src schema is valid\n\n");
            
            report.$("checking dst schema...\n");
            final Validator dstValidator = dst.newValidator();
            report.$("dst schema is valid\n\n");
            
            
            
            final List<File> docsToTranslate = new ArrayList<>();
            
            {
                final List<File> xml = new ArrayList<>();
                if(srcDocsDir != null){
                    xml.addAll(FileUtils.listFiles(srcDocsDir, new String[]{"xml"}, true));
                }
                report.$("validating %d documents discovered in input dir against src schema...\n",xml.size());
                for(File f:xml){
                    report.$("\tchecking %s...\n",f.getCanonicalPath());
                    
                    try(FileInputStream fis = new FileInputStream(f)){
                        
                        srcValidator.validate(new StreamSource(fis));
                        report.$("\t\tthe document is compliant with the src schema\n");
                        
                        if(false)
                        {//TODO: enable this block if you want to allow already-compliant documents to be included
                            try(FileInputStream fis2 = new FileInputStream(f)){
                                dstValidator.validate(new StreamSource(fis2));
                                report.$("\t\tthe document is already compliant with the dst schema and will be excluded during testing\n");
                                
                                continue;//continue on to the next document since this one is trivially conformant and therefore uninteresting for testing
                            } catch(SAXException e){
                                report.$("\t\tthe document is NOT compliant with the dst schema\n");
                                //do nothing, we expected an error here
                            }
                        }
                        
                        report.$("\t\tthe document is interesting and will be included during testing\n");
                        
                        docsToTranslate.add(f);
                    } catch(Exception e){
                        System.out.println(f.getAbsolutePath());
                        e.printStackTrace(System.out);
                        
                        report.$("\t\tthe document is not compliant with the src schema and will be excluded during testing");
                        report.$(" because %s\n",e.getMessage());
                    }
                }
            }
            
            report.$("\n");
            report.$("found %d good input documents to translate\n",docsToTranslate.size());
            
            report.$("\n");
            report.$("testing interaction with client @ %s\n",clientUrl);
            
            final XsdtsClient client = new XsdtsClient(clientUrl);
            
            {
                report.$("\ntesting ping\n");
                for(int i=0;i<10;i++){
                    final long start = client.ping();
                    final long end = client.ping();
                    report.$("\tclient ping is %dms\n",end - start);
                }
            }
            
            {
                report.$("\nperforming baseline badness check\n");
                
                {
                    report.$("\ntesting fidelity of schema translation\n");
                    
                    double normalizedBadness = 0d;
                    double weightedBadness = 0d;
                    double numPerfect = 0d;
                    double numProblematic = 0d;
                    int numDocuments = 0;
                    
                    for(File f:docsToTranslate){
                        try{
                            final String inputXml = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
                            //no translation
                            final String convertedXml = inputXml;
                            FileUtils.writeStringToFile(
                                new File(resultsDir,"output/" + f.getName()), 
                                convertedXml, 
                                StandardCharsets.UTF_8
                                );
                            
                            final BadnessReport r = SchemaComplianceChecker.getDocumentBadnessScore(
                                dstSchemaFile, 
                                convertedXml
                                );
                            
                            numDocuments++;
                            
                            localMetrics.addValue("badElementsCountBefore",r.getNumBadElements());
                            localMetrics.addValue("badLinesCountBefore",r.getWeightedBadnessScore());
                            localMetrics.addValue("badLinesFractionBefore",r.getBadnessScore());
                            
                            if(r.getBadnessScore() > 0){
                                normalizedBadness += r.getBadnessScore();
                                weightedBadness += r.getWeightedBadnessScore();
                                numProblematic += 1d;
                                
                                localMetrics.incrementValue("documentsTranslatedImperfectlyCountBefore");
                                report.$("\tthe translated document is NOT schema compliant because:\n%s\n", r.toString());
                                
                                for(BadnessReportElement bre:r.getReportElements()){
                                    localMetrics.incrementFrequency("validatorExceptionClassBefore",bre.getE().getClass().getName());//TODO
                                    localMetrics.incrementFrequency("validatorProblematicElementNameBefore", extractElementName(bre.getE()));
                                    localMetrics.incrementFrequency("validatorProblemDescBefore", bre.getE().getMessage());
                                }
                            } else {
                                numPerfect += 1d;
                                
                                localMetrics.incrementValue("documentsTranslatedPerfectlyCountBefore");
                                report.$("\tthe translated document is schema compliant\n");
                            }
                        } catch(Exception e){
                            System.out.println(f.getAbsolutePath());
                            e.printStackTrace(System.out);
                            
                            report.$("\t\tthe translated document is not compliant with the dst schema");
                            report.$(" because %s\n",e.getMessage());
                        }
                    }
                    
                    report.$(
                        "performed %d compliance checks against the target schema of which " +
                        "%d were perfect and %d were imperfect with " +
                        "normalized badness score %1.4f and " +
                        "weighted badness score %1.4f\n",
                        numDocuments,
                        (int)numPerfect,
                        (int)numProblematic,
                        normalizedBadness,
                        weightedBadness
                        );
                }
            }
            
            {//actually perform the translation
                final String xslt;
                {
                    report.$("\ntesting acquisition of schema translation\n");
                    
                    final TranslationProblemDefinition t = new TranslationProblemDefinition();
                    t.setSrcSchema(ClientHelper.getDocumentSetFromSchema(srcSchemaFile));
                    t.setDstSchema(ClientHelper.getDocumentSetFromSchema(dstSchemaFile));
                    
                    FileUtils.writeStringToFile(
                        new File(resultsDir,"service/translation-request.json"),
                        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(t),
                        StandardCharsets.UTF_8
                        );
                    
                    final long start = System.currentTimeMillis();
                    xslt = client.getXsdTranslation(t);
                    final long elapsed = System.currentTimeMillis() - start;
                    
                    FileUtils.writeStringToFile(
                        new File(resultsDir,"service/translation-response.xslt"),
                        xslt,
                        StandardCharsets.UTF_8
                        );
                    
                    localMetrics.addValue("xsltAcquisitionTimeMillis",elapsed);
                    localMetrics.addValue("xsltSizeBytes",xslt.getBytes().length);
                    
                    report.$("obtained a %dB XSLT in %dms\n",xslt.getBytes().length,elapsed);
                }
                
                {
                    report.$("\ntesting fidelity of schema translation\n");
                    
                    double normalizedBadness = 0d;
                    double weightedBadness = 0d;
                    double numPerfect = 0d;
                    double numProblematic = 0d;
                    int numDocuments = 0;
                    
                    for(File f:docsToTranslate){
                        localMetrics.incrementValue("documentsTranslatedCountAfter");
                        localMetrics.incrementValue("documentsTranslatedPerfectlyCountAfter",0L);
                        localMetrics.incrementValue("documentsTranslatedImperfectlyCountAfter",0L);
                        
                        report.$("\tusing %s...\n",f.getCanonicalPath());
                        
                        try{
                            final String inputXml = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
                            FileUtils.writeStringToFile(
                                new File(resultsDir,"input/" + f.getName()), 
                                inputXml, 
                                StandardCharsets.UTF_8
                                );
                            
                            final String convertedXml = XsltTransformer.translate(xslt, inputXml);
                            FileUtils.writeStringToFile(
                                new File(resultsDir,"output/" + f.getName()), 
                                convertedXml, 
                                StandardCharsets.UTF_8
                                );
                            
                            final BadnessReport r = SchemaComplianceChecker.getDocumentBadnessScore(
                                dstSchemaFile, 
                                convertedXml
                                );
                            
                            numDocuments++;
                            
                            localMetrics.addValue("badElementsCountAfter",r.getNumBadElements());
                            localMetrics.addValue("badLinesCountAfter",r.getWeightedBadnessScore());
                            localMetrics.addValue("badLinesFractionAfter",r.getBadnessScore());
                            
                            if(r.getBadnessScore() > 0){
                                normalizedBadness += r.getBadnessScore();
                                weightedBadness += r.getWeightedBadnessScore();
                                numProblematic += 1d;
                                
                                localMetrics.incrementValue("documentsTranslatedImperfectlyCountAfter");
                                report.$("\tthe translated document is NOT schema compliant because:\n%s\n", r.toString());
                                
                                for(BadnessReportElement bre:r.getReportElements()){
                                    localMetrics.incrementFrequency("validatorExceptionClassAfter",bre.getE().getClass().getName());//TODO
                                    localMetrics.incrementFrequency("validatorProblematicElementNameAfter", extractElementName(bre.getE()));
                                    localMetrics.incrementFrequency("validatorProblemDescAfter", bre.getE().getMessage());
                                }
                            } else {
                                numPerfect += 1d;
                                
                                localMetrics.incrementValue("documentsTranslatedPerfectlyCountAfter");
                                report.$("\tthe translated document is schema compliant\n");
                            }
                        } catch(Exception e){
                            System.out.println(f.getAbsolutePath());
                            e.printStackTrace(System.out);
                            
                            report.$("\t\tthe translated document is not compliant with the dst schema");
                            report.$(" because %s\n",e.getMessage());
                        }
                    }
                    
                    report.$(
                        "performed %d test translations of which " +
                        "%d were perfect and %d were imperfect with " +
                        "normalized badness score %1.4f and " +
                        "weighted badness score %1.4f\n",
                        numDocuments,
                        (int)numPerfect,
                        (int)numProblematic,
                        normalizedBadness,
                        weightedBadness
                        );
                }
            }
            
        } finally {
            {//dump the report
                final File reportFile = new File(resultsDir,"report.dat");
                
                FileUtils.writeStringToFile(reportFile, report.sb.toString(), StandardCharsets.UTF_8);
            }
        }
    }
    
    private static String extractElementName(Exception e){
        final String message = e.getMessage();
        
        if(!message.contains("Invalid content was found starting with element '")){
            return "unknown";
        }
        
        final String magic = "Invalid content was found starting with element '";
        
        final int startIndex = message.indexOf(magic) + magic.length();
        
        final int endIndex = message.indexOf("'",startIndex);
        
        return message.substring(startIndex,endIndex);
    }
    
    private static class MetricsTracker{
        
        private final String name;
        
        private final MetricsTracker parent;
        
        private final List<MetricsTracker> children = new ArrayList<>();
        
        private final Map<String,Frequency> frequencies = new LinkedHashMap<>();
        
        private final Map<String,DescriptiveStatistics> metrics = new LinkedHashMap<>();
        
        private final Map<String,Long> counters = new LinkedHashMap<>();
        
        
        //e.g., "problematicElementName" : "<module>"->1234, "<other>"->4567
        private void incrementFrequency(
                final String key,
                final String value
                ){
            if(parent != null){
                parent.incrementFrequency(key, value);
            }
            
            Frequency f = frequencies.get(key);
            if(f == null){
                f = new Frequency();
                frequencies.put(key,f);
            }
            
            f.addValue(value);
        }
        
        private static void addValueNumeric(
                final StringBuilder sb,
                final String metricName, 
                final double value
                ){
            sb.append(String.format(
                "%s = %1.4f, ",
                metricName,
                value
                ));
        }
        
        @Override
        public String toString(){
            final StringBuilder sb = new StringBuilder();
            
            toString(sb);
            
            return sb.toString();
        }
        
        private String getPath(){
            Stack<String> stack = new Stack<>();
            
            MetricsTracker current = this;
            while(current != null){
                stack.push(current.name);
                current = current.parent;
            }
            
            StringBuilder sb = new StringBuilder();
            
            boolean first = true;
            while(!stack.isEmpty()){
                if(first){
                    first = false;
                } else {
                    sb.append(" -> ");
                }
                
                sb.append("[" + stack.pop() + "]");
            }
            
            return sb.toString();
        }
        
        private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
            List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
            list.sort(Entry.comparingByValue());
            
            Collections.reverse(list);//sort descending

            Map<K, V> result = new LinkedHashMap<>();
            for (Entry<K, V> entry : list) {
                result.put(entry.getKey(), entry.getValue());
            }

            return result;
        }
        
        private void toString(StringBuilder sb){
            sb.append(String.format("metrics for context %s\n",getPath()));
            
            for(String key:counters.keySet()){
                final Long stats = counters.get(key);
                
                sb.append(String.format("counter \"%s\" = %d\n",key,stats));
            }
            for(String key:metrics.keySet()){
                final DescriptiveStatistics stats = metrics.get(key);
                
                sb.append(String.format("statistic \"%s\" ",key));
                
                addValueNumeric(sb,"arithmeticMean",stats.getMean());
                addValueNumeric(sb,"stdv",stats.getStandardDeviation());
                addValueNumeric(sb,"min",stats.getMin());
                addValueNumeric(sb,"max",stats.getMax());
                addValueNumeric(sb,"percentile1",stats.getPercentile(1d));
                addValueNumeric(sb,"percentile5",stats.getPercentile(5d));
                addValueNumeric(sb,"percentile15",stats.getPercentile(15d));
                addValueNumeric(sb,"percentile25",stats.getPercentile(25d));
                addValueNumeric(sb,"percentile50",stats.getPercentile(50d));
                addValueNumeric(sb,"percentile75",stats.getPercentile(50d));
                addValueNumeric(sb,"percentile85",stats.getPercentile(85d));
                addValueNumeric(sb,"percentile95",stats.getPercentile(95d));
                addValueNumeric(sb,"percentile99",stats.getPercentile(99d));
                addValueNumeric(sb,"skewness",stats.getSkewness());
                addValueNumeric(sb,"geometricMean",stats.getGeometricMean());
                
                sb.append("\n");
            }
            for(String key:frequencies.keySet()){
                sb.append(String.format("freq \"%s\" => ", key));
                
                final Frequency f = frequencies.get(key);
                
                final Map<Comparable<?>,Long> counts = new HashMap<>();
                Iterator<Comparable<?>> iterator = f.valuesIterator();
                while(iterator.hasNext()){
                    Comparable<?> c = iterator.next();
                    
                    final long count = f.getCount(c);
                    
                    counts.put(c, count);
                }
                
                Map<Comparable<?>,Long> sorted = sortByValue(counts);
                
                for(Comparable<?> k:sorted.keySet()){
                    sb.append(String.format("\"%s\" = %d, ",k,sorted.get(k)));
                }
                
                sb.append("\n");
            }
            
            
            sb.append("\n");
            
            for(MetricsTracker child:children){
                child.toString(sb);
            }
        }
        
        public void incrementValue(
                final String key
                ){
            incrementValue(key,1L);
        }
                
        
        public void incrementValue(
                final String key, 
                final long value
                ){
            if(parent != null){
                parent.incrementValue(key, value);
            }
            
            Long v = counters.get(key);
            if(v == null){
                v = 0L;
            }
            
            counters.put(key, v+value);
        }
        
        public void addValue(
                final String key, 
                final double value
                ){
            if(parent != null){
                parent.addValue(key, value);
            }
            
            DescriptiveStatistics s = metrics.get(key);
            
            if(s == null){
                s = new DescriptiveStatistics();
                metrics.put(key, s);
            }
            
            s.addValue(value);
        }

        public MetricsTracker(
                String name
                ) {
            this(name,null);
        }
        
        private MetricsTracker(
                String name,
                MetricsTracker parent
                ) {
            this.name = name;
            this.parent = parent;
        }
        
        public MetricsTracker getChild(String name){
            MetricsTracker child = new MetricsTracker(name,this);
            
            this.children.add(child);
            
            return child;
        }
    }
    
    private static class Report{
        final StringBuilder sb = new StringBuilder();
        
        void $(String format, Object...args){
            System.out.printf(format, args);
            sb.append(String.format(format, args));
        }
        
        void metricNumeric(
                String metricName, 
                Object metricValue
                ){
            $("%s, %s, number\n",metricName,metricValue);
        }
        
        void metricOther(
                String metricName, 
                Object metricValue
                ){
            $("%s, %s, other\n",metricName,metricValue);
        }
        
        void metricBoolean(
                String metricName, 
                Object metricValue
                ){
            $("%s, %s, boolean\n",metricName,metricValue);
        }
        
        void metricDiscrete(
                String metricName, 
                Object metricValue
                ){
            $("%s, %s, discrete\n",metricName,metricValue);
        }
    }
    
    private static File getFileFromProperty(
            final String key, 
            final String desc, 
            final String defaultValue,
            final boolean allowNull
            ){
        final String property = getProperty(key,desc,defaultValue,allowNull);
        
        if(property == null){
            return null;
        }
        
        return new File(property);
    }
    
    private static String getProperty(
            final String key, 
            final String desc,
            final String defaultValue,
            final boolean allowNull
            ){
        final String value = System.getProperties().getProperty(key);
        
        if(!allowNull && value == null){
            throw new RuntimeException("you must override system property \"" + key + "\" which holds \"" + desc + "\"");
        }
        
        final String propertyValue = value == null ? defaultValue : value;
        
        System.out.printf(
            "Using value %s for property \"%s\" (System property value was %s)\n",
            propertyValue, 
            key, 
            value
            );
        
        return propertyValue;
    }
    
    private static class ClientHelper{
        
        private static void verifyCompliance(
                final String xmlToValidate,
                final File xsd
                ) throws SAXException, IOException {
            final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            
            Schema schema = factory.newSchema(xsd);
            Validator validator = schema.newValidator();
            
            validator.validate(new StreamSource(new ByteArrayInputStream(xmlToValidate.getBytes())));
        }
        
        private static String normalName(
                final File f
        ) throws IOException {
            return f.getCanonicalPath().replace("\\", "/");
        }
        
        private static DocumentSet getDocumentSetFromSchema(final File schema) throws IOException{
            final DocumentSet ds = new DocumentSet();
    
            ds.setName(schema.getName());
            
            final File dir = schema.getParentFile();
    
            final String base = normalName(dir);
            
            for(File f:FileUtils.listFiles(dir, new String[]{"xml","xsd"}, true)){
                if(f.getName().endsWith(".dat")){
                    continue;
                }
    
                final String path = normalName(f);
    
                final String name = path.substring(path.indexOf(base)+base.length()+1);
    
                Document d = new Document();
                d.setDocumentContent(FileUtils.readFileToString(f,StandardCharsets.UTF_8));
                d.setDocumentName(name);
                
                if(f.getName().equals(schema.getName())){
                    d.setPrimarySchemaDoc(true);
                }
    
                ds.getDocuments().add(d);
            }
    
            return ds;
        }
    }
    
    private static final String identityXslt = 
            "<xsl:stylesheet version=\"3.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\r\n" + 
            "    <xsl:template match=\"@*|node()\">\r\n" + 
            "        <xsl:copy>\r\n" + 
            "            <xsl:apply-templates select=\"@*|node()\"/>\r\n" + 
            "        </xsl:copy>\r\n" + 
            "    </xsl:template>\r\n" + 
            "</xsl:stylesheet>"
            ;

}
