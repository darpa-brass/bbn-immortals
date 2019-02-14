package com.securboration.immortals.ontology.cp3;

import com.securboration.immortals.ontology.constraint.*;
import com.securboration.immortals.ontology.cp1.MartiDBEnvironment;
import com.securboration.immortals.ontology.database.MostCurrentVersion;
import com.securboration.immortals.ontology.database.SchemaLogic;
import com.securboration.immortals.ontology.metrics.MeasurementType;
import com.securboration.immortals.ontology.metrics.Metric;
import com.securboration.immortals.ontology.metrics.VersioningMetric;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.property.impact.*;
import com.securboration.immortals.ontology.resources.DataBase;
import com.securboration.immortals.ontology.resources.Software;
import com.securboration.immortals.ontology.resources.SoftwareLibrary;
import com.securboration.immortals.ontology.resources.logical.Version;
import com.securboration.immortals.ontology.resources.logical.VersioningInfo;

@Ignore
public class ImmortalsSystemLibraryEnv {

    @ConceptInstance
    public static class LibraryVersionConstraint extends ProscriptiveCauseEffectAssertion {
        
        public LibraryVersionConstraint() {
            MeasurementCriterion criterion = new MeasurementCriterion();
            {
                criterion.setCriterion(ValueCriterionType.VALUE_LESS_THAN_EXCLUSIVE);
                VersioningMetric versioningMetric = new VersioningMetric();
                versioningMetric.setLibraryName("commons-IO");
                versioningMetric.setValue("2.0.0");
                MeasurementType measurementType = new MeasurementType();
                measurementType.setCorrespondingProperty(Version.class);
                measurementType.setMeasurementType("We are measuring the version numbers");
                versioningMetric.setMeasurementType(measurementType);
                criterion.setVersioningMetric(versioningMetric);
                criterion.setHumanReadableDescription("Every commons-IO library must reach version 2.0.0");
            }

            ConstraintViolationImpact impact = new ConstraintViolationImpact();
            {
                impact.setConstraintViolationType(ConstraintImpactType.HARD_CONSTRAINT_VIOLATION);
                impact.setDirectionOfViolation(null);
            }

            AbstractResourceBindingSite abstractResourceBindingSite = new AbstractResourceBindingSite();
            {
                abstractResourceBindingSite.setResourceType(SoftwareLibrary.class);
                abstractResourceBindingSite.setHumanReadableDescription("All software libraries are under constraint");
            }

            this.setCriterion(criterion);
            this.setImpact(new ImpactStatement[] {impact});
            this.setApplicableDataType(VersioningInfo.class);
            this.setHumanReadableDescription("All commons-IO libraries must reach version 2.0.0");
        }
    }

    @ConceptInstance
    public static class LibraryUpdateStrategy extends PrescriptiveCauseEffectAssertion {

        public LibraryUpdateStrategy() {

            ConstraintViolationCriterion criterion = new ConstraintViolationCriterion();{
                criterion.setConstraint(new LibraryVersionConstraint());
                criterion.setTriggeringConstraintCriterion(ConstraintCriterionType.WHEN_HARD_VIOLATED);
            }
            this.setCriterion(criterion);

            RemediationImpact impact = new RemediationImpact();{
                impact.setRemediationStrategy(new ImpactOfLibraryUpgrade());
            }
            this.setImpact(new ImpactStatement[] {impact});
            this.setHumanReadableDescription("When a database isn't at the most current version, this strategy mitigates" +
                    "the constraint by applying the impact of schema migration.");

        }
    }

    @ConceptInstance
    public static class ImpactOfLibraryUpgrade extends PredictiveCauseEffectAssertion {
        public ImpactOfLibraryUpgrade() {
            
            ResourceCriterion criterion = new ResourceCriterion();{
                criterion.setCriterion(ResourceCriterionType.WHEN_RESOURCE_ALTERED);
                criterion.setResource(SoftwareLibrary.class);
            }
            
            this.setCriterion(criterion);

            PropertyImpact impact = new PropertyImpact();{
                impact.setImpactedProperty(Version.class);
                impact.setImpactOnProperty(PropertyImpactType.PROPERTY_INCREASES);
            }
            this.setImpact(new ImpactStatement[] {impact});
        }
    }
}
