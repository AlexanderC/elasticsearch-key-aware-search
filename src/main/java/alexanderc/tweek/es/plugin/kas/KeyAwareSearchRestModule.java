package alexanderc.tweek.es.plugin.kas;

import org.elasticsearch.common.inject.AbstractModule;

public class KeyAwareSearchRestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(KeyAwareSearchRestHandler.class).asEagerSingleton();
    }
}
