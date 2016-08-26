package com.bbn.cot;

import com.bbn.cot.CotParser.ValidationErrorCallback.ErrorLevel;
import com.bbn.marti.service.CoreConfig;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.StringReader;

public class CotParser {
    public static final String SCHEMA_FILE = "Event-PUBLIC.xsd";
    public static final String VALIDATION_KEY = "submission.validateXml";

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(CotParser.class.getCanonicalName());
    private static SAXParserFactory factory = SAXParserFactory.newInstance();
    // Figure out if we should be validating from CoreConfig
    private static Boolean isValidating = CoreConfig.getInstance().getAttributeBoolean(VALIDATION_KEY);

    /////////////////////////////////////////////
    // Individual parsers
    private static SchemaFactory schemaFactory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    static {
        if (isValidating != null && isValidating) {
            if (new File(SCHEMA_FILE).canRead()) {
                try {
                    factory.setSchema(
                            schemaFactory.newSchema(
                                    new Source[]{new StreamSource(SCHEMA_FILE)}));
                } catch (SAXException e) {
                    //e.printStackTrace();
                    isValidating = false;
                }
            } else {
                isValidating = false;
            }
        } else {
            isValidating = false;
        }
    }

    private SAXReader reader = null;

    private CotParser() {
        this(null);
    }
    private CotParser(ValidationErrorCallback errback) {
        // Setup the XML parser
        try {
            SAXParser parser = factory.newSAXParser();
            reader = new SAXReader(parser.getXMLReader());
            reader.setValidation(false);

            // If we are going to be validating...
            if (isValidating) {
                final ValidationErrorCallback errorCallback = errback;
                reader.setErrorHandler(new ErrorHandler() {
                    @Override
                    public void warning(SAXParseException exception) throws SAXException {
                        //exception.printStackTrace();
                        if (errorCallback != null)
                            errorCallback.onValidationError(ErrorLevel.WARNING, exception);
                        throw exception;
                    }

                    @Override
                    public void fatalError(SAXParseException exception) throws SAXException {
                        //exception.printStackTrace();
                        if (errorCallback != null)
                            errorCallback.onValidationError(ErrorLevel.FATAL, exception);
                        throw exception;
                    }

                    @Override
                    public void error(SAXParseException exception) throws SAXException {
                        //exception.printStackTrace();
                        if (errorCallback != null)
                            errorCallback.onValidationError(ErrorLevel.ERROR, exception);
                        throw exception;
                    }
                });
            }
        } catch (Exception e) {
            //e.printStackTrace();
            isValidating = false;
        }

//		logger.log(Level.DEBUG, "XML validation set to " + isValidating);

    }

    public static CotParser instance() {
        return new CotParser();
    }

    public static CotParser instance(ValidationErrorCallback errback) {
        return new CotParser(errback);
    }

    public static Document parse(String xml) throws DocumentException {
//		return parse(instance(), xml);
        return instance().reader.read(new InputSource(new StringReader(xml)));
    }

    public interface ValidationErrorCallback {
        void onValidationError(ErrorLevel level, Exception saxParseException);

        enum ErrorLevel {
            WARNING,
            ERROR,
            FATAL
        }
    }

}
