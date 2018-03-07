package com.securboration.immortals.ontology.cp1;

import com.securboration.immortals.ontology.constraint.*;
import com.securboration.immortals.ontology.cp2.ClientServerEnvironment;
import com.securboration.immortals.ontology.database.MostCurrentVersion;
import com.securboration.immortals.ontology.database.SchemaLogic;

import com.securboration.immortals.ontology.functionality.ConfidentialProperty;
import com.securboration.immortals.ontology.functionality.dataproperties.Encrypted;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.property.impact.*;
import com.securboration.immortals.ontology.resources.DataBase;


@Ignore
public class MartiDBEnvironment {
    
    
    
    @ConceptInstance
    public static class MostCurrentSchemaConstraint extends ProscriptiveCauseEffectAssertion {

        public MostCurrentSchemaConstraint() {
            AbstractPropertyCriterion criterion = new AbstractPropertyCriterion();
            {
                criterion.setProperty(MostCurrentVersion.class);
                criterion.setCriterion(PropertyCriterionType.PROPERTY_ABSENT);
                criterion.setHumanReadableDescription("This criterion specifies a situation where a schema is not the most current version.");
            }

            ConstraintViolationImpact impact = new ConstraintViolationImpact();
            {
                impact.setConstraintViolationType(ConstraintImpactType.HARD_CONSTRAINT_VIOLATION);
                impact.setDirectionOfViolation(null);
            }

            AbstractResourceBindingSite abstractResourceBindingSite = new AbstractResourceBindingSite();
            {
                abstractResourceBindingSite.setResourceType(DataBase.class);
                abstractResourceBindingSite.setHumanReadableDescription("Every database is constrained");
            }

            this.setCriterion(criterion);
            this.setAssertionBindingSite(abstractResourceBindingSite);
            this.setImpact(new ImpactStatement[] {impact});
            this.setHumanReadableDescription("All database schemas most be the must current version");
        }
    }
    
    @ConceptInstance
    public static class SchemaUpdateStretegy extends PrescriptiveCauseEffectAssertion {
        
        public SchemaUpdateStretegy() {

            ConstraintViolationCriterion criterion = new ConstraintViolationCriterion();{
                criterion.setConstraint(new MostCurrentSchemaConstraint());
                criterion.setTriggeringConstraintCriterion(ConstraintCriterionType.WHEN_HARD_VIOLATED);
            }
            this.setCriterion(criterion);

            RemediationImpact impact = new RemediationImpact();{
                impact.setRemediationStrategy(new ImpactOfSchemaMigration());
            }
            this.setImpact(new ImpactStatement[] {impact});
            this.setHumanReadableDescription("When a database isn't at the most current version, this strategy mitigates" +
                    "the constraint by applying the impact of schema migration.");
            
        }
    }
    
    @ConceptInstance
    public static class ImpactOfSchemaMigration extends PredictiveCauseEffectAssertion {
        public ImpactOfSchemaMigration() {
            ResourceCriterion criterion = new ResourceCriterion();{
                criterion.setCriterion(ResourceCriterionType.WHEN_RESOURCE_ALTERED);
                criterion.setResource(SchemaLogic.class);
            }
            this.setCriterion(criterion);

            PropertyImpact impact = new PropertyImpact();{
                impact.setImpactedProperty(MostCurrentVersion.class);
                impact.setImpactOnProperty(PropertyImpactType.ADDS);
            }
            this.setImpact(new ImpactStatement[] {impact});
        }
    }
}
