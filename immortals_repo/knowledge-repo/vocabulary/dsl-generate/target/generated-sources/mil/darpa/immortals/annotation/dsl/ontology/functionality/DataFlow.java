/* WARNING: auto-generated content */
/* generated from class com.securboration.immortals.ontology.functionality.DataFlow on 2018.05.14 at 16:12:20 EDT */

package mil.darpa.immortals.annotation.dsl.ontology.functionality;


@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface DataFlow{
  public java.lang.Class<? extends com.securboration.immortals.ontology.functionality.datatype.DataType> type() default com.securboration.immortals.ontology.functionality.datatype.DataType.class;
  public mil.darpa.immortals.annotation.dsl.ontology.property.Property[] properties() default {};
  public java.lang.String flowName() default "";
  public java.lang.String specTag() default "";

/* begin:[FOR IMMORTALS TOOLING USE ONLY] */
  public static final String SEMANTIC_URI="http://darpa.mil/immortals/ontology/r2.0.0/functionality#DataFlow";
  public static final String SEMANTIC_VERSION="r2.0.0";
  public static final long GEN_EPOCH_TIMESTAMP=1526328740149L;
  public static final Class<?> BACKING_POJO = com.securboration.immortals.ontology.functionality.DataFlow.class;
/* end:[FOR IMMORTALS TOOLING USE ONLY] */
}
