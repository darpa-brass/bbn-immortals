/* WARNING: auto-generated content */
/* generated from class com.securboration.immortals.ontology.functionality.dataproperties.QualitativeFidelityAssertion on 2018.05.14 at 16:12:20 EDT */

package mil.darpa.immortals.annotation.dsl.ontology.functionality.dataproperties;


@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface QualitativeFidelityAssertion{
  public java.lang.Class<? extends com.securboration.immortals.ontology.functionality.dataproperties.Fidelity> subjectOfAssertion() default com.securboration.immortals.ontology.functionality.dataproperties.Fidelity.class;
  public com.securboration.immortals.ontology.constraint.BinaryComparisonOperatorType operator() default com.securboration.immortals.ontology.constraint.BinaryComparisonOperatorType.GREATER_THAN_EXCLUSIVE;
  public java.lang.Class<? extends com.securboration.immortals.ontology.functionality.dataproperties.Fidelity>[] objectOfAssertion() default {};

/* begin:[FOR IMMORTALS TOOLING USE ONLY] */
  public static final String SEMANTIC_URI="http://darpa.mil/immortals/ontology/r2.0.0/functionality/dataproperties#QualitativeFidelityAssertion";
  public static final String SEMANTIC_VERSION="r2.0.0";
  public static final long GEN_EPOCH_TIMESTAMP=1526328740149L;
  public static final Class<?> BACKING_POJO = com.securboration.immortals.ontology.functionality.dataproperties.QualitativeFidelityAssertion.class;
/* end:[FOR IMMORTALS TOOLING USE ONLY] */
}
