/* WARNING: auto-generated content */
/* generated from class com.securboration.immortals.ontology.FunctionalityTest on 2018.05.14 at 16:12:20 EDT */

package mil.darpa.immortals.annotation.dsl.ontology;


@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface FunctionalityTest{
  public java.lang.Class<? extends com.securboration.immortals.ontology.functionality.Functionality> functionality() default com.securboration.immortals.ontology.functionality.Functionality.class;
  public java.lang.String humanReadableDesc() default "";

/* begin:[FOR IMMORTALS TOOLING USE ONLY] */
  public static final String SEMANTIC_URI="http://darpa.mil/immortals/ontology/r2.0.0#FunctionalityTest";
  public static final String SEMANTIC_VERSION="r2.0.0";
  public static final long GEN_EPOCH_TIMESTAMP=1526328740149L;
  public static final Class<?> BACKING_POJO = com.securboration.immortals.ontology.FunctionalityTest.class;
/* end:[FOR IMMORTALS TOOLING USE ONLY] */
}
