package we.devs.forever.client.command.impl.chunks;

import we.devs.forever.api.manager.impl.client.ModuleManager;
import we.devs.forever.client.command.api.SyntaxChunk;
import we.devs.forever.client.setting.Setting;

import java.util.ArrayList;

public class SettingChunk extends SyntaxChunk {
    public SettingChunk(String name) {
        super(name);
    }

    @Override
    public String predict(String currentArg) {
        ModuleChunk chunk = getGroup().findChunk(ModuleChunk.class);
        ArrayList<Setting<?>> settings = new ArrayList<>();
        if (chunk != null) {
            settings = ModuleManager.getSettings(ModuleManager.moduleManager.getModuleByName(chunk.getValue()));
        } else {
            ModuleManager.moduleSettings.values().parallelStream().forEach(settings::addAll);
        }

        for (Setting<?> setting : settings) {
            if (setting.getName().toLowerCase().startsWith(currentArg.toLowerCase())) {
                return setting.getName();
            }
        }
//            for (String alias : module.getAliases()) {
//                if (alias.toLowerCase().startsWith(currentArg.toLowerCase())) {
//                    return alias;
//                }
//            }

        return currentArg;
    }
}
