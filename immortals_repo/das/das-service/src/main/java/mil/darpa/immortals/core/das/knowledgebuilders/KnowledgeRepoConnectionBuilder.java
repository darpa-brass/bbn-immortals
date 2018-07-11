package mil.darpa.immortals.core.das.knowledgebuilders;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.das.adaptationmodules.crossappdependencies.Prefix;
import mil.darpa.immortals.core.das.adaptationmodules.crossappdependencies.Res;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by awellman@bbn.com on 7/2/18.
 */
public class KnowledgeRepoConnectionBuilder implements IKnowledgeBuilder {

    public KnowledgeRepoConnectionBuilder() {

    }

    private static String getDfuResourceUri(Path sourceFilepath) throws IOException {
        Model m = ModelFactory.createDefaultModel();
        InputStream is = new FileInputStream(sourceFilepath.toFile());
        RDFDataMgr.read(m, is, Lang.TTL);

        List<Resource> resources = m.listResourcesWithProperty(RDF.type, m.getResource(Res.DFU_INSTANCE.uri)).toList();

        if (resources.size() != 1) {
            throw new RuntimeException("There should only be one DfuInstance in the JavaX Dfu file!");
        }

        return resources.get(0).getURI();
    }


    @Override
    public Model buildKnowledge(Map<String, Object> parameters) throws Exception {
        // KRHACK: This should be handled by KR analysis somehow... 
        Path krgpAnalysisPath = ImmortalsConfig.getInstance().extensions.krgp.getTtlTargetDirectory();
        Path ttlIngestionDirectory = ImmortalsConfig.getInstance().globals.getTtlIngestionDirectory();

        // Set the DfuInstance identifiers
        String javaxResourceUri = getDfuResourceUri(
                krgpAnalysisPath.resolve("JavaxCrypto/structures/dfus/CipherImplJavaxCrypto.class-DFU.ttl"))
                .replaceAll(Prefix.IMMoRTALS_dfu_instance.uri, Prefix.IMMoRTALS_dfu_instance.name() + ":");


        String bcResourceUri = getDfuResourceUri(
                krgpAnalysisPath.resolve("BouncyCastleCipher/structures/dfus/CipherImplBouncyCrypto.class-DFU.ttl"))
                .replaceAll(Prefix.IMMoRTALS_dfu_instance.uri, Prefix.IMMoRTALS_dfu_instance.name() + ":");

        String noopResourceUri = getDfuResourceUri(
                krgpAnalysisPath.resolve("NoOpCipher/structures/dfus/CipherImplNoop.class-DFU.ttl"))
                .replaceAll(Prefix.IMMoRTALS_dfu_instance.uri, Prefix.IMMoRTALS_dfu_instance.name() + ":");

        InputStream is = this.getClass().getResourceAsStream("/cipherUsageParadigms.ttl");
        List<String> fileLines = IOUtils.readLines(is);

        List<String> outputLines = new ArrayList<>(fileLines.size());

        for (String str : fileLines) {
            str = str.replaceAll("<\\?\\?\\?NOOP_DFU\\?\\?\\?>", noopResourceUri);
            str = str.replaceAll("<\\?\\?\\?BC_DFU\\?\\?\\?>", bcResourceUri);
            str = str.replaceAll("<\\?\\?\\?JAVAX_DFU\\?\\?\\?>", javaxResourceUri);
            outputLines.add(str);
        }

        InputStream atakmartiDuplexInputStream = this.getClass().getResourceAsStream("/atakMartiDuplex.ttl");
        List<String> atakMartiDuplexLines = IOUtils.readLines(atakmartiDuplexInputStream);
        Path atakmartiDuplexTargetPath = ttlIngestionDirectory.resolve("atakMartiDuplex.ttl");
        if (Files.exists(atakmartiDuplexTargetPath)) {
            Files.delete(atakmartiDuplexTargetPath);
        }

        Files.write(atakmartiDuplexTargetPath, atakMartiDuplexLines);

        // Remove all the missleading DfuInstance identifiers
        Files.write(ImmortalsConfig.getInstance().globals.getTtlIngestionDirectory().resolve("cipherUsageParadigms.ttl"), outputLines);

        Path[] filesToDelete = {
                krgpAnalysisPath.resolve("Marti/structures/dfus/JavaxCrypto-2.0-LOCAL.jarDFU.ttl"),
                krgpAnalysisPath.resolve("Marti/structures/dfus/BouncyCastleCipher-2.0-LOCAL.jarDFU.ttl"),
                krgpAnalysisPath.resolve("Marti/structures/dfus/BogoCipher-2.0-LOCAL.jarDFU.ttl"),
                krgpAnalysisPath.resolve("Marti/structures/dfus/NoOpCipher-2.0-LOCAL.jarDFU.ttl")
        };

        for (Path p : filesToDelete) {
            if (Files.exists(p)) {
                Files.delete(p);
            }
        }

        return null;
    }

    public static void main(String[] args) {
        try {
            KnowledgeRepoConnectionBuilder x = new KnowledgeRepoConnectionBuilder();
            x.buildKnowledge(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
