package we.devs.forever.client.command.impl.commands;

import com.google.gson.JsonParser;
import we.devs.forever.api.manager.impl.client.ModuleManager;
import we.devs.forever.api.manager.impl.config.ConfigManager;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.command.api.SyntaxChunk;
import we.devs.forever.client.command.impl.chunks.ModuleChunk;
import we.devs.forever.client.command.impl.chunks.SettingChunk;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public
class ModuleCommand extends Command {

    public ModuleCommand() {
        super("module", new ModuleChunk("<module>"), new SyntaxChunk("<set/reset>") {
            @Override
            public String predict(String currentArg) {
                if(currentArg.toLowerCase().startsWith("s")) {
                    return "set";
                }
                if(currentArg.toLowerCase().startsWith("r")) {
                    return "reset";
                }
                return currentArg;
            }
        },new SettingChunk("<setting>"),new SyntaxChunk("<value>"));
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            sendMessage("Modules: ");
            for (Module.Category category : moduleManager.getCategories()) {
                StringBuilder modules = new StringBuilder(category.getName() + ": ");
                for (Module module : moduleManager.getModulesByCategory(category)) {
                    modules.append(module.isEnabled() ? TextUtil.GREEN : TextUtil.RED).append(module.getName()).append(TextUtil.RESET).append(", ");
                }
                sendMessage(modules.toString());
            }
            return;
        }

        Module module = moduleManager.getModuleByDisplayName(commands[0]);
        if (module == null) {
            module = moduleManager.getModuleByName(commands[0]);
            if (module == null) {
                sendMessage(TextUtil.RED + "This module doesnt exist.");
                return;
            }
            sendMessage(TextUtil.RED + " This is the original name of the module. Its current name is: " + module.getName());
            return;
        }

        if (commands.length == 2) {
            sendMessage(module.getName() + " : " + module.getDescription());
            for (Setting setting : ModuleManager.getSettings(module)) {
                sendMessage(setting.getName() + " : " + setting.getValue() + ", " + setting.getDescription());
            }
            return;
        }

        if (commands.length == 3) {
            if (commands[1].equalsIgnoreCase("set")) {
                sendMessage(TextUtil.RED + "Please specify a setting.");
            } else if (commands[1].equalsIgnoreCase("reset")) {
                for (Setting setting : ModuleManager.getSettings(module)) {
                    setting.setValue(setting.getDefaultValue());
                }
            } else {
                sendMessage(TextUtil.RED + "This command doesnt exist.");
            }
            return;
        }

        if (commands.length == 4) {
            sendMessage(TextUtil.RED + "Please specify a value.");
            return;
        }

        if (commands.length == 5) {
            Setting setting = ModuleManager.getSettingByName(module,commands[2]);
            if (setting != null) {
                JsonParser jp = new JsonParser();
                if (setting.getType().equalsIgnoreCase("String")) {
                    setting.setValue(commands[3]);
                    sendMessage(TextUtil.GREEN + module.getName() + " " + setting.getName() + " has been set to " + commands[3] + ".");
                    return;
                }
                try {
                    if (setting.getName().equalsIgnoreCase("Enabled")) {
                        if (commands[3].equalsIgnoreCase("true")) {
                            module.enable();
                        }
                        if (commands[3].equalsIgnoreCase("false")) {
                            module.disable();
                        }
                    }
                    ConfigManager.setValueFromJson(module, setting, jp.parse(commands[3]));
                } catch (Exception e) {
                    sendMessage(TextUtil.RED + "Bad Value! This setting requires a: " + setting.getType() + " value.");
                    return;
                }
                sendMessage(TextUtil.GREEN + module.getName() + " " + setting.getName() + " has been set to " + commands[3] + ".");
            }
        }
    }

}
