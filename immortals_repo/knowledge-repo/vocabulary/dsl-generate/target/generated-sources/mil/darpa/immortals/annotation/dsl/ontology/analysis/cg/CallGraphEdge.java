/* WARNING: auto-generated content */
/* generated from class com.securboration.immortals.ontology.analysis.cg.CallGraphEdge on 2018.05.14 at 16:12:20 EDT */

package mil.darpa.immortals.annotation.dsl.ontology.analysis.cg;


@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface CallGraphEdge{
  public mil.darpa.immortals.annotation.dsl.ontology.measurement.CodeUnitPointer originMethod() default @mil.darpa.immortals.annotation.dsl.ontology.measurement.CodeUnitPointer;
  public mil.darpa.immortals.annotation.dsl.ontology.measurement.CodeUnitPointer calledMethod() default @mil.darpa.immortals.annotation.dsl.ontology.measurement.CodeUnitPointer;

/* begin:[FOR IMMORTALS TOOLING USE ONLY] */
  public static final String SEMANTIC_URI="http://darpa.mil/immortals/ontology/r2.0.0/analysis/cg#CallGraphEdge";
  public static final String SEMANTIC_VERSION="r2.0.0";
  public static final long GEN_EPOCH_TIMESTAMP=1526328740149L;
  public static final Class<?> BACKING_POJO = com.securboration.immortals.ontology.analysis.cg.CallGraphEdge.class;
/* end:[FOR IMMORTALS TOOLING USE ONLY] */
}
