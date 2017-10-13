package mil.darpa.immortals.core;


import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 12/21/15.
 */
@Deprecated
public class AbstractOutputProvider<OUTPUT> implements OutputProviderInterface<OUTPUT> {

    private LinkedList<InputProviderInterface<OUTPUT>> handlers = new LinkedList<>();

    @Override
    public void addSuccessor(InputProviderInterface<OUTPUT> handler) {
        synchronized (handlers) {
            handlers.add(handler);
        }
    }

    @Override
    public void removeSuccessor(InputProviderInterface<OUTPUT> handler) {
        synchronized (handlers) {
            handlers.remove(handler);
        }
    }

    protected void distributeResult(OUTPUT data) {
        synchronized (handlers) {
            for (InputProviderInterface<OUTPUT> handler : handlers) {
                handler.handleData(data);
            }
        }
    }
}
