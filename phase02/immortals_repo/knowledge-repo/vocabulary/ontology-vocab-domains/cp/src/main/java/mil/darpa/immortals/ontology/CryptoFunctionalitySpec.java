package mil.darpa.immortals.ontology;

import com.securboration.immortals.ontology.cp.FunctionalitySpec;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

/**
 * Created by awellman@bbn.com on 4/9/18.
 */
@ConceptInstance
public class CryptoFunctionalitySpec extends FunctionalitySpec {
    public CryptoFunctionalitySpec() {
        this.setFunctionalityProvided(CryptoFunctionalAspect.class);
    }
}
