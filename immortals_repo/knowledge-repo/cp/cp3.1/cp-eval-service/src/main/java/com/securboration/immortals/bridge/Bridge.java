package com.securboration.immortals.bridge;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.securboration.immortals.service.eos.api.types.EosType;
import com.securboration.immortals.service.eos.api.types.EvaluationConfiguration;
import com.securboration.immortals.swri.EvaluationPackageBuilder;
import com.securboration.immortals.swri.EvaluationPackageBuilder.MdlSchemaVersion;
import com.securboration.immortals.swri.EvaluationProperties;
import com.securboration.immortals.swri.EvaluationProperties.EvaluationPropertyKey;

import mil.darpa.immortals.schemaevolution.ChallengeProblemBridge;
import mil.darpa.immortals.schemaevolution.TerminalStatus;

public class Bridge {
    
    /**
     * This is a mock implementation of the MDL scheme evolution bridge that
     * will ultimately be implemented by awellman. Instead of reading from and
     * writing to OrientDB, it reads evaluation configuration values from JVM
     * properties and writes to disk.
     * 
     * @author jstaples
     *
     */
    public static class MockEvaluationBridge extends ChallengeProblemBridge{

        private final EvaluationProperties props;
        
        private final File outDir;
        
        public MockEvaluationBridge(
                EvaluationProperties props
                ) throws Exception {
            this.props = props;
            
            this.outDir = new File(props.get(EvaluationPropertyKey.evalOutputDir));
        }

        @Override
        public synchronized void storeLargeBinaryData(
                final String evaluationInstanceIdentifier, 
                final String artifactIdentifier,
                final byte[] binaryData
                ) throws Exception {
            write(evaluationInstanceIdentifier,artifactIdentifier,binaryData);
        }
        
        private void write(
                final String evaluationInstanceId, 
                final String artifactId, 
                final byte[] data
                ) throws IOException{
            final boolean useUuid = 
                    Boolean.parseBoolean(props.get(EvaluationPropertyKey.uniqueDirPerEval));
            
            final File dir;
            
            if(useUuid){
                dir = new File(outDir,evaluationInstanceId);
            } else {
                dir = outDir;
            }
            
            FileUtils.writeByteArrayToFile(
                new File(dir,artifactId), 
                data
                );
        }
        
        
        private EvaluationConfiguration getEvaluationPackage() throws IOException{
            if(props.get(EvaluationPropertyKey.evalType).equals("testSanity")){
                final File templateDir = new File(props.get(EvaluationPropertyKey.essTemplateDir));
                
                if(!templateDir.exists()){
                    throw new RuntimeException("template dir " + templateDir.getAbsolutePath() + " does not exist");
                }
                if(!templateDir.isDirectory()){
                    throw new RuntimeException("template dir " + templateDir.getAbsolutePath() + " is not a directory");
                }
                
                return EvaluationPackageBuilder.createEvaluationPackageSimple(
                    templateDir, 
                    schemaFromTag("v1"), 
                    schemaFromTag("v1"), 
                    schemaFromTag("v1"),
                    createCheatArchive()
                    );
            } else if(props.get(EvaluationPropertyKey.evalType).equals("testSimple")){
                final File templateDir = new File(props.get(EvaluationPropertyKey.essTemplateDir));
                
                if(!templateDir.exists()){
                    throw new RuntimeException("template dir " + templateDir.getAbsolutePath() + " does not exist");
                }
                if(!templateDir.isDirectory()){
                    throw new RuntimeException("template dir " + templateDir.getAbsolutePath() + " is not a directory");
                }
                
                return EvaluationPackageBuilder.createEvaluationPackageSimple(
                    templateDir, 
                    schemaFromTag(props.get(EvaluationPropertyKey.simpleClientSchemaVersion)), 
                    schemaFromTag(props.get(EvaluationPropertyKey.simpleServerSchemaVersion)), 
                    schemaFromTag(props.get(EvaluationPropertyKey.simpleDatasourceSchemaVersion)),
                    createCheatArchive()
                    );
                
            } else if(props.get(EvaluationPropertyKey.evalType).equals("testCanned")){
                final File inputJson = new File(props.get(EvaluationPropertyKey.cannedInputJson));
                if(!inputJson.exists()){
                    throw new RuntimeException(inputJson.getAbsolutePath() + " does not exist");
                }
                
                return EosType.fromJson(
                    FileUtils.readFileToString(inputJson), 
                    EvaluationConfiguration.class
                    );
            } else if(props.get(EvaluationPropertyKey.evalType).equals("testComplex")){
                return EvaluationPackageBuilder.createCustomEvaluationPackage(
                    new File(props.get(EvaluationPropertyKey.customDirContainingClientXsds)), 
                    new File(props.get(EvaluationPropertyKey.customDirContainingServerXsds)),
                    new File(props.get(EvaluationPropertyKey.customDirContainingDatasourceXsds)),
                    new File(props.get(EvaluationPropertyKey.customDirContainingDatasourceXml)),
                    createCheatArchive()
                    );
            } else {
                throw new RuntimeException("unsupported evaluation type: " + props.get(EvaluationPropertyKey.evalType).equals("complex"));
            }
        }
        
        private static MdlSchemaVersion schemaFromTag(final String tag){
            return MdlSchemaVersion.get(tag);
        }
        
        private byte[] createCheatArchive() throws IOException{
            final String cheatDirPath = props.get(EvaluationPropertyKey.cheatDir);
            if(cheatDirPath == null){
                return null;
            }
            
            final ByteArrayOutputStream zipped = new ByteArrayOutputStream();
            
            final File cheatDir = new File(cheatDirPath);
            
            final String baseNormalPath = 
                    new File(cheatDirPath).getAbsolutePath().replace("\\", "/");
            
            try(final ZipOutputStream zos = new ZipOutputStream(zipped)){
                for(File f:FileUtils.listFiles(cheatDir, null, true)){
                    final byte[] data = FileUtils.readFileToByteArray(f);
                    
                    final String path = f.getAbsolutePath().replace("\\", "/");
                    
                    final String common = StringUtils.getCommonPrefix(baseNormalPath,path);
                    
                    final String result = path.substring(common.length()+1);
                    
                    ZipEntry z = new ZipEntry(result);
                    
                    zos.putNextEntry(z);
                    
                    zos.write(data);
                }
            }
            
            return zipped.toByteArray();
        }
        


        @Override
        public synchronized String getConfigurationJson(
                final String evaluationInstanceIdentifier
                ) throws Exception {
            return getEvaluationPackage().toJson();
        }
        
        @Override
        public synchronized void postError(
                final String evaluationInstanceIdentifier, 
                final String errorDesc,
                final String errorData
                ) throws Exception {
            write(
                evaluationInstanceIdentifier,
                "error." + System.currentTimeMillis() + ".log",
                (errorDesc + "\n" + errorData).getBytes(StandardCharsets.UTF_8)
                );
        }
        
        @Override
        public synchronized void postResultsJson(
                final String evaluationInstanceIdentifier, 
                final TerminalStatus status,
                final String results
                ) throws Exception {
            write(
                evaluationInstanceIdentifier,
                "results.json",
                results.getBytes(StandardCharsets.UTF_8)
                );
        }

        @Override
        public synchronized void init() throws Exception {
            //does nothing
        }
        
        
        
    }
    
    

}
