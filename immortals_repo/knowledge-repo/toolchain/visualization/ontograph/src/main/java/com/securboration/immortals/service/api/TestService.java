package com.securboration.immortals.service.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service interface containing simple test methods
 * 
 * @author jstaples
 *
 */
@RestController
@RequestMapping("/immortalsTestService")
public class TestService {
    
    /**
     * A simple method useful for approximating the client/server latency.
     * 
     * @return the server's current epoch time in milliseconds
     */
    @RequestMapping(
            method = RequestMethod.GET,
            value="/ping",
            produces=MediaType.TEXT_PLAIN_VALUE
            )
    public String ping() {
        System.out.println("received a ping request\n");
        return "" + System.currentTimeMillis();
    }

}
