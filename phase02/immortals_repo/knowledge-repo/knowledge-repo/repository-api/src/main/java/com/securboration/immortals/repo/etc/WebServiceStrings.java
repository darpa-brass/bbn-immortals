package com.securboration.immortals.repo.etc;

import java.util.Optional;

/**
 * Created by CharlesEndicott on 7/20/2017.
 */
public class WebServiceStrings {
    
    public WebServiceStrings(String serviceHost) {
        SERVICES_HOST = serviceHost + "graphService/";
    }
    
    public WebServiceStrings() {
        SERVICES_HOST = "http://localhost:8080/graphService/";
    }
    private String SERVICES_HOST;
    private final String GRAPH_SERVICE = "graph";
    private final String GRAPH_CREATE = "?type=?TYPE?";
    private final String GRAPH_VIEW = "/?GID?";
    private final String GRAPHS_BY_TYPE_WITHOUT_CON = "?type=?TYPE?";
    private final String GRAPHS_BY_TYPE_WITH_CON = "?contextId=?CID?&type=?TYPE?";


    private final String CONTEXT_SERVICE = "context";
    private final String CONTEXT_CREATE_WITHOUT_ID = "";
    private final String CONTEXT_CREATE_WITH_ID = "?name=?CID?";
    private final String CONTEXT_DELETE_ALL = "";
    private final String CONTEXT_DELETE = "/?CID?";
    private final String CONTEXT_ADD_GRAPH = "/?CID?/add/?GID?";
    private final String CONTEXT_REMOVE_GRAPH = "/?CID?/remove/?GID?";

    private final String PUSH_CONTEXT = "pushContext?conId=?CID?";

    public String createGraphUrl(String type) {
        return  SERVICES_HOST + GRAPH_SERVICE + GRAPH_CREATE.replace("?TYPE?", type);
    }
    
    public String getGraphsOfTypeUrl(String type, Optional<String> possContextId) {
        
        if (possContextId.isPresent()) {
            return  SERVICES_HOST + GRAPH_SERVICE + GRAPHS_BY_TYPE_WITH_CON.replace("?TYPE?", type)
                    .replace("?CID?", possContextId.get());
        } else {
            return  SERVICES_HOST + GRAPH_SERVICE + GRAPHS_BY_TYPE_WITHOUT_CON.replace("?TYPE?", type);
        }
    }

    public String createContextUrl(Optional<String> possContextId) {
        if (possContextId.isPresent()) {
            return  SERVICES_HOST + CONTEXT_SERVICE + CONTEXT_CREATE_WITH_ID.replace("?CID?", possContextId.get());
        } else {
            return  SERVICES_HOST + CONTEXT_SERVICE + CONTEXT_CREATE_WITHOUT_ID;
        }
    }

    public String addGraphToContextUrl(String contextId, String graphId) {
        return  SERVICES_HOST + CONTEXT_SERVICE + CONTEXT_ADD_GRAPH.replace("?CID?", contextId).replace("?GID?", graphId);
    }

    public String removeGraphFromContextUrl(String contextId, String graphId) {
        return  SERVICES_HOST + CONTEXT_SERVICE + CONTEXT_REMOVE_GRAPH.replace("?CID?", contextId).replace("?GID?", graphId);
    }

    public String deleteContextUrl(Optional<String> possContextId) {
        if (possContextId.isPresent()) {
            return  SERVICES_HOST + CONTEXT_SERVICE + CONTEXT_DELETE.replace("?CID?", possContextId.get());
        } else {
            return  SERVICES_HOST + CONTEXT_SERVICE + CONTEXT_DELETE_ALL;
        }
    }
    
    public   String pushContextUrl(String contextId) {
        return  SERVICES_HOST + PUSH_CONTEXT.replace("?CID?", contextId);
    }
}
