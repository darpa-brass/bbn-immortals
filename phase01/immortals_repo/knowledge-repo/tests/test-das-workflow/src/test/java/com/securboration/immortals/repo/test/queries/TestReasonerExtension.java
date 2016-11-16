package com.securboration.immortals.repo.test.queries;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Derivation;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.junit.Assert;
import org.junit.Test;

import com.securboration.immortals.o2t.ontology.OntologyHelper;

public class TestReasonerExtension {
    
    private static void assertNumMatches(
            Model m,
            Resource s,
            Property p,
            RDFNode o,
            final int matchCount
            ){
        
        StringBuilder sb = new StringBuilder();
        int count = 0;
        
        StmtIterator i = m.listStatements(s, p, o);
        while(i.hasNext()){
            Statement match = i.next();
            
            sb.append(String.format(
                ">\tmatch {?s=%s, ?p=%s, ?o=%s}\n", 
                match.getSubject(),match.getPredicate(),match.getObject()
                ));

            count++;
        }

        final String message = 
                String.format(
                    "expected %d matches on query {?s=%s, ?p=%s, ?o=%s} " +
                    "but found %d:\n%s", 
                    matchCount,
                    s,p,o,
                    count,sb.toString()
                    );
        
        Assert.assertEquals(message, matchCount, count);
        
        System.out.println(
            "> PASS: " + message.replace(" but found ", " and found ")
            );
    }
    
    private static String tab(final String s,final String tabbing){
        return tabbing+s.replace("\n", "\n"+tabbing);
    }
    
    @Test
    public void testCustomRules() throws IOException{
        
        Model model = ModelFactory.createDefaultModel();
        
        final Resource a = model.getResource("r1");
        final Resource b = model.getResource("r2");
        final Resource c = model.getResource("r3");
        final Resource d = model.getResource("r4");
        
        final Property p = model.getProperty("p1");
        final Property q = model.getProperty("p2");
        final Property r = model.getProperty("p3");
        
        
        //assert some initial triples
        {
            model.add(a, p, b);
            model.add(b, p, c);
            model.add(c, q, d);
        }
        
        //print initial model
        {
            System.out.println(
                "initial model:"
                );
            System.out.println(
                tab(OntologyHelper.serializeModel(model, "TURTLE", false),"\t")
                );
            System.out.println();
        }
        
        //verify the model contains no inferred triples
        {
            assertNumMatches(model,a,p,null,1);
            assertNumMatches(model,a,p,c,0);
            assertNumMatches(model,a,null,d,0);
            
            assertNumMatches(model,b,p,null,1);
            assertNumMatches(model,b,null,d,0);
            
            assertNumMatches(model,null,p,null,2);
            assertNumMatches(model,null,q,null,1);
            assertNumMatches(model,null,r,null,0);
            
            assertNumMatches(model,null,null,null,3);
        }
        
        //define several transitive closures
        final String rules = 
                "[rule1: (?a p1 ?b) (?b p1 ?c) -> (?a p1 ?c)]\n"+
                "[rule2: (?a p1 ?b) (?b p2 ?c) -> (?a p3 ?c)]\n"+
                ""
                ;
        
        //print closues
        {
            System.out.println("rules:");
            System.out.println(tab(rules,"\t"));
            System.out.println();
        }
        
        //create a new reasoner and inference model using the rules
        List<Rule> ruleSet = new ArrayList<>();
        Reasoner reasoner = new GenericRuleReasoner(ruleSet);
        reasoner.setDerivationLogging(true);
        
        //add rules
        {
            ruleSet.addAll(
                Rule.parseRules(rules)
                );
//            ruleSet.addAll(
//                ((GenericRuleReasoner) ReasonerRegistry.getRDFSReasoner()).getRules()
//                );
        }
        
        InfModel inferenceModel = ModelFactory.createInfModel(reasoner, model);
        
        //print the inference model contents
        {
            System.out.println("inf model:");
            System.out.println(
                tab(OntologyHelper.serializeModel(inferenceModel, "TURTLE", false),"\t")
                );
            System.out.println();
        }
        
        //verify the inference model contains inferred triples
        {
            assertNumMatches(inferenceModel,a,p,null,2);
            assertNumMatches(inferenceModel,a,q,null,0);
            assertNumMatches(inferenceModel,a,r,null,1);
            assertNumMatches(inferenceModel,a,p,b,1);
            assertNumMatches(inferenceModel,a,p,c,1);
            assertNumMatches(inferenceModel,a,r,d,1);
            
            assertNumMatches(inferenceModel,b,p,null,1);
            assertNumMatches(inferenceModel,b,q,null,0);
            assertNumMatches(inferenceModel,b,r,null,1);
            assertNumMatches(inferenceModel,b,p,c,1);
            assertNumMatches(inferenceModel,b,r,d,1);
            
            assertNumMatches(inferenceModel,c,null,null,1);
            assertNumMatches(inferenceModel,c,q,d,1);
            
            assertNumMatches(inferenceModel,null,null,null,6);
        }
        
        //make assertions about derived triples
        {
            ByteArrayOutputStream derivationStream = new ByteArrayOutputStream();
            Model deductions = inferenceModel.getDeductionsModel();
            
            PrintWriter out = 
                    new PrintWriter(derivationStream);
            
            StmtIterator statements = 
                    inferenceModel.listStatements();
            
            int deducedStatements = 0;
            
            while(statements.hasNext()){
                Statement statement = statements.nextStatement();
                out.println("Examining derivation of statement " + statement);
                
                Iterator<Derivation> derivations = inferenceModel.getDerivation(statement);
                
                boolean anyDerivations = false;
                boolean isDeductionStatement = deductions.contains(statement);
                
                while(derivations.hasNext()){
                    Derivation deriv = derivations.next();
                    deriv.printTrace(out, false);
                    anyDerivations = true;
                }
                
                if(isDeductionStatement){
                    Assert.assertTrue(
                        statement.toString() + " did not have any derivations",
                        anyDerivations
                        );
                    deducedStatements++;
                }
            }
            out.flush();
            
            Assert.assertTrue(
                "expected to find > 0 deductions", 
                deductions.size() > 0
                );
            
            Assert.assertEquals(
                "expected to find " + deductions.size() + 
                " deductions but found " + deducedStatements, 
                deductions.size(), 
                deducedStatements
                );
            
            System.out.println(derivationStream.toString());
        }
    }

}
