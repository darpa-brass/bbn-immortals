package mil.darpa.immortals.ontology;

import com.securboration.immortals.ontology.cp.FunctionalitySpec;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

/**
 * Created by awellman@bbn.com on 4/9/18.
 */
@ConceptInstance
public class BaselineFunctionalitySpec extends FunctionalitySpec {
    public BaselineFunctionalitySpec() {
        this.setFunctionalityProvided(BaselineFunctionalAspect.class);
    }
}
