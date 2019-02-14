package com.securboration.immortals.soot;

import com.securboration.immortals.ontology.analysis.MethodInvocationDataflowNode;

import java.util.List;

public class UserAppInfo {

    private String userAppName;

    private List<String> userAppLines;

    private int initializerLineNumber;

    private String initializerMethodName;

    private String initNodeClassOwner;

    private List<MethodInvocationDataflowNode> problematicMethodNodes;

    public String getUserAppName() {
        return userAppName;
    }

    public void setUserAppName(String userAppName) {
        this.userAppName = userAppName;
    }

    public List<String> getUserAppLines() {
        return userAppLines;
    }

    public void setUserAppLines(List<String> userAppLines) {
        this.userAppLines = userAppLines;
    }

    public int getInitializerLineNumber() {
        return initializerLineNumber;
    }

    public void setInitializerLineNumber(int initializerLineNumber) {
        this.initializerLineNumber = initializerLineNumber;
    }

    public String getInitializerMethodName() {
        return initializerMethodName;
    }

    public void setInitializerMethodName(String initializerMethodName) {
        this.initializerMethodName = initializerMethodName;
    }

    public List<MethodInvocationDataflowNode> getProblematicMethodNodes() {
        return problematicMethodNodes;
    }

    public void setProblematicMethodNodes(List<MethodInvocationDataflowNode> problematicMethodNodes) {
        this.problematicMethodNodes = problematicMethodNodes;
    }

    public String getInitNodeClassOwner() {
        return initNodeClassOwner;
    }

    public void setInitNodeClassOwner(String initNodeClassOwner) {
        this.initNodeClassOwner = initNodeClassOwner;
    }
}
