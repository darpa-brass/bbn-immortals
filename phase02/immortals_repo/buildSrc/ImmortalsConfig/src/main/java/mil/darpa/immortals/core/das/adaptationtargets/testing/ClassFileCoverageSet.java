package mil.darpa.immortals.core.das.adaptationtargets.testing;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * Created by awellman@bbn.com on 4/16/18.
 */
public class ClassFileCoverageSet extends CopyOnWriteArraySet<ClassFileCoverage> {


    private String padString(int length, @Nonnull String s) {
        return String.format("%1$-" + length + "s", s);
    }

    public String toChart(@Nonnull List<String>... columns) {
        StringBuilder sb = new StringBuilder();

        int[] columnWidths = new int[columns.length];

        for (int row = 0; row < columns[0].size(); row++) {
            for (int column = 0; column < columns.length; column++) {
                columnWidths[column] = Math.max(columnWidths[column], columns[column].get(row).length());
            }
        }

        for (int row = 0; row < columns[0].size(); row++) {
            for (int column = 0; column < columns.length; column++) {
                sb.append(" | ").append(padString(columnWidths[column], columns[column].get(row)));
            }
            sb.append(" |\n");
        }

        return sb.toString();
    }

    public ClassFileCoverageSet() {
        super();
    }

    public ClassFileCoverageSet(Collection<ClassFileCoverage> collection) {
        super(collection);
    }


    public ClassFileCoverageSet getPartiallyOrFullyCovered() {
        ClassFileCoverageSet cfcs = new ClassFileCoverageSet();
        
        for (ClassFileCoverage cfc : this) {
            if (cfc.getLinesCovered() > 0) {
                cfcs.add(cfc);
            }
        }
        return cfcs;
    }
}
