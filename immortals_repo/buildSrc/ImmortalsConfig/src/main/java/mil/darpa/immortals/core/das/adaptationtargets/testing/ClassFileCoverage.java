package mil.darpa.immortals.core.das.adaptationtargets.testing;

import javax.annotation.Nonnull;
import java.util.Comparator;

/**
 * Created by awellman@bbn.com on 4/10/18.
 */
public class ClassFileCoverage implements Comparator<ClassFileCoverage>, Comparable<ClassFileCoverage> {
    private final String identifier;
    private final int instructionsCovered;
    private final int instructionsMissed;
    private final int branchesCovered;
    private final int branchesMissed;
    private final int linesCovered;
    private final int linesMissed;
    private final int complexityCovered;
    private final int complexityMissed;
    private final int methodsCovered;
    private final int methodsMissed;


    public ClassFileCoverage(@Nonnull String identifier, int instructionsCovered, int instructionsMissed,
                             int branchesCovered, int branchesMissed, int linesCovered, int linesMissed,
                             int complexityCovered, int complexityMissed, int methodsCovered, int methodsMissed) {
        this.identifier = identifier;
        this.instructionsCovered = instructionsCovered;
        this.instructionsMissed = instructionsMissed;
        this.branchesCovered = branchesCovered;
        this.branchesMissed = branchesMissed;
        this.linesCovered = linesCovered;
        this.linesMissed = linesMissed;
        this.complexityCovered = complexityCovered;
        this.complexityMissed = complexityMissed;
        this.methodsCovered = methodsCovered;
        this.methodsMissed = methodsMissed;
    }

    public double getInstructionCoverage() {
        if (instructionsCovered == 0 && instructionsMissed == 0) {
            return 0.0;
        }
        return (double) instructionsCovered / ((double) instructionsCovered + (double) instructionsMissed);
    }

    public double getBranchCoverage() {
        if (branchesCovered == 0 && branchesMissed == 0) {
            return 0.0;
        }
        return (double) branchesCovered / ((double) branchesCovered + (double) branchesMissed);
    }

    public double getLineCoverage() {
        if (linesCovered == 0 && linesMissed == 0) {
            return 0.0;
        }
        return (double) linesCovered / ((double) linesCovered + (double) linesMissed);
    }

    public double getComplexityCoverage() {
        if (complexityCovered == 0 && complexityMissed == 0) {
            return 0.0;
        }
        return (double) complexityCovered / ((double) complexityCovered + (double) complexityMissed);
    }

    public double getMethodCoverage() {
        if (methodsCovered == 0 && methodsMissed == 0) {
            return 0.0;
        }
        return (double) methodsCovered / ((double) methodsCovered + (double) methodsMissed);
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getInstructionsCovered() {
        return instructionsCovered;
    }

    public int getInstructionsMissed() {
        return instructionsMissed;
    }

    public int getBranchesCovered() {
        return branchesCovered;
    }

    public int getBranchesMissed() {
        return branchesMissed;
    }

    public int getLinesCovered() {
        return linesCovered;
    }

    public int getLinesMissed() {
        return linesMissed;
    }

    public int getComplexityCovered() {
        return complexityCovered;
    }

    public int getComplexityMissed() {
        return complexityMissed;
    }

    public int getMethodsCovered() {
        return methodsCovered;
    }

    public int getMethodsMissed() {
        return methodsMissed;
    }

    @Override
    public int compare(ClassFileCoverage o1, ClassFileCoverage o2) {
        return o1.compareTo(o2);
    }

    @Override
    public int compareTo(ClassFileCoverage o) {
        if (o == null) {
            return -1;

        } else if (this.getLineCoverage() < o.getLineCoverage()) {
            return -1;


        } else if (this.getLineCoverage() > o.getLineCoverage()) {
            return 1;
        } else {
            return 0;
        }
    }
}
