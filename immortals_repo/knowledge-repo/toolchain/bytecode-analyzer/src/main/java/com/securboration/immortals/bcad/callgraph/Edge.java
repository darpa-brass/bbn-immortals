package com.securboration.immortals.bcad.callgraph;


public class Edge {
  
  public Edge(String from, String to) {
    super();
    this.from = from;
    this.to = to;
  }
  
  private String from;

  private String to;
  
  @Override
  public boolean equals(Object o){
    if(!(o instanceof Edge)){
      return false;
    }
    
    Edge e = (Edge)o;
    
    if(!e.from.equals(this.from)){
      return false;
    }
    
    if(!e.to.equals(this.to)){
      return false;
    }
    
    return true;
  }
  
  @Override
  public int hashCode(){
    return from.hashCode() * 17 + to.hashCode();
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }
  
}