/* WARNING: auto-generated content */
/* generated from class com.securboration.immortals.ontology.dfu.annotation.DfuAnnotation on 2018.05.14 at 16:12:20 EDT */

package mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation;


@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface DfuAnnotation{
  public java.lang.Class<? extends com.securboration.immortals.ontology.functionality.Functionality> functionalityBeingPerformed() default com.securboration.immortals.ontology.functionality.Functionality.class;
  public java.lang.Class<? extends com.securboration.immortals.ontology.core.Resource>[] resourceDependencies() default {};
  public java.lang.String[] resourceDependencyUris() default {};
  public java.lang.Class<? extends com.securboration.immortals.ontology.functionality.FunctionalAspect>[] functionalAspects() default {};
  public mil.darpa.immortals.annotation.dsl.ontology.property.Property[] properties() default {};
  public java.lang.String tag() default "";

/* begin:[FOR IMMORTALS TOOLING USE ONLY] */
  public static final String SEMANTIC_URI="http://darpa.mil/immortals/ontology/r2.0.0/dfu/annotation#DfuAnnotation";
  public static final String SEMANTIC_VERSION="r2.0.0";
  public static final long GEN_EPOCH_TIMESTAMP=1526328740149L;
  public static final Class<?> BACKING_POJO = com.securboration.immortals.ontology.dfu.annotation.DfuAnnotation.class;
/* end:[FOR IMMORTALS TOOLING USE ONLY] */
}
