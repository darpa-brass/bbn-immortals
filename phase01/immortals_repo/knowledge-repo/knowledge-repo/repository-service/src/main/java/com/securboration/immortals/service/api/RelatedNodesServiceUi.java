package com.securboration.immortals.service.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.securboration.immortals.repo.api.RepositoryUnsafe;
import com.securboration.immortals.service.config.ImmortalsServiceProperties;

/**
 * A service for returning the relationships to nodes in a graph
 * 
 * @author jstaples
 *
 */
@Controller
public class RelatedNodesServiceUi {
    
    private static final Logger logger = 
            LoggerFactory.getLogger(RelatedNodesServiceUi.class);
    
    @Autowired(required = true)
    private ImmortalsServiceProperties properties;
    
    @Autowired(required = true)
    private RepositoryUnsafe repository;
    
    
    @RequestMapping("/graphViewerUi")
    public String getHandler(@RequestParam(value="query", required=false, defaultValue="test") String query, 
            Model model
            ){
        String graph = "ERROR";
        model.addAttribute("title","OntoTab");
        query = query.replace("\n"," ").replace("\r", " ");
        //graph = graphUtil.parseOutGraphName(query);
        Pattern p = Pattern.compile("GRAPH <[A-z:/0-9-.]*");
        Matcher m = p.matcher(query);
        boolean found = m.find();
        if (found){
        	graph = m.group();
        	graph = graph.replace("GRAPH <", "");
        }
        model.addAttribute("graph",graph);
        model.addAttribute("query",query);
        return "StaticPageMain";
    }
}
