package com.securboration.immortals.bcad.callgraph;


import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.securboration.immortals.bcad.runtime.IEventListener;


public class GraphBuildingListener implements IEventListener {
  
  public interface StackStateVisitor{
    public void postEntry(Stack<String> currentCallStack);
    public void postReturn(Stack<String> currentCallStack);
    public void postCatch(Stack<String> currentCallStack,Throwable t);
    public void postUncaught(Stack<String> currentCallStack,Throwable t);
    public void postControlFlow(Stack<String> currentCallStack);
  }
  
  private static final Logger logger = 
          LoggerFactory.getLogger(GraphBuildingListener.class);
  
  private volatile Map<Thread,Stack<String>> stacks = new ConcurrentHashMap<>();
  
  private final CallgraphBuilder visitor = new CallgraphBuilder();
  
  public GraphBuildingListener(){}
  
  public Map<Thread,Map<Edge,AtomicLong>> getEdges(){
      return visitor.getThreadsToCounts();
  }
  
  public Stack<String> getStack(){
    Stack<String> s = stacks.get(Thread.currentThread());
    
    if(s == null){
      logger.info("  creating new stack for thread %s\n",Thread.currentThread());
      logger.info("  currently tracking %d threads\n",stacks.size());
      for(Thread t:stacks.keySet()){
        logger.info("\t%s: %d entries on stack\n",t,stacks.get(t).size());
      }
      
      s = new Stack<String>();
      stacks.put(Thread.currentThread(), s);
    }
    
    return s;
  }
  
  private static void abort(){
    logger.error("ABORT: the JVM should probably abort due to an error condition in RSMT");
  }
  
  
  
  

  @Override
  public void reset() {
    logger.info("clearing all call stacks\n");
    
    stacks = new ConcurrentHashMap<>();
  }

  @Override
  public boolean postEntry(String methodHash) {
    
    Stack<String> stack = getStack();
    
    stack.push(methodHash);
    
    visitor.postEntry(stack);
    
//    error(printCurrentStack(stack));
    
    return true;
  }
  
  /**
   * Pop off all control flow events from the stack up to the last method call
   * 
   * @param stack
   * @param methodHash
   */
  private void popControlFlow(Stack<String> stack,String methodHash){
    
    boolean stop = stack.isEmpty();
    while(!stop){
      String top = stack.peek();
      
      if(top.startsWith("block-")){
        stack.pop();
      } else {
        stop = true;
      }
      
      if(stack.isEmpty()){
        stop = true;
      }
    }
  }

  @Override
  public boolean preReturn(String methodHash) {
    Stack<String> stack = getStack();
    
    popControlFlow(stack,methodHash);
    
    String aboutToBePopped = stack.peek();
    
    if(!aboutToBePopped.equals(methodHash)){
      logger.error(
          "stack misalignment detected in thread %s:\n  %s\n   popped vs expected\n  %s\n%s",
          Thread.currentThread().getName(),
          aboutToBePopped,
          methodHash,
          printCurrentStack(stack)
          );
      
      abort();
    }
    
    stack.pop();
    
    visitor.postReturn(stack);
    
//    error(printCurrentStack(stack));
    
    return true;
  }
  
  private static String printCurrentStack(Stack<String> stack){
    StringBuilder sb = new StringBuilder();
    
    sb.append("stack dump for thread " + Thread.currentThread().getName() + ":\n");
    int i = 0;
    for(String s:stack){
      sb.append(String.format("%4d  ", i++) + s);
      sb.append("\n");
    }
    
    return sb.toString();
  }

  @Override
  public boolean postCatch(String methodHash, Throwable t) {
    
    visitor.postCatch(getStack(),t);
    
    return true;
  }

  @Override
  public boolean uncaught(String methodHash, Throwable t) {
    Stack<String> stack = getStack();
    
    if(stack.size() == 0){
      logger.error(
          "stack underflow detected in thread %s at: %s\n",
          Thread.currentThread().getName(),
          methodHash
          );
      
      abort();
      
      return true;
    }
    
    popControlFlow(stack,methodHash);
    
    String top = stack.pop();
    
    if(!top.equals(methodHash)){
      logger.error(
          "stack misalignment detected:\n  %s\n   on top vs \n  %s\n%s",
          top,
          methodHash,
          printCurrentStack(stack)
          );
      
      abort();
    }
    
    visitor.postUncaught(stack,t);
    
    return true;
  }

  @Override
  public boolean postControlFlowPathTaken(String pathId) {
      //TODO: track control flow
      
//    Stack<String> stack = getStack();
//    
//    stack.push(pathId);
//    
//    visitor.postControlFlow(stack);
    
    return true;
  }

}
