package com.securboration.immortals.bcad.runtime;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a lightweight POJO for use by instrumented code and tuned for
 * performance
 * 
 * @author jstaples
 *
 */
public class TrackingFilter
{
  private static final Map<String,TrackingFilter> filterMap = 
      new ConcurrentHashMap<>();
  
  /**
   * This field should never be modified directly (exposed as public to enable 
   * quick access from an external class).
   * 
   * Use startFiltering() or stopFiltering() instead
   */
  public boolean filterProfiling = false;
  
  /**
   * This field should never be modified directly (exposed as public to enable 
   * quick access from an external class).
   * 
   * Use startFiltering() or stopFiltering() instead
   */
  public boolean filterTraceCalls = false;
  
  /**
   * Instantiate a tracking filter and bind to a specific method
   * 
   * @param methodId the method to which this filter binds
   */
  public TrackingFilter(final String methodId){
    
    //TODO: possibly not thread safe
    filterMap.put(methodId, this);
  };
  
  public void stopFilteringTraceCalls()
  {
    this.filterTraceCalls = false;
  }
  
  public void stopFilteringProfilingCalls()
  {
    this.filterProfiling = false;
  }
  
  public void filterTraceCalls()
  {
    this.filterTraceCalls = true;
  }
  
  public void filterProfilingCalls()
  {
    this.filterProfiling = true;
  }
}
