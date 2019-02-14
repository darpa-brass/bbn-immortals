package com.securboration.immortals.bcad.runtime;


/**
 * Generic interface to be implemented by components in an event listener
 * pipeline. Each event returns a boolean indicating whether it should propagate
 * to subsequent listeners (true) or be "eaten" by the current listener (false)
 * 
 * @author Securboration
 *
 */
public interface IEventListener {
  
  /**
   * Reset all state associated with the listener (called only by test logic)
   */
  public void reset();
  
  /**
   * Called just after a method is invoked
   * 
   * @param methodHash
   *          uniquely identifies the invoked method
   * @return true iff this event should propagate to any subsequent listners
   */
  public boolean postEntry(
      final String methodHash
      );
  
  /**
   * Called just before a method returns
   * 
   * @param methodHash
   *          uniquely identifies the invoked method
   * @return true iff this event should propagate to any subsequent listeners
   */
  public boolean preReturn(
      final String methodHash
      );
  
  /**
   * Called just after a catch block is entered
   * 
   * @param methodHash
   *          uniquely identifies the method in which the catch occurred
   * @param t
   *          the caught exception
   * @return true iff this event should propagate to any subsequent listeners
   */
  public boolean postCatch(
      final String methodHash,
      final Throwable t
      );
  
  /**
   * Called just before an uncaught exception throw
   * 
   * @param methodHash the method in which the exception throw originates
   * @param t the uncaught exception
   * @return true iff this event should propagate to any subsequent listeners
   */
  public boolean uncaught(
      final String methodHash,
      final Throwable t
      );
  
  /**
   * Called just after a control flow path is taken. E.g., given the method
   * below, the following are valid sequences:
   * 
   * 1,2,3 1,2,2,2,2,2,2,3 1,2,4
   * 
   * But we'd never see: 4 (because 1 is always reached first) 2 (because 1 is
   * always reached first)
   * 
   * 
   * public void f(int x){
   * 
   * //1
   * 
   * for(int i=0;i<x;i++){
   * 
   * //2
   * 
   * if(i%77 == 0){ //3 return; }
   * 
   * }
   * 
   * //4
   * 
   * }
   * 
   * @param pathId
   *          uniquely identifies the path taken
   * @return true iff this event should propagate to any subsequent listeners
   */
  public boolean postControlFlowPathTaken(
      final String pathId
      );
  
}
