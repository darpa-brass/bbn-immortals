/* WARNING: auto-generated content */
/* generated from class com.securboration.immortals.ontology.image.fidelity.ColorChannel on 2018.05.14 at 16:12:20 EDT */

package mil.darpa.immortals.annotation.dsl.ontology.image.fidelity;


@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface ColorChannel{
  public int bitDepth() default -2147483648;
  public com.securboration.immortals.ontology.image.fidelity.ColorType channelColor() default com.securboration.immortals.ontology.image.fidelity.ColorType.RED;

/* begin:[FOR IMMORTALS TOOLING USE ONLY] */
  public static final String SEMANTIC_URI="http://darpa.mil/immortals/ontology/r2.0.0/image/fidelity#ColorChannel";
  public static final String SEMANTIC_VERSION="r2.0.0";
  public static final long GEN_EPOCH_TIMESTAMP=1526328740149L;
  public static final Class<?> BACKING_POJO = com.securboration.immortals.ontology.image.fidelity.ColorChannel.class;
/* end:[FOR IMMORTALS TOOLING USE ONLY] */
}
