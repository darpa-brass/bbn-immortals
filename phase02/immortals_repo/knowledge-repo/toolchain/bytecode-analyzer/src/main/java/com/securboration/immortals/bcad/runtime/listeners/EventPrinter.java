package com.securboration.immortals.bcad.runtime.listeners;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.securboration.immortals.bcad.runtime.IEventListener;


/**
 * Logs events to stdout
 * 
 * @author Securboration
 *
 */
public class EventPrinter implements IEventListener {
  
  private Map<Thread,Integer> stackCounts = new ConcurrentHashMap<>();

  private static void log(String format, Object...args){
    System.out.println(String.format(">> " + Thread.currentThread().getName() + " >> " + format, args));
  }
  
  @Override
  public void reset() {
    log("reset");
    
    stackCounts = new ConcurrentHashMap<>();
  }
  
  private int deltaAndGet(int offset){
    Integer stackCount = stackCounts.get(Thread.currentThread());
    
    if(stackCount == null){
      stackCount = 0;
    }
    
    stackCount += offset;
    
    stackCounts.put(Thread.currentThread(), stackCount);
    
    return stackCount;
  }

  @Override
  public boolean postEntry(String methodHash) {
    log(
        "postEntry [%4d] @%s",
        deltaAndGet(+1),
        methodHash
        );
    
    return true;
  }

  @Override
  public boolean preReturn(String methodHash) {
    log(
        "preReturn [%4d] @%s",
        deltaAndGet(-1),
        methodHash
        );
    
    return true;
  }

  @Override
  public boolean postCatch(String methodHash, Throwable t) {
    log(
        "postCatch [%4d] @%s @@%s",
        deltaAndGet(0),
        methodHash,
        t.getClass().getName()
        );
    
    return true;
  }

  @Override
  public boolean uncaught(String methodHash, Throwable t) {
    log(
        "uncaught [%4d] @%s @@%s",
        deltaAndGet(-1),
        methodHash,
        t.getClass().getName()
        );
    
    return true;
  }

  @Override
  public boolean postControlFlowPathTaken(String pathId) {
    log(
        "control flow @%s",
        pathId
        );
    
    return true;
  }
  

}
