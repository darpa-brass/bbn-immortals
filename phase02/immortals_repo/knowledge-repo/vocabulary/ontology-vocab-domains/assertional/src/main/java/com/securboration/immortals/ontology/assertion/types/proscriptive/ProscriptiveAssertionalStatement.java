package com.securboration.immortals.ontology.assertion.types.proscriptive;

import com.securboration.immortals.ontology.assertion.AssertionalStatement;
import com.securboration.immortals.ontology.assertion.binding.BindingSiteBase;
import com.securboration.immortals.ontology.expression.BooleanExpression;
import com.securboration.immortals.ontology.impact.constraint.ViolationImpact;

/**
 * Describes the desired state of affairs.
 * 
 * For example:
 * <ul>
 * <li>
 * There should be at least 5 location update messages per minute transmitted
 * from client to server
 * </li>
 * <li>
 * Location update messages should have at most 20 meters
 * of SEP-95 accuracy 
 * </li>
 * <li>
 * SymmetricCipher code units must use at least 192-bit keys
 * </li>
 * <li>
 * AsymmetricCipher code units must use at least 8192-bit keys 
 * </li>
 * <li>
 * AsymmetricCipher
 * code units must use post-quantum secure algorithms
 * </li>
 * </ul>
 * 
 * @author jstaples
 *
 */
public class ProscriptiveAssertionalStatement extends AssertionalStatement {
    
    /**
     * Describes the subject of a proscriptive assertion. E.g., In the assertion
     * "SymmetricCipher code units must use at least 192-bit keys",
     * "SymmetricCipher code units" is the subject.
     */
    private BindingSiteBase subjectOfProscriptiveAssertion;
    
    /**
     * Describes the criterion of a proscriptive assertion. E.g., In the
     * assertion "SymmetricCipher code units must use at least 192-bit keys",
     * "key_length >= 192-bits" is the criterion.  At the end of the day, a
     * criterion is simply an expression that can be unambiguously evaluated to
     * produce a value of true or false.
     */
    private BooleanExpression proscriptiveAssertionCriterion;
    
    /**
     * Describes the impact of <i>violating</i> the proscriptive criterion.
     * E.g., "Failing to meet the keylength constraint results in the creation
     * of a SEVERE constraint"
     */
    private ViolationImpact impactOfViolation;

    
    public BindingSiteBase getSubjectOfProscriptiveAssertion() {
        return subjectOfProscriptiveAssertion;
    }

    
    public void setSubjectOfProscriptiveAssertion(
            BindingSiteBase subjectOfProscriptiveAssertion) {
        this.subjectOfProscriptiveAssertion = subjectOfProscriptiveAssertion;
    }

    
    public BooleanExpression getProscriptiveAssertionCriterion() {
        return proscriptiveAssertionCriterion;
    }

    
    public void setProscriptiveAssertionCriterion(
            BooleanExpression proscriptiveAssertionCriterion) {
        this.proscriptiveAssertionCriterion = proscriptiveAssertionCriterion;
    }

    
    public ViolationImpact getImpactOfViolation() {
        return impactOfViolation;
    }

    
    public void setImpactOfViolation(ViolationImpact impactOfViolation) {
        this.impactOfViolation = impactOfViolation;
    }

}
