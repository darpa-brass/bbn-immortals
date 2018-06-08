/* WARNING: auto-generated content */
/* generated from class com.securboration.immortals.ontology.profiling.MetricValue on 2018.05.14 at 16:12:20 EDT */

package mil.darpa.immortals.annotation.dsl.ontology.profiling;


@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface MetricValue{
  public com.securboration.immortals.ontology.profiling.MetricType metric() default com.securboration.immortals.ontology.profiling.MetricType.CPU_CYCLES_WALL;
  public mil.darpa.immortals.annotation.dsl.ontology.profiling.Value value() default @mil.darpa.immortals.annotation.dsl.ontology.profiling.Value;

/* begin:[FOR IMMORTALS TOOLING USE ONLY] */
  public static final String SEMANTIC_URI="http://darpa.mil/immortals/ontology/r2.0.0/profiling#MetricValue";
  public static final String SEMANTIC_VERSION="r2.0.0";
  public static final long GEN_EPOCH_TIMESTAMP=1526328740149L;
  public static final Class<?> BACKING_POJO = com.securboration.immortals.ontology.profiling.MetricValue.class;
/* end:[FOR IMMORTALS TOOLING USE ONLY] */
}
