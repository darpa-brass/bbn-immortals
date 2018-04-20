package mil.darpa.immortals.core.das.upgrademodules;

import mil.darpa.immortals.das.context.DasAdaptationContext;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 4/16/18.
 */
public interface UpgradeModuleInterface {

    boolean isApplicable(@Nonnull DasAdaptationContext dac) throws Exception;

    void apply(@Nonnull DasAdaptationContext dac) throws Exception;
}
