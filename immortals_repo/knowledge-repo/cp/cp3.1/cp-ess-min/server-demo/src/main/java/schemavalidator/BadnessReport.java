package schemavalidator;

import java.util.ArrayList;
import java.util.List;

public class BadnessReport{
    final List<BadnessReportElement> elements = new ArrayList<>();
    private final double normalizedBadnessScore;
    private final double weightedBadnessScore;
    
    public BadnessReport(double normalizedBadnessScore, double weightedBadnessScore) {
        super();
        this.normalizedBadnessScore = normalizedBadnessScore;
        this.weightedBadnessScore = weightedBadnessScore;
    }

    
    public double getBadnessScore() {
        return normalizedBadnessScore;
    }
    
    @Override
    public String toString(){
        if(elements.size() == 0){
            return String.format("no issues found, score is %1.4f",normalizedBadnessScore);
        }
        
        StringBuilder sb = new StringBuilder();
        
        sb.append(String.format("%d errors/fatals found in document:\n", elements.size()));
        for(BadnessReportElement e:elements){
            sb.append(String.format("\t%s\n", e.summary));
        }
        
        sb.append(String.format("document's [normalized, weighted] badness scores are [%1.4f, %1.4f]\n", normalizedBadnessScore, weightedBadnessScore));
        
        return sb.toString();
    }


    
    public double getWeightedBadnessScore() {
        return weightedBadnessScore;
    }
    
}