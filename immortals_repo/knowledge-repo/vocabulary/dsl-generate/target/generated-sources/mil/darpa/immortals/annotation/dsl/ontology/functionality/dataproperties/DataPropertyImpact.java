/* WARNING: auto-generated content */
/* generated from class com.securboration.immortals.ontology.functionality.dataproperties.DataPropertyImpact on 2018.05.14 at 16:12:20 EDT */

package mil.darpa.immortals.annotation.dsl.ontology.functionality.dataproperties;


@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface DataPropertyImpact{
  public java.lang.Class<? extends com.securboration.immortals.ontology.functionality.datatype.DataProperty> propertyType() default com.securboration.immortals.ontology.functionality.datatype.DataProperty.class;
  public com.securboration.immortals.ontology.functionality.dataproperties.ImpactType impact() default com.securboration.immortals.ontology.functionality.dataproperties.ImpactType.IMPROVES_SIGNIFICANTLY;
  public boolean hidden() default false;
  public com.securboration.immortals.ontology.core.TruthConstraint truthConstraint() default com.securboration.immortals.ontology.core.TruthConstraint.NONE;

/* begin:[FOR IMMORTALS TOOLING USE ONLY] */
  public static final String SEMANTIC_URI="http://darpa.mil/immortals/ontology/r2.0.0/functionality/dataproperties#DataPropertyImpact";
  public static final String SEMANTIC_VERSION="r2.0.0";
  public static final long GEN_EPOCH_TIMESTAMP=1526328740149L;
  public static final Class<?> BACKING_POJO = com.securboration.immortals.ontology.functionality.dataproperties.DataPropertyImpact.class;
/* end:[FOR IMMORTALS TOOLING USE ONLY] */
}
