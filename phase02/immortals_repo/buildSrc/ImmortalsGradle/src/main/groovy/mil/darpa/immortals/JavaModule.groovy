package mil.darpa.immortals

import mil.darpa.immortals.internal.JavaModuleImpl

/**
 * Created by awellman@bbn.com on 11/1/17.
 */
class JavaModule extends JavaModuleImpl {

    @Override
    boolean isShadow() {
        return false
    }
    
}
