package PostProcessing;

import ASTManipulation.ExtendedStatement;
import com.github.javaparser.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by root on 1/8/17.
 */
public class StatInfo {

    public int numberOfNodesReduced;
    public int highestlevelFromLeafNode;
    List<Statement> statementList;
    List<ExtendedStatement> extendedStatementList;
    Set<String> statementListIDS;
    public StatInfo(){
        numberOfNodesReduced = 0;
        highestlevelFromLeafNode = 0;
        statementListIDS = new HashSet<>();
        statementList = new ArrayList<Statement>();

    }

}
