package alexanderc.tweek.es.plugin.kas;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.AbstractPlugin;

import java.util.Collection;

public class KeyAwareSearchPlugin extends AbstractPlugin {
    @Override
    public String name() {
        return "es-kas";
    }

    @Override
    public String description() {
        return "Perform key aware searches in the available indexes.";
    }

    @Override
    public Collection<Class<? extends Module>> modules() {
        Collection<Class<? extends Module>> modules = Lists.newArrayList();
        modules.add(KeyAwareSearchRestModule.class);
        return modules;
    }
}
