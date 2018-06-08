/* WARNING: auto-generated content */
/* generated from class com.securboration.immortals.ontology.functionality.alg.encryption.SymmetricBlockCipher on 2018.05.14 at 16:12:20 EDT */

package mil.darpa.immortals.annotation.dsl.ontology.functionality.alg.encryption;


@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface SymmetricBlockCipher{
  public mil.darpa.immortals.annotation.dsl.ontology.algorithm.AlgorithmStandardProperty encryptionSpec() default @mil.darpa.immortals.annotation.dsl.ontology.algorithm.AlgorithmStandardProperty;
  public mil.darpa.immortals.annotation.dsl.ontology.functionality.alg.encryption.properties.BlockBased blockSpec() default @mil.darpa.immortals.annotation.dsl.ontology.functionality.alg.encryption.properties.BlockBased;
  public mil.darpa.immortals.annotation.dsl.ontology.functionality.alg.encryption.properties.KeyLength keySpec() default @mil.darpa.immortals.annotation.dsl.ontology.functionality.alg.encryption.properties.KeyLength;
  public mil.darpa.immortals.annotation.dsl.ontology.algorithm.AlgorithmProperty[] properties() default {};

/* begin:[FOR IMMORTALS TOOLING USE ONLY] */
  public static final String SEMANTIC_URI="http://darpa.mil/immortals/ontology/r2.0.0/functionality/alg/encryption#SymmetricBlockCipher";
  public static final String SEMANTIC_VERSION="r2.0.0";
  public static final long GEN_EPOCH_TIMESTAMP=1526328740149L;
  public static final Class<?> BACKING_POJO = com.securboration.immortals.ontology.functionality.alg.encryption.SymmetricBlockCipher.class;
/* end:[FOR IMMORTALS TOOLING USE ONLY] */
}
