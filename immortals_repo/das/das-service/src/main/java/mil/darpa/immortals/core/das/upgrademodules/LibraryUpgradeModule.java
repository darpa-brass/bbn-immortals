package mil.darpa.immortals.core.das.upgrademodules;

import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildInstance;
import mil.darpa.immortals.core.das.knowledgebuilders.building.GradleKnowledgeBuilder;
import mil.darpa.immortals.core.das.sparql.deploymentmodel.DetermineLibraryUpgrades;
import mil.darpa.immortals.das.context.DasAdaptationContext;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Created by awellman@bbn.com on 4/12/18.
 */
public class LibraryUpgradeModule implements UpgradeModuleInterface {

    private DasAdaptationContext dac;
    
    private List<DetermineLibraryUpgrades.LibraryUpgrade> libraryUpgrades;

    public LibraryUpgradeModule() {
    }

    @Override
    public synchronized boolean isApplicable(@Nonnull DasAdaptationContext dac) {
        libraryUpgrades = DetermineLibraryUpgrades.select(dac);
        this.dac = dac;
        return !libraryUpgrades.isEmpty();
    }

    @Override
    public synchronized void apply(@Nonnull DasAdaptationContext dac) throws IOException {
        if (this.dac != dac) {
            isApplicable(dac);
        }

        for (DetermineLibraryUpgrades.LibraryUpgrade upgrade : libraryUpgrades) {
            AdaptationTargetBuildInstance buildInstance =
                    GradleKnowledgeBuilder.getBuildInstance(upgrade.adaptationTarget, dac.getAdaptationIdentifer());

            buildInstance.updateBuildScriptDependency(upgrade.originalResourceDependency, upgrade.replacementResourceDependency);
        }
    }
}
