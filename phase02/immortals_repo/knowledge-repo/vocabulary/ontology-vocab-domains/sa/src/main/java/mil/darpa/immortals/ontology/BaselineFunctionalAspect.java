package mil.darpa.immortals.ontology;

import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

/**
 * Baseline functionality tag to be used on tests
 * 
 * This isn't very specific, but consolidating and scanning JUnit/TestNG annotations and category/group tags may take 
 * time to fully flesh out and implement
 * 
 * Created by awellman@bbn.com on 4/3/18.
 */
// TODO: Extract functionality from "@Test" annotations instead?
@ConceptInstance
public class BaselineFunctionalAspect extends DefaultAspectBase {
}
