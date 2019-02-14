package FaultLocalization;

import Helper.Debugger;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by root on 2/10/18.
 * Calculate rank of each javaparser.ast.stmt statement based on
 * Tarantula and other formulae.
 */
public class Rank {
    Dictionary<String,Set<String>> testCoverage;
    Hashtable<String,Set<String>> hashCoverage;
    Dictionary<String,Boolean> testPassFail;
    Hashtable<String,Boolean> hashPassFail;
    int totalFailed;
    int totalPassed;
    Set<String> allStatements;
    Set<String> failed;
    Set<String> passed;
    Dictionary<String,Float> tarantulaRank;
    List<String> finalRanking;






    public Rank(Dictionary<String,Set<String>> testCoverage, Dictionary<String,Boolean> testPassFail){
        this.testCoverage = testCoverage;
        this.testPassFail = testPassFail;
        allStatements = new HashSet<>();
        failed = new HashSet<>();
        passed = new HashSet<>();
        tarantulaRank = new Hashtable<>();
        finalRanking = new ArrayList<>();

        hashCoverage = (Hashtable<String,Set<String>>)testCoverage;

        for (String key1:hashCoverage.keySet()
             ) {
            allStatements.addAll(hashCoverage.get(key1));

        }

        hashPassFail = (Hashtable<String,Boolean>)testPassFail;

        for(String key1: hashPassFail.keySet()){
            if(hashPassFail.get(key1))
                passed.add(key1);
            else
                failed.add(key1);

        }

        totalFailed = failed.size();
        totalPassed = passed.size();
    }

    public void calculateTarantula(){
        for (String stmt: allStatements
             ) {
            int failedOfStmt = 0;
            int passedOfStmt = 0;

            try{
                for(String test : failed){
                    Set<String> statementCoveredByTest = testCoverage.get(test);
                    if(statementCoveredByTest.contains(stmt))
                        failedOfStmt++;
                }

                for(String test : passed){
                    Set<String> statementCoveredByTest = testCoverage.get(test);
                    if(statementCoveredByTest.contains(stmt))
                        passedOfStmt++;
                }

            }
            catch (NullPointerException nex){
                nex.printStackTrace();
            }


            float failedPart = (float)failedOfStmt / totalFailed;
            float passedPart = (float)passedOfStmt / totalPassed;

            float rank = failedPart / (failedPart + passedPart);

            tarantulaRank.put(stmt,Float.valueOf(rank));

        }
    }

    public void sortByRanking(){
        Hashtable<String,Float> tarantulaRankCopy = (Hashtable<String, Float>) ((Hashtable<String,Float>)tarantulaRank).clone();
        Collection<Float> fList = tarantulaRankCopy.values().stream().sorted().collect(Collectors.toSet());
        List<String> finalList = new ArrayList<String>();
        fList = fList.stream().sorted(new Comparator<Float>() {
            @Override
            public int compare(Float o1, Float o2) {
                if (o1.floatValue() < o2.floatValue())
                    return 1;
                else
                    return -1;
            }
        }).collect(Collectors.toList());

        for (Float f:fList
             ) {
            Debugger.log(f.floatValue());
            Set<Map.Entry<String,Float>> list =  tarantulaRankCopy.entrySet().stream().filter(y -> y.getValue().floatValue() == f.floatValue()).collect(Collectors.toSet());
            Debugger.log(list);
            ArrayList<String> intermediateList = new ArrayList<>();
            for (Map.Entry<String,Float> item: list
                 ) {
                intermediateList.add(item.getKey());

            }
            Collections.shuffle(intermediateList);
            finalRanking.addAll(intermediateList);



        }


    }




}
