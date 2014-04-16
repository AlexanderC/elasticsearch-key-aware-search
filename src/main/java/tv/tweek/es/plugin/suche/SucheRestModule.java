package tv.tweek.es.plugin.suche;

import org.elasticsearch.common.inject.AbstractModule;

/**
 * Created by hasan on 4/16/14.
 */
public class SucheRestModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(SucheRestHandler.class).asEagerSingleton();
  }
}
