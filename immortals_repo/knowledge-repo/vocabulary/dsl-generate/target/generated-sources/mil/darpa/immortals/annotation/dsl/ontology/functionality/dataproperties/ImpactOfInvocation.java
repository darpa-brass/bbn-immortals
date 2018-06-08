/* WARNING: auto-generated content */
/* generated from class com.securboration.immortals.ontology.functionality.dataproperties.ImpactOfInvocation on 2018.05.14 at 16:12:20 EDT */

package mil.darpa.immortals.annotation.dsl.ontology.functionality.dataproperties;


@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface ImpactOfInvocation{
  public com.securboration.immortals.ontology.functionality.dataproperties.ImpactType impactOfInvocation() default com.securboration.immortals.ontology.functionality.dataproperties.ImpactType.IMPROVES_SIGNIFICANTLY;
  public java.lang.Class<? extends com.securboration.immortals.ontology.property.Property>[] impactedProperties() default {};
  public boolean hidden() default false;
  public com.securboration.immortals.ontology.core.TruthConstraint truthConstraint() default com.securboration.immortals.ontology.core.TruthConstraint.NONE;

/* begin:[FOR IMMORTALS TOOLING USE ONLY] */
  public static final String SEMANTIC_URI="http://darpa.mil/immortals/ontology/r2.0.0/functionality/dataproperties#ImpactOfInvocation";
  public static final String SEMANTIC_VERSION="r2.0.0";
  public static final long GEN_EPOCH_TIMESTAMP=1526328740149L;
  public static final Class<?> BACKING_POJO = com.securboration.immortals.ontology.functionality.dataproperties.ImpactOfInvocation.class;
/* end:[FOR IMMORTALS TOOLING USE ONLY] */
}
