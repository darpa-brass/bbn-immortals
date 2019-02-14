package com.securboration.immortals.constraint;

import java.io.File;

public class InjectionSite {

    public InjectionSite(String _className, String _methodName, int _lineNumber) {
        className = _className;
        methodName = _methodName;
        lineNumber = _lineNumber;
    }

    private String className;
    private String methodName;
    private int lineNumber;
    private File projectBaseDir;

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public File getProjectBaseDir() {
        return projectBaseDir;
    }

    public void setProjectBaseDir(File projectBaseDir) {
        this.projectBaseDir = projectBaseDir;
    }
}
