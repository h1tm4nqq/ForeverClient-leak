package we.devs.forever.client.command.impl.chunks;


import we.devs.forever.api.manager.impl.client.ModuleManager;
import we.devs.forever.client.command.api.SyntaxChunk;
import we.devs.forever.client.modules.api.Module;

import java.util.Objects;

public class ModuleChunk extends SyntaxChunk {
    public ModuleChunk(String name) {
        super(name);
    }
    @Override
    public String predict(String currentArg) {
        for (Module module : ModuleManager.modules) {
            if (module.getName().toLowerCase().startsWith(currentArg.toLowerCase())) {
                return module.getName();
            }
//            for (String alias : module.getAliases()) {
//                if (alias.toLowerCase().startsWith(currentArg.toLowerCase())) {
//                    return alias;
//                }
//            }
        }
        if(Objects.equals(currentArg, "")) {
            return ModuleManager.modules.stream().findFirst().get().getName();
        }
        return currentArg;
    }
}
