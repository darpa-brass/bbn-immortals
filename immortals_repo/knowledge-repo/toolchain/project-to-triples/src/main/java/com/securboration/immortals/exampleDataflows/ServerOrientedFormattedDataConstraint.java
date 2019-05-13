package com.securboration.immortals.exampleDataflows;

import com.securboration.immortals.ontology.constraint.ConstraintCriterionType;
import com.securboration.immortals.ontology.constraint.ConstraintImpactType;
import com.securboration.immortals.ontology.constraint.DirectionOfViolationType;
import com.securboration.immortals.ontology.constraint.XmlResourceImpactType;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.property.impact.*;
import com.securboration.immortals.ontology.property.impact.StructuredDocumentCriterionType;
import com.securboration.immortals.ontology.property.impact.StructuredDocumentImpact;
import com.securboration.immortals.ontology.property.impact.StructuredDocumentImpactType;
import com.securboration.immortals.ontology.property.impact.StructuredDocumentVersionCriterion;
import com.securboration.immortals.ontology.resources.Client;
import com.securboration.immortals.ontology.resources.Server;
import com.securboration.immortals.ontology.resources.xml.StructuredDocument;
import com.securboration.immortals.ontology.resources.xml.XmlDocument;

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

        StructuredDocumentVersionCriterion structuredDocumentVersionCriterion = new StructuredDocumentVersionCriterion();
        structuredDocumentVersionCriterion.setStructuredDocument(XmlDocument.class);
        structuredDocumentVersionCriterion.setStructuredDocumentCriterionType(StructuredDocumentCriterionType.VERSION_DIFFERENT);
        this.setCriterion(structuredDocumentVersionCriterion);

        this.setApplicableDataType(StructuredDocument.class);
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

            StructuredDocumentImpact structuredDocumentImpact = new StructuredDocumentImpact();
            structuredDocumentImpact.setImpactedData(XmlDocument.class);
            structuredDocumentImpact.setImpactType(StructuredDocumentImpactType.FORMAT_CHANGE);

            XmlResourceImpact resourceImpact = new XmlResourceImpact();
            resourceImpact.setImpactedResource(Client.class);
            resourceImpact.setTargetResource(Server.class);
            resourceImpact.setXmlResourceImpactType(XmlResourceImpactType.XML_SCHEMA_CHANGE);
            structuredDocumentImpact.setApplicableResource(resourceImpact);

            this.setImpact(new ImpactStatement[]{structuredDocumentImpact});
        }
    }
}
