package com.securboration.immortals.deployment.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

public class DeploymentJsonMetaAspectSet{
        
        private String name;
        private String[] guidsInSet;
        
        public DeploymentJsonMetaAspectSet(){}
        
        DeploymentJsonMetaAspectSet(String name,JSONArray a){
            this.name = name;
            
            List<String> guids = new ArrayList<>();
            for(int i=0;i<a.length();i++){
                guids.add(a.getJSONObject(i).getString("guid"));
            }
            guidsInSet = guids.toArray(new String[]{});
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String[] getGuidsInSet() {
            return guidsInSet;
        }

        public void setGuidsInSet(String[] guidsInSet) {
            this.guidsInSet = guidsInSet;
        }
        
        
//"MetaAspectSet": [
//                          {
//                              "attributes": {},
//                              "guid": "0f7f33c5-62d4-d6f8-9afa-974a9947c140",
//                              "registry": {
//                                  "position": {
//                                      "x": 830,
//                                      "y": 313
//                                  }
//                              }
//                          },
    }