package com.securboration.immortals.utility.cipher;

import java.util.Optional;

public class CipherInfo {

    private String cipherClassName;
    private Optional<String> configurationParameters;


    public String getCipherClassName() {
        return cipherClassName;
    }

    public void setCipherClassName(String cipherClassName) {
        this.cipherClassName = cipherClassName;
    }

    public Optional<String> getConfigurationParameters() {
        return configurationParameters;
    }

    public void setConfigurationParameters(Optional<String> configurationParameters) {
        this.configurationParameters = configurationParameters;
    }
}
