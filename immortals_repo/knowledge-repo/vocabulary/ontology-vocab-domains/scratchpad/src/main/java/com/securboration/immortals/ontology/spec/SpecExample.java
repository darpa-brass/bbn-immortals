package com.securboration.immortals.ontology.spec;

import java.util.ArrayList;
import java.util.List;

import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;
import com.securboration.immortals.ontology.bytecode.ClasspathElement;
import com.securboration.immortals.ontology.bytecode.JarArtifact;
import com.securboration.immortals.ontology.dfu.instance.DfuInstance;
import com.securboration.immortals.ontology.functionality.DfuConfigurationVariable;
import com.securboration.immortals.ontology.functionality.alg.encryption.*;
import com.securboration.immortals.ontology.functionality.wrapper.AspectRuntimeImplementation;
import com.securboration.immortals.ontology.functionality.wrapper.AspectUtilizeDecryptedStream;
import com.securboration.immortals.ontology.functionality.wrapper.AspectUtilizeEncryptedStream;
import com.securboration.immortals.ontology.pattern.spec.*;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.resources.streams.*;

@Ignore
public class SpecExample {
    
    @ConceptInstance
    public static class CipherSpecSocketExampleOuput extends LibraryFunctionalAspectSpec {

        public CipherSpecSocketExampleOuput() {
            this.setDurableId("CipherSpecSocketExample");
            this.setFunctionality(Cipher.class);
            this.setLibraryCoordinateTag("com.secur:cipher:1.2.3");
            this.setAspect(AspectCipherEncrypt.class);
            this.setUsageParadigm(new ParadigmCipherExample());
            this.setComponent(getCipherSpecSocketOuput());
        }
    }
    
    private static SpecComponent[] getCipherSpecSocketOuput(){
        List<SpecComponent> components = new ArrayList<>();

        components.add(new SpecComponent(){{
            this.setDurableId("cipher-init");

            CodeSpec codeSpec = new CodeSpec();
            codeSpec.setClassName("java/net/Socket");
            codeSpec.setCode("{ ???CIPHER_CLASS??? cipherImpl = new ???CIPHER_CLASS???();\n" +
                    "                    cipherImpl.configure(\"$ALG$\", $KEY_LENGTH$, \"$CHAINING_MODE$\", \"$PADDING$\", \"a test password\", \"an init vector\");\n" +
                    "                    return cipherImpl; }");
            codeSpec.setMethodSignature("initCipherImpl()L???CIPHER_CLASS???");
            this.setCodeSpec(codeSpec);
            this.setAspectBeingPerformed(AspectCipherInitialize.class);
        }});

        components.add(new SpecComponent(){{
            this.setDurableId("get-stream-impl");

            CodeSpec codeSpec = new CodeSpec();
            codeSpec.setClassName("java/net/Socket");
            codeSpec.setMethodSignature("getOutputStreamImpl()");
            codeSpec.setCode("\t\t{OutputStream outputStream = this.socket.getOutputStream();\n\t\treturn outputStream;}");
            this.setCodeSpec(codeSpec);
            this.setAspectBeingPerformed(AspectRetrieveOutputStream.class);

        }});

        components.add(new SpecComponent(){{
            this.setDurableId("wrap-stream-with-cipher");
            CodeSpec codeSpec = new CodeSpec();
            codeSpec.setMethodSignature("wrapOutputStream(OutputStream," +
                    " CipherImpl)");
            codeSpec.setClassName("java/net/Socket");
            codeSpec.setCode("\t\t{outputStream = cipherImpl.aquire(outputStream);\n" +
                    "\t\treturn var1;}");
            this.setCodeSpec(codeSpec);
            this.setAspectBeingPerformed(WrapOutputStreamWithCipher.class);
        }});

        components.add(new SpecComponent(){{
            this.setDurableId("wrapper-use-encrypt-stream");
            CodeSpec codeSpec = new CodeSpec();
            codeSpec.setClassName("java/net/Socket");
            codeSpec.setMethodSignature("getOutputStream()");
            codeSpec.setCode("{return this.outputstream;}");
            this.setCodeSpec(codeSpec);
            this.setAspectBeingPerformed(AspectUtilizeEncryptedStream.class);
        }});
        
        

        return components.toArray(new SpecComponent[]{});
    }

    private static SpecComponent[] getCipherSpecSocket(){
        List<SpecComponent> components = new ArrayList<>();

        components.add(new SpecComponent(){{
            this.setDurableId("cipher-init");
            
            CodeSpec codeSpec = new CodeSpec();
            codeSpec.setClassName("java/net/Socket");
            codeSpec.setMethodSignature("initCipherImpl()");
            this.setCodeSpec(codeSpec);
            this.setAspectBeingPerformed(AspectCipherInitialize.class);
        }});

        components.add(new SpecComponent(){{
            this.setDurableId("get-stream-impl");
            CodeSpec codeSpec = new CodeSpec();
            codeSpec.setClassName("java/net/Socket");
            codeSpec.setMethodSignature("getInputStreamImpl()");
            codeSpec.setCode("\t\t{InputStream inputStream = this.socket.getInputStream();\n\t\treturn inputStream;}");
            this.setCodeSpec(codeSpec);
            this.setAspectBeingPerformed(AspectRetrieveInputStream.class);
            
        }});

        components.add(new SpecComponent(){{
            this.setDurableId("wrap-stream-with-cipher");
            
            CodeSpec codeSpec = new CodeSpec();
            codeSpec.setClassName("java/net/Socket");
            codeSpec.setMethodSignature("wrapInputStream(InputStream," +
                    " CipherImpl)");
            codeSpec.setCode("\t\t{inputStream = cipherImpl.aquire(inputStream);\n" +
                    "\t\treturn var1;}");
            this.setCodeSpec(codeSpec);
            this.setAspectBeingPerformed(WrapInputStreamWithCipher.class);
        }});

        components.add(new SpecComponent(){{
            this.setDurableId("wrapper-use-decrypt-stream");
            CodeSpec codeSpec = new CodeSpec();
            codeSpec.setClassName("java/net/Socket");
            codeSpec.setMethodSignature("getInputStream()");
            codeSpec.setCode("{return this.inputstream;}");
            this.setCodeSpec(codeSpec);
            this.setAspectBeingPerformed(AspectUtilizeDecryptedStream.class);
        }});
        
        return components.toArray(new SpecComponent[]{});
    }


    @ConceptInstance
    public static class CipherSpecSocketExample extends LibraryFunctionalAspectSpec {

        public CipherSpecSocketExample() {
            this.setDurableId("CipherSpecSocketExample");
            this.setFunctionality(Cipher.class);
            this.setLibraryCoordinateTag("com.secur:cipher:1.2.3");
            this.setAspect(AspectCipherDecrypt.class);
            this.setUsageParadigm(new ParadigmCipherExample());
            this.setComponent(getCipherSpecSocket());
        }
    }

    @ConceptInstance
    public static class ParadigmCipherSocketExample extends AbstractUsageParadigm{

        public ParadigmCipherSocketExample(){
            this.setDurableId("ParadigmCipherSocketExample-initStreamWrapper-getStreamImpl-initWrap");
            this.setComponent(new ParadigmComponent[]{
                    new InitStreamWrapper(),
                    new GetStreamImpl(),
                    new InitWrap(),
            });
        }

    }
    
    @ConceptInstance
    public static class BouncyCastleCipher extends DfuInstance {
        public BouncyCastleCipher() {
            this.setClassPointer("TODO");
            this.setFunctionalityAbstraction(Cipher.class);
        }
    }
    
    @ConceptInstance
    public static class JavaxCryptoCipher extends DfuInstance {
        public JavaxCryptoCipher() {
            this.setClassPointer("TODO");
            this.setFunctionalityAbstraction(Cipher.class);
        }
    }
    
    @ConceptInstance
    public static class NoOpCipherParadigm extends AbstractUsageParadigm {
        private DfuInstance dfuInstance;
        private String magicInitString;
        private DfuConfigurationVariable[] configurationVariables;
        
        public NoOpCipherParadigm() {
            this.setMagicInitString("{ CipherImplNoop cipherImplNoop = new CipherImplNoop();\n" +
                    "cipherImplNoop.configure(\"AES\", 16, \"CBC\", \"PKCS5Padding\", \"a test password\", \"an init vector\");\n" +
                    "return cipherImplNoop; }");
            DfuConfigurationVariable[] dfuConfigurationVariables = new DfuConfigurationVariable[5];
            DfuConfigurationVariable dfuConfigurationVariable0 = new DfuConfigurationVariable();
            dfuConfigurationVariable0.setSemanticType(CipherAlgorithm.class);
            dfuConfigurationVariable0.setMagicStringVar("$ALG$");
            DfuConfigurationVariable dfuConfigurationVariable1 = new DfuConfigurationVariable();
            dfuConfigurationVariable1.setSemanticType(CipherKeyLength.class);
            dfuConfigurationVariable1.setMagicStringVar("$KEY_LENGTH$");
            DfuConfigurationVariable dfuConfigurationVariable2 = new DfuConfigurationVariable();
            dfuConfigurationVariable2.setSemanticType(CipherBlockSize.class);
            dfuConfigurationVariable2.setMagicStringVar("$BLOCK_SIZE$");
            DfuConfigurationVariable dfuConfigurationVariable3 = new DfuConfigurationVariable();
            dfuConfigurationVariable3.setSemanticType(CipherChainingMode.class);
            dfuConfigurationVariable3.setMagicStringVar("$CHAINING_MODE$");
            DfuConfigurationVariable dfuConfigurationVariable4 = new DfuConfigurationVariable();
            dfuConfigurationVariable4.setSemanticType(PaddingScheme.class);
            dfuConfigurationVariable4.setMagicStringVar("$PADDING_SCHEME$");
            dfuConfigurationVariables[0] = dfuConfigurationVariable0;
            dfuConfigurationVariables[1] = dfuConfigurationVariable1;
            dfuConfigurationVariables[2] = dfuConfigurationVariable2;
            dfuConfigurationVariables[3] = dfuConfigurationVariable3;
            dfuConfigurationVariables[4] = dfuConfigurationVariable4;
            this.setConfigurationVariables(dfuConfigurationVariables);
        }

        public String getMagicInitString() {
            return magicInitString;
        }

        public void setMagicInitString(String magicInitString) {
            this.magicInitString = magicInitString;
        }

        public DfuConfigurationVariable[] getConfigurationVariables() {
            return configurationVariables;
        }

        public void setConfigurationVariables(DfuConfigurationVariable[] configurationVariables) {
            this.configurationVariables = configurationVariables;
        }
    }
    

    @ConceptInstance
    public static class BouncyCastleCipherParadigm extends AbstractUsageParadigm {

        private DfuInstance dfuInstance;
        private String magicInitString;
        private DfuConfigurationVariable[] configurationVariables;

        public BouncyCastleCipherParadigm() {
            this.setDfuInstance(new BouncyCastleCipher());
            this.setMagicInitString("{ CipherImplBouncyCrypto cipherImpl = new CipherImplBouncyCrypto();\n" +
                    "cipherImpl.configure(\"AES\", 16, \"CBC\", \"PKCS5Padding\", \"a test password\", \"an init vector\");\n" +
                    "return cipherImpl; }");

            DfuConfigurationVariable[] dfuConfigurationVariables = new DfuConfigurationVariable[5];
            DfuConfigurationVariable dfuConfigurationVariable0 = new DfuConfigurationVariable();
            dfuConfigurationVariable0.setSemanticType(CipherAlgorithm.class);
            dfuConfigurationVariable0.setMagicStringVar("$ALG$");
            DfuConfigurationVariable dfuConfigurationVariable1 = new DfuConfigurationVariable();
            dfuConfigurationVariable1.setSemanticType(CipherKeyLength.class);
            dfuConfigurationVariable1.setMagicStringVar("$KEY_LENGTH$");
            DfuConfigurationVariable dfuConfigurationVariable2 = new DfuConfigurationVariable();
            dfuConfigurationVariable2.setSemanticType(CipherBlockSize.class);
            dfuConfigurationVariable2.setMagicStringVar("$BLOCK_SIZE$");
            DfuConfigurationVariable dfuConfigurationVariable3 = new DfuConfigurationVariable();
            dfuConfigurationVariable3.setSemanticType(CipherChainingMode.class);
            dfuConfigurationVariable3.setMagicStringVar("$CHAINING_MODE$");
            DfuConfigurationVariable dfuConfigurationVariable4 = new DfuConfigurationVariable();
            dfuConfigurationVariable4.setSemanticType(PaddingScheme.class);
            dfuConfigurationVariable4.setMagicStringVar("$PADDING_SCHEME$");
            dfuConfigurationVariables[0] = dfuConfigurationVariable0;
            dfuConfigurationVariables[1] = dfuConfigurationVariable1;
            dfuConfigurationVariables[2] = dfuConfigurationVariable2;
            dfuConfigurationVariables[3] = dfuConfigurationVariable3;
            dfuConfigurationVariables[4] = dfuConfigurationVariable4;
            this.setConfigurationVariables(dfuConfigurationVariables);
        }

        public DfuInstance getDfuInstance() {
            return dfuInstance;
        }

        public void setDfuInstance(DfuInstance dfuInstance) {
            this.dfuInstance = dfuInstance;
        }

        public String getMagicInitString() {
            return magicInitString;
        }

        public void setMagicInitString(String magicInitString) {
            this.magicInitString = magicInitString;
        }

        public DfuConfigurationVariable[] getConfigurationVariables() {
            return configurationVariables;
        }

        public void setConfigurationVariables(DfuConfigurationVariable[] configurationVariables) {
            this.configurationVariables = configurationVariables;
        }
    }
    
    @ConceptInstance
    public static class JavaxCryptoCipherParadigm extends AbstractUsageParadigm {
        
        private DfuInstance dfuInstance;
        private String magicInitString;
        private DfuConfigurationVariable[] configurationVariables;
        
        public JavaxCryptoCipherParadigm() {
            this.setDfuInstance(new JavaxCryptoCipher());
            this.setMagicInitString("{ CipherImplJavaxCrypto cipherImpl = new CipherImplJavaxCrypto();\n" +
                    "cipherImpl.configure(\"AES\", 16, \"CBC\", \"PKCS5Padding\", \"a test password\", \"an init vector\");\n" +
                    "return cipherImpl; }");
            
            DfuConfigurationVariable[] dfuConfigurationVariables = new DfuConfigurationVariable[5];
            DfuConfigurationVariable dfuConfigurationVariable0 = new DfuConfigurationVariable();
            dfuConfigurationVariable0.setSemanticType(CipherAlgorithm.class);
            dfuConfigurationVariable0.setMagicStringVar("$ALG$");
            DfuConfigurationVariable dfuConfigurationVariable1 = new DfuConfigurationVariable();
            dfuConfigurationVariable1.setSemanticType(CipherKeyLength.class);
            dfuConfigurationVariable1.setMagicStringVar("$KEY_LENGTH$");
            DfuConfigurationVariable dfuConfigurationVariable2 = new DfuConfigurationVariable();
            dfuConfigurationVariable2.setSemanticType(CipherBlockSize.class);
            dfuConfigurationVariable2.setMagicStringVar("$BLOCK_SIZE$");
            DfuConfigurationVariable dfuConfigurationVariable3 = new DfuConfigurationVariable();
            dfuConfigurationVariable3.setSemanticType(CipherChainingMode.class);
            dfuConfigurationVariable3.setMagicStringVar("$CHAINING_MODE$");
            DfuConfigurationVariable dfuConfigurationVariable4 = new DfuConfigurationVariable();
            dfuConfigurationVariable4.setSemanticType(PaddingScheme.class);
            dfuConfigurationVariable4.setMagicStringVar("$PADDING_SCHEME$");
            dfuConfigurationVariables[0] = dfuConfigurationVariable0;
            dfuConfigurationVariables[1] = dfuConfigurationVariable1;
            dfuConfigurationVariables[2] = dfuConfigurationVariable2;
            dfuConfigurationVariables[3] = dfuConfigurationVariable3;
            dfuConfigurationVariables[4] = dfuConfigurationVariable4;
            this.setConfigurationVariables(dfuConfigurationVariables);
        }

        public DfuInstance getDfuInstance() {
            return dfuInstance;
        }

        public void setDfuInstance(DfuInstance dfuInstance) {
            this.dfuInstance = dfuInstance;
        }

        public String getMagicInitString() {
            return magicInitString;
        }

        public void setMagicInitString(String magicInitString) {
            this.magicInitString = magicInitString;
        }

        public DfuConfigurationVariable[] getConfigurationVariables() {
            return configurationVariables;
        }

        public void setConfigurationVariables(DfuConfigurationVariable[] configurationVariables) {
            this.configurationVariables = configurationVariables;
        }
    }
    
    @ConceptInstance
    public static class CipherSpecExample extends LibraryFunctionalAspectSpec {
        
        public CipherSpecExample() {
            this.setDurableId("CipherSpecExample");
            this.setFunctionality(Cipher.class);
            this.setLibraryCoordinateTag("com.secur:cipher:1.2.3");
            this.setAspect(AspectCipherEncrypt.class);
            this.setUsageParadigm(new ParadigmCipherExample());
            this.setComponent(getCipherSpec());
        }
    }
    
    @ConceptInstance
    public static class ParadigmCipherExample extends AbstractUsageParadigm{

        public ParadigmCipherExample(){
            this.setDurableId("ParadigmCipherExample-initStreamWrapper-getStreamImpl-initWrap");
            this.setComponent(new ParadigmComponent[]{
                    new InitStreamWrapper(),
                    new GetStreamImpl(),
                    new InitWrap(),
            });
        }

    }
    @ConceptInstance
    public static class InitStreamWrapper extends ParadigmComponent{
        public InitStreamWrapper(){
            this.setDurableId("initStreamWrapper");
            this.setMultiplicityOperator("1");
            this.setOrdering(1);
        }
    }
    
    @ConceptInstance
    public static class GetStreamImpl extends ParadigmComponent{
        public GetStreamImpl(){
            this.setDurableId("GetStreamImpl");
            this.setMultiplicityOperator("1");
            this.setOrdering(0);
        }
    }

    @ConceptInstance
    public static class InitWrap extends ParadigmComponent{
        public InitWrap(){
            this.setDurableId("initWrap");
            this.setMultiplicityOperator("1");
            this.setOrdering(0);
        }
    }

    @ConceptInstance
    public static class CipherSpecExampleInput extends LibraryFunctionalAspectSpec {

        public CipherSpecExampleInput() {
            this.setDurableId("CipherSpecExample");
            this.setFunctionality(Cipher.class);
            this.setLibraryCoordinateTag("com.secur:cipher:1.2.3");
            this.setAspect(AspectCipherDecrypt.class);
            this.setUsageParadigm(new ParadigmCipherExample());
            this.setComponent(getCipherSpecInput());
        }
    }

    private static SpecComponent[] getCipherSpecInput() {
        List<SpecComponent> components = new ArrayList<>();

        components.add(new SpecComponent(){{
            this.setDurableId("cipher-init");

            CodeSpec codeSpec = new CodeSpec();
            codeSpec.setClassName("java/nio/channels/SocketChannel");
            codeSpec.setCode("{ ???CIPHER_CLASS??? cipherImpl = new ???CIPHER_CLASS???();\n" +
                    "                    cipherImpl.configure(\"$ALG$\", $KEY_LENGTH$, \"$CHAINING_MODE$\", \"$PADDING$\", \"a test password\", \"an init vector\");\n" +
                    "                    return cipherImpl; }");
            codeSpec.setMethodSignature("initCipherImpl()L???CIPHER_CLASS???");
            this.setCodeSpec(codeSpec);
            this.setAspectBeingPerformed(AspectCipherInitialize.class);
        }});
        
        components.add(new SpecComponent() {{
            this.setDurableId("read-message");

            CodeSpec codeSpec = new CodeSpec();
            codeSpec.setClassName("java/nio/channels/SocketChannel");
            codeSpec.setMethodSignature("read(ByteBuffer)");
            codeSpec.setCode(" //create a temp buffer capable of reading AT MOST as many bytes\n" +
                    "        // as we have room to read in byteBuffer\n" +
                    "        final ByteBuffer tmpBuffer = ByteBuffer.wrap(new byte[byteBuffer.remaining()]);\n" +
                    "\n" +
                    "        //read encrypted bytes from the delegate stream\n" +
                    "        final int bytesReadFromStream = this.delegate.read(tmpBuffer);\n" +
                    "\n" +
                    "        if(bytesReadFromStream <= 0) {\n" +
                    "            //TODO: handle the EOS case, -1, e.g., call the decryptFinal method\n" +
                    "            return bytesReadFromStream;\n" +
                    "        }\n" +
                    "\n" +
                    "        //copy the encrypted contents of the buffer into an array\n" +
                    "        final byte[] encryptedBytes = new byte[bytesReadFromStream];\n" +
                    "        {\n" +
                    "            System.arraycopy(tmpBuffer.array(), 0, encryptedBytes, 0, bytesReadFromStream);\n" +
                    "        }\n" +
                    "\n" +
                    "        //decrypt the array of encrypted bytes\n" +
                    "        final byte[] decrypted;\n" +
                    "        {\n" +
                    "            try {\n" +
                    "                decrypted = cipher.decryptChunk(encryptedBytes);\n" +
                    "            } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException\n" +
                    "                    | NoSuchPaddingException | InvalidAlgorithmParameterException e) {\n" +
                    "                throw new RuntimeException(e);\n" +
                    "            }\n" +
                    "        }\n" +
                    "        //sanity check\n" +
                    "        {\n" +
                    "            if(decrypted.length > byteBuffer.remaining()) {\n" +
                    "                throw new RuntimeException(\n" +
                    "                        \"sanity check failed.  \"\n" +
                    "                                + \"decrypted info too large to fit into provided buffer \"\n" +
                    "                                + \"(needed \" + decrypted.length +\n" +
                    "                                \" and have \" + byteBuffer.remaining() + \")\");\n" +
                    "            }\n" +
                    "        }\n" +
                    "\n" +
                    "        //write the decrypted bytes to the parent buffer\n" +
                    "        byteBuffer.put(decrypted, 0, decrypted.length);\n" +
                    "\n" +
                    "        return decrypted.length;");
            this.setCodeSpec(codeSpec);
            this.setAspectBeingPerformed(AspectReadMessage.class);
            
        }});
        
        components.add(new SpecComponent() {{
            this.setDurableId("configure-blocking");
            CodeSpec codeSpec = new CodeSpec();
            codeSpec.setClassName("java/nio/channels/SocketChannel");
            codeSpec.setMethodSignature("configureBlockingWrapped(boolean)Ljava/nio/channels/SelectableChannel");
            codeSpec.setCode("\t\t{(this.delegate).configureBlocking(value);}");
            this.setCodeSpec(codeSpec);
            this.setAspectBeingPerformed(AspectRuntimeImplementation.class);
        }});
        
        components.add(new SpecComponent(){{
            this.setDurableId("register");
            CodeSpec codeSpec = new CodeSpec();
            codeSpec.setClassName("java/nio/channels/SocketChannel");
            codeSpec.setMethodSignature("registerWrapped(Selector, int)Ljava/nio/channels/SelectableChannel");
            codeSpec.setCode("\t\t{return (this.delegate).register(sel,ops);}");
            this.setCodeSpec(codeSpec);
            this.setAspectBeingPerformed(AspectRuntimeImplementation.class);
        }});

        components.add(new SpecComponent(){{
            this.setDurableId("implClose");
            CodeSpec codeSpec = new CodeSpec();
            codeSpec.setClassName("java/nio/channels/SocketChannel");
            codeSpec.setMethodSignature("implCloseSelectableChannel()V");
            codeSpec.setCode("\t\t{try {\n" +
                    "            delegate.write(ByteBuffer.wrap(cipher.finishCrypt$()));\n" +
                    "        } catch (IllegalBlockSizeException | BadPaddingException e) {\n" +
                    "            throw new RuntimeException(e);\n" +
                    "        }\n" +
                    "\n" +
                    "        delegate.close();}");
            this.setCodeSpec(codeSpec);
            this.setAspectBeingPerformed(AspectRuntimeImplementation.class);
        }});

        return components.toArray(new SpecComponent[]{});
    }

    private static SpecComponent[] getCipherSpec(){
        List<SpecComponent> components = new ArrayList<>();

        components.add(new SpecComponent(){{
            this.setDurableId("cipher-init");
            
            CodeSpec codeSpec = new CodeSpec();
            codeSpec.setClassName("java/nio/channels/SocketChannel");
            codeSpec.setCode("{ ???CIPHER_CLASS??? cipherImpl = new ???CIPHER_CLASS???();\n" +
                    "                    cipherImpl.configure(\"$ALG$\", $KEY_LENGTH$, \"$CHAINING_MODE$\", \"$PADDING$\", \"a test password\", \"an init vector\");\n" +
                    "                    return cipherImpl; }");
            this.setCodeSpec(codeSpec);
            
            this.setAspectBeingPerformed(AspectCipherInitialize.class);
        }});

        components.add(new SpecComponent() {{

            this.setDurableId("write-message");
            CodeSpec codeSpec = new CodeSpec();
            codeSpec.setClassName("java/nio/channels/SocketChannel");
            codeSpec.setMethodSignature("write(ByteBuffer)");
            codeSpec.setCode(" {final ByteBuffer writeThis;\n" +
                    "\n" +
                    "        try {\n" +
                    "            writeThis = ByteBuffer.wrap(cipher.encryptChunk(byteBuffer.array()));\n" +
                    "        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException\n" +
                    "                | NoSuchPaddingException | InvalidAlgorithmParameterException e) {\n" +
                    "            throw new RuntimeException(e);\n" +
                    "        }\n" +
                    "\n" +
                    "        double diff;\n" +
                    "        if (byteBuffer.capacity() != writeThis.capacity()) {\n" +
                    "            diff = (double)byteBuffer.capacity() / (double)writeThis.capacity();\n" +
                    "        } else {\n" +
                    "            diff = 1.0;\n" +
                    "        }\n" +
                    "\n" +
                    "        int bytesWritten = (this.delegate.write(writeThis));\n" +
                    "\n" +
                    "        double compensationForSizeDiff = bytesWritten * diff;\n" +
                    "\n" +
                    "        byteBuffer.position(byteBuffer.position() + (int) compensationForSizeDiff);\n" +
                    "\n" +
                    "        return (int) compensationForSizeDiff;}");
            this.setCodeSpec(codeSpec);
            this.setAspectBeingPerformed(AspectWriteMessage.class);
        }});
        
        return components.toArray(new SpecComponent[]{});
    }
    
    @ConceptInstance
    public static class SpecExample1 extends LibraryFunctionalAspectSpec{
        public SpecExample1(){
            this.setDurableId("SpecExample-1");
            this.setFunctionality(Cipher.class);
            this.setAspect(AspectCipherEncrypt.class);
            this.setLibraryCoordinateTag(
                "org.third.party:lib-cipher:1.2.3"
                );
            this.setComponent(getExample1Spec());
            this.setUsageParadigm(new ParadigmExample());
        }
    }
    
    @ConceptInstance
    public static class ParadigmExample extends AbstractUsageParadigm{
        
        public ParadigmExample(){
            this.setDurableId("ParadigmExample-declare-init-doWork-cleanup");
            this.setComponent(new ParadigmComponent[]{
                    new Declare(),
                    new Init(),
                    new DoWork(),
                    new Cleanup()
            });
        }
        
    }
    
    @ConceptInstance
    public static class Declare extends ParadigmComponent{
        public Declare(){
            this.setDurableId("declare");
            this.setMultiplicityOperator("1");
            this.setOrdering(0);
        }
    }
    
    @ConceptInstance
    public static class Init extends ParadigmComponent{
        public Init(){
            this.setDurableId("init");
            this.setMultiplicityOperator("1");
            this.setOrdering(1);
        }
    }
    
    @ConceptInstance
    public static class DoWork extends ParadigmComponent{
        public DoWork(){
            this.setDurableId("doWork");
            this.setMultiplicityOperator("[0,inf]");
            this.setOrdering(2);
        }
    }
    
    @ConceptInstance
    public static class Cleanup extends ParadigmComponent{
        public Cleanup(){
            this.setDurableId("cleanup");
            this.setMultiplicityOperator("1");
            this.setOrdering(3);
        }
    }
    
    
    
    private static SpecComponent[] getExample1Spec(){
        List<SpecComponent> components = new ArrayList<>();
        
        components.add(new SpecComponent(){{
            this.setDurableId("example1-declare");
            this.setSpec("{com.securboration.example.Cipher cipher$2215879;}");
            this.setAbstractComponentLinkage(new Declare());
        }});
        
        components.add(new SpecComponent(){{
            this.setDurableId("example1-init");
            this.setSpec("cipher$2215879 = new com.securboration.example.Cipher();cipher$2215879.setMode(\"ENCRYPT\");");
            this.setAbstractComponentLinkage(new Init());
        }});
        
        components.add(new SpecComponent(){{
            this.setDurableId("example1-doWork");
            this.setSpec("output$127997 = cipher$2215879.encrypt(input$0876887);");
            this.setAbstractComponentLinkage(new DoWork());
        }});
        
        components.add(new SpecComponent(){{
            this.setDurableId("example1-cleanup");
            this.setSpec("cipher$2215879.cleanup();");
            this.setAbstractComponentLinkage(new Cleanup());
        }});
        
        return components.toArray(new SpecComponent[]{});
    }
    
    private static SpecComponent[] getExample2Spec(){
        List<SpecComponent> components = new ArrayList<>();
        
//        {
//            SpecComponent c = new SpecComponent();
//            c.setDurableId("example1-init");
//            c.setAbstractComponentLinkage(abstractComponentLinkage);
//        }
        
//      this.setSpec(
//      "instance = constructor(key)," +
//      "instance.encryptMode()," +
//      "byte[] chunk = put(byte[] dataChunk)*," +
//      "byte[] chunk = finish()"
//      );
        
        return components.toArray(new SpecComponent[]{});
    }
    
    @ConceptInstance
    public static class SpecExample2 extends LibraryFunctionalAspectSpec{
        public SpecExample2(){
            this.setDurableId("SpecExample-2 (incomplete)");
            this.setFunctionality(Cipher.class);
            this.setAspect(AspectCipherEncrypt.class);
            this.setLibraryCoordinateTag(
                "com.securboration:immortals-examples-cipher:0.0.1"
                );
            this.setComponent(getExample2Spec());

        }
    }
    
    @ConceptInstance
    public static class CipherLibrary1 extends JarArtifact{
        
        public CipherLibrary1(){
            this.setHash("hashValueGoesHere");
            this.setName(this.getClass().getSimpleName());
            this.setJarContents(new ClasspathElement[]{});
            this.setCoordinate(acquire(
                "org.third.party","lib-cipher","1.2.3"
                ));
            this.setBinaryForm(new byte[]{});
        }
    }
    
    @ConceptInstance
    public static class CipherLibrary2 extends JarArtifact{
        
        public CipherLibrary2(){
            this.setHash("hashValueGoesHere");
            this.setName(this.getClass().getSimpleName());
            this.setJarContents(new ClasspathElement[]{});
            this.setCoordinate(acquire(
                "com.securboration","immortals-examples-cipher","0.0.1"
                ));
            this.setBinaryForm(new byte[]{});
        }
    }
    
    private static BytecodeArtifactCoordinate acquire(
            String groupId,
            String artifactId,
            String version
            ){
        BytecodeArtifactCoordinate c = new BytecodeArtifactCoordinate();
        c.setGroupId(groupId);
        c.setArtifactId(artifactId);
        c.setVersion(version);
        c.setCoordinateTag(groupId + ":" + artifactId + ":" + version);
        
        return c;
    }

}
