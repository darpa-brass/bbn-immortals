package ASTManipulation;

import com.github.javaparser.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 2/19/18.
 */
public class Diff
{
    List<Statement> originalStmts;
    List<Statement> reducedStmts;
    List<Statement> diffStmts;
    public Diff(){
        diffStmts = new ArrayList<>();
    }
    public List<Statement> calculate(List<Statement> originalStmts, List<Statement> reducedStmts){
        
        if(originalStmts.size() == reducedStmts.size()){
            // do nothing;
            return diffStmts;
        }
        else{
            int originalMarker = 0;
            int reducedMarker = 0;
            for(originalMarker = 0;originalMarker<originalStmts.size();originalMarker++){
                if(reducedMarker >=  reducedStmts.size()){
                    diffStmts.add(originalStmts.get(originalMarker).clone());
                }
                else{
                    if(StatementComparor.compare(originalStmts.get(originalMarker),reducedStmts.get(reducedMarker)))
                        reducedMarker++;
                    else{
                        diffStmts.add(originalStmts.get(originalMarker).clone());
                    }
                }


            }


        }
        return  diffStmts;
        
    }
    
    
    
}
