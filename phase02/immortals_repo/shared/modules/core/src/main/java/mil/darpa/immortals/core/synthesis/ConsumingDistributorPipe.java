package mil.darpa.immortals.core.synthesis;

import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import javax.annotation.Nullable;

/**
 * Created by awellman@bbn.com on 8/7/17.
 */
public class ConsumingDistributorPipe<T> extends AbstractMultisuccessorConsumingPipe<T, T> {
    public ConsumingDistributorPipe(boolean distributeNull, @Nullable ConsumingPipe<T>... next) {
        super(distributeNull, next);
    }

    @Override
    public T process(T input) {
        return null;
    }
}
