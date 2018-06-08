/* WARNING: auto-generated content */
/* generated from class com.securboration.immortals.ontology.java.testing.annotation.ProvidedFunctionalityValidationAnnotation on 2018.05.14 at 16:12:20 EDT */

package mil.darpa.immortals.annotation.dsl.ontology.java.testing.annotation;


@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface ProvidedFunctionalityValidationAnnotation{
  public java.lang.String[] intents() default {};
  public java.lang.Class<? extends com.securboration.immortals.ontology.functionality.Functionality> validatedFunctionality() default com.securboration.immortals.ontology.functionality.Functionality.class;
  public java.lang.Class<? extends com.securboration.immortals.ontology.functionality.FunctionalAspect>[] validatedAspects() default {};

/* begin:[FOR IMMORTALS TOOLING USE ONLY] */
  public static final String SEMANTIC_URI="http://darpa.mil/immortals/ontology/r2.0.0/java/testing/annotation#ProvidedFunctionalityValidationAnnotation";
  public static final String SEMANTIC_VERSION="r2.0.0";
  public static final long GEN_EPOCH_TIMESTAMP=1526328740149L;
  public static final Class<?> BACKING_POJO = com.securboration.immortals.ontology.java.testing.annotation.ProvidedFunctionalityValidationAnnotation.class;
/* end:[FOR IMMORTALS TOOLING USE ONLY] */
}
