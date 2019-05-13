package com.securboration.immortals.bcd;

public interface IClasspathVisitor {
  
  public void visitClasspathElement(
          final String classpathName,
          final byte[] classpathData
          );
  
  public default void beforeTraversal() {};
  
  public default void afterTraversal() {};
}
