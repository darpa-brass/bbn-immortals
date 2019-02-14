package com.securboration.immortals.utility;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * This is a placeholder API for translating XML documents passed along dataflow
 * edges known before adaptation. This API wraps calls to Vanderbilt's XSD
 * translation service endpoint.
 *
 * @author jstaples
 *
 */
public class CannedEssSchemaTranslator {

    /**
     * Returns an XSLT for translating client documents into a format that works
     * for the server, or null if no translation is needed.
     *
     * @param essMinBaseDir
     *            points to the base of an ess-min directory containing
     *            build.gradle
     * @param translationServiceEndpointUrl
     *            URL of an XSD translation endpoint. E.g.,
     *            http://localhost:9999/xsdsts
     * @return an XSLT for translating, or null if one isn't needed
     * @throws IOException
     *             if anything goes awry
     */
    public static String translateClientToServer(
            final File essMinBaseDir,
            final String translationServiceEndpointUrl
    ) throws IOException{
        final DocumentSet src = getDocumentSetFromDirectory(
                new File(essMinBaseDir,"schema/client")
        );

        final DocumentSet dst = getDocumentSetFromDirectory(
                new File(essMinBaseDir,"schema/server")
        );

        return translate(translationServiceEndpointUrl,src,dst);
    }


    /**
     * Returns an XSLT for translating server documents into a format that works
     * for the client, or null if no translation is needed.
     *
     * @param essMinBaseDir
     *            points to the base of an ess-min directory containing
     *            build.gradle
     * @param translationServiceEndpointUrl
     *            URL of an XSD translation endpoint. E.g.,
     *            http://localhost:9999/xsdsts
     * @return an XSLT for translating, or null if one isn't needed
     * @throws IOException
     *             if anything goes awry
     */
    public static String translateServerToClient(
            final String translationServiceEndpointUrl,
            final File essMinBaseDir
    ) throws IOException{
        final DocumentSet src = getDocumentSetFromDirectory(
                new File(essMinBaseDir,"schema/server")
        );

        final DocumentSet dst = getDocumentSetFromDirectory(
                new File(essMinBaseDir,"schema/client")
        );

        return translate(translationServiceEndpointUrl,src,dst);
    }

//    public static String translateDatasourceToClient(final File essMinBaseDir){
//
//    }
//
//    public static String translateClientToDatasource(final File essMinBaseDir){
//
//    }

    private static String translate(
            final String translationEndpoint,
            final DocumentSet fromSchema,
            final DocumentSet toSchema
    ) throws IOException{
        final XsdtsClient client = new XsdtsClient(translationEndpoint);

        TranslationProblemDefinition tpd = new TranslationProblemDefinition();
        tpd.setSrcSchema(fromSchema);
        tpd.setDstSchema(toSchema);

        return client.getXsdTranslation(tpd);
    }

    private static DocumentSet getDocumentSetFromDirectory(final File dir) throws IOException{
        final DocumentSet ds = new DocumentSet();

        ds.setName(dir.getName());

        final String base = normalName(dir);

        System.out.printf(
                "constructing \"%s\" from %s\n",
                ds.getName(),
                dir.getAbsolutePath()
        );
        for(File f:FileUtils.listFiles(dir, null, true)){
            if(f.getName().endsWith(".dat")){
                continue;
            }

            final String path = normalName(f);

            final String name = path.substring(path.indexOf(base)+base.length()+1);

            Document d = new Document();
            d.setDocumentContent(FileUtils.readFileToString(f));
            d.setDocumentName(name);

            ds.getDocuments().add(d);

            System.out.printf(
                    "\tadded \"%s\" from %s\n",
                    d.getDocumentName(),
                    f.getAbsolutePath()
            );
        }

        return ds;
    }

    private static String normalName(
            final File f
    ) throws IOException {
        return f.getCanonicalPath().replace("\\", "/");
    }

}
