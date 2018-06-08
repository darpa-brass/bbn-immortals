/* WARNING: auto-generated content */
/* generated from class com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase on 2018.05.14 at 16:12:20 EDT */

package mil.darpa.immortals.annotation.dsl.ontology.functionality.aspects;


@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface DefaultAspectBase{
  public java.lang.String aspectId() default "";
  public java.lang.Class<? extends com.securboration.immortals.ontology.core.Resource>[] aspectSpecificResourceDependencies() default {};
  public java.lang.Class<? extends com.securboration.immortals.ontology.functionality.FunctionalAspect> inverseAspect() default com.securboration.immortals.ontology.functionality.FunctionalAspect.class;
  public mil.darpa.immortals.annotation.dsl.ontology.property.Property[] aspectProperties() default {};
  public mil.darpa.immortals.annotation.dsl.ontology.functionality.Input[] inputs() default {};
  public mil.darpa.immortals.annotation.dsl.ontology.functionality.Output[] outputs() default {};
  public mil.darpa.immortals.annotation.dsl.ontology.property.impact.ImpactStatement[] impactStatements() default {};

/* begin:[FOR IMMORTALS TOOLING USE ONLY] */
  public static final String SEMANTIC_URI="http://darpa.mil/immortals/ontology/r2.0.0/functionality/aspects#DefaultAspectBase";
  public static final String SEMANTIC_VERSION="r2.0.0";
  public static final long GEN_EPOCH_TIMESTAMP=1526328740149L;
  public static final Class<?> BACKING_POJO = com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase.class;
/* end:[FOR IMMORTALS TOOLING USE ONLY] */
}
