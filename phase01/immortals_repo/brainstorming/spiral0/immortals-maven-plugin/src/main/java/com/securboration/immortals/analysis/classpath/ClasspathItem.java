package com.securboration.immortals.analysis.classpath;

import java.net.URL;

import com.securboration.immortals.analysis.JvmBytecodeAbstraction;

public class ClasspathItem extends JvmBytecodeAbstraction {
    
    public ClasspathItem(){super();}
    public ClasspathItem(String s){super(s);}
    
    private URL urlToResource;
    private byte[] resourceBytes;

    
    public byte[] getResourceBytes() {
        return resourceBytes;
    }

    public void setResourceBytes(byte[] resourceBytes) {
        this.resourceBytes = resourceBytes;
    }

    public URL getUrlToResource() {
        return urlToResource;
    }

    public void setUrlToResource(URL urlToResource) {
        this.urlToResource = urlToResource;
    }
    
}
