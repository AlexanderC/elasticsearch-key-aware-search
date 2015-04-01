package alexanderc.es.plugin.kas;

import org.elasticsearch.common.inject.AbstractModule;

/**
 * Created by AlexanderC on 10/28/14.
 */
public class KeyAwareSearchRestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(KeyAwareSearchRestHandler.class).asEagerSingleton();
    }
}
