/* WARNING: auto-generated content */
/* generated from class com.securboration.immortals.ontology.image.fidelity.ResolutionFidelity on 2018.05.14 at 16:12:20 EDT */

package mil.darpa.immortals.annotation.dsl.ontology.image.fidelity;


@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface ResolutionFidelity{
  public int width() default -2147483648;
  public int height() default -2147483648;
  public mil.darpa.immortals.annotation.dsl.ontology.functionality.dataproperties.QualitativeFidelityAssertion[] fidelityRelationships() default {};
  public boolean hidden() default false;
  public com.securboration.immortals.ontology.core.TruthConstraint truthConstraint() default com.securboration.immortals.ontology.core.TruthConstraint.NONE;

/* begin:[FOR IMMORTALS TOOLING USE ONLY] */
  public static final String SEMANTIC_URI="http://darpa.mil/immortals/ontology/r2.0.0/image/fidelity#ResolutionFidelity";
  public static final String SEMANTIC_VERSION="r2.0.0";
  public static final long GEN_EPOCH_TIMESTAMP=1526328740149L;
  public static final Class<?> BACKING_POJO = com.securboration.immortals.ontology.image.fidelity.ResolutionFidelity.class;
/* end:[FOR IMMORTALS TOOLING USE ONLY] */
}
