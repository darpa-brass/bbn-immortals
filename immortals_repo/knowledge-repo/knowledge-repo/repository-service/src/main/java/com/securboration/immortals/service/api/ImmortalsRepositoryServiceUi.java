package com.securboration.immortals.service.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.securboration.immortals.repo.api.RepositoryUnsafe;

/**
 * Very simplistic UI for controlling the repository service
 * 
 * @author jstaples
 *
 */
@RestController
@RequestMapping("/immortalsRepositoryServiceUi")
public class ImmortalsRepositoryServiceUi {
    
    private static final Logger logger = 
            LoggerFactory.getLogger(ImmortalsRepositoryServiceUi.class);
    
    @Autowired(required = true)
    private RepositoryUnsafe repository;
    
    @RequestMapping(
            method = RequestMethod.GET,
            value="/",
            produces=MediaType.TEXT_HTML_VALUE
            )
    public ModelAndView  getGraphUris(){
        return getModelAndView();
    }
    
    private ModelAndView getModelAndView(){
        ModelAndView m = new ModelAndView("graphManager");
        m.addObject(
                "graphs", 
                repository.getGraphs());
 
        return m;
    }
    
    @RequestMapping(
            method = RequestMethod.POST,
            value="/",
            produces=MediaType.TEXT_HTML_VALUE
            )
    public ModelAndView  getGraphUrisPost(){
        return getModelAndView();
    }

}
