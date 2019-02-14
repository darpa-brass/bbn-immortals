package com.securboration.immortals.exampleDataflows;

import com.securboration.immortals.ontology.constraint.ConstraintCriterionType;
import com.securboration.immortals.ontology.constraint.ConstraintImpactType;
import com.securboration.immortals.ontology.constraint.DirectionOfViolationType;
import com.securboration.immortals.ontology.constraint.XmlResourceImpactType;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.property.impact.*;
import com.securboration.immortals.ontology.resources.Client;
import com.securboration.immortals.ontology.resources.FormattedData;
import com.securboration.immortals.ontology.resources.Server;
import com.securboration.immortals.ontology.resources.xml.XmlInstance;

@ConceptInstance
public class ServerOrientedFormattedDataConstraint extends ProscriptiveCauseEffectAssertion {

    public ServerOrientedFormattedDataConstraint() {
        ConstraintViolationImpact impact = new ConstraintViolationImpact();
        impact.setConstraintViolationType(ConstraintImpactType.HARD_CONSTRAINT_VIOLATION);
        impact.setDirectionOfViolation(DirectionOfViolationType.UNDERSHOOT);
        this.setImpact(new ImpactStatement[]{impact});

        AbstractResourceBindingSite resourceBindingSiteServer = new AbstractResourceBindingSite();
        resourceBindingSiteServer.setResourceType(Server.class);
        AbstractResourceBindingSite resourceBindingSiteClient = new AbstractResourceBindingSite();
        resourceBindingSiteClient.setResourceType(Client.class);
        this.setAssertionBindingSites(new AssertionBindingSite[]{resourceBindingSiteClient, resourceBindingSiteServer});

        FormattedDataVersionCriterion formattedDataVersionCriterion = new FormattedDataVersionCriterion();
        formattedDataVersionCriterion.setFormattedData(XmlInstance.class);
        formattedDataVersionCriterion.setFormattedDataCriterionType(FormattedDataCriterionType.VERSION_DIFFERENT);
        this.setCriterion(formattedDataVersionCriterion);

        this.setApplicableDataType(FormattedData.class);
        this.setHumanReadableDescription("Client devices transmitting xml messages to the server must adhere to the version present");
    }

    @ConceptInstance
    public static class XsltImplementationStrategy extends PrescriptiveCauseEffectAssertion {

        public XsltImplementationStrategy() {
            ConstraintViolationCriterion criterion = new ConstraintViolationCriterion();
            criterion.setConstraint(new ServerOrientedFormattedDataConstraint());
            criterion.setTriggeringConstraintCriterion(ConstraintCriterionType.WHEN_HARD_VIOLATED);

            RemediationImpact remediationImpact = new RemediationImpact();
            remediationImpact.setRemediationStrategy(new XsltTransformImpact());

            this.setCriterion(criterion);
            this.setImpact(new ImpactStatement[]{remediationImpact});
        }
    }

    @ConceptInstance
    public static class XsltTransformImpact extends PredictiveCauseEffectAssertion {

        public XsltTransformImpact() {

            FormattedDataImpact formattedDataImpact = new FormattedDataImpact();
            formattedDataImpact.setImpactedData(XmlInstance.class);
            formattedDataImpact.setImpactType(FormattedDataImpactType.FORMAT_CHANGE);

            XmlResourceImpact resourceImpact = new XmlResourceImpact();
            resourceImpact.setImpactedResource(Client.class);
            resourceImpact.setTargetResource(Server.class);
            resourceImpact.setXmlResourceImpactType(XmlResourceImpactType.XML_SCHEMA_CHANGE);
            formattedDataImpact.setApplicableResource(resourceImpact);

            this.setImpact(new ImpactStatement[]{formattedDataImpact});
        }
    }
}
