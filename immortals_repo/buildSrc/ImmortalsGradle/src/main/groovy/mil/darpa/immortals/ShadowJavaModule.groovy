package mil.darpa.immortals

import mil.darpa.immortals.internal.JavaModuleImpl

/**
 * Created by awellman@bbn.com on 11/1/17.
 */
class ShadowJavaModule extends JavaModuleImpl {

    @Override
    boolean isShadow() {
        return true
    }
}
