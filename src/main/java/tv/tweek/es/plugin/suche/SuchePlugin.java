package tv.tweek.es.plugin.suche;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.AbstractPlugin;

import java.util.Collection;

/**
 * Created by hasan on 4/16/14.
 */
public class SuchePlugin extends AbstractPlugin {
  @Override
  public String name() {
    return "es-suche";
  }

  @Override
  public String description() {
    return "An example plugin implementation for ES which performs only search.";
  }

  @Override
  public Collection<Class<? extends Module>> modules() {
    Collection<Class<? extends Module>> modules = Lists.newArrayList();
    modules.add(SucheRestModule.class);
    return modules;
  }
}
