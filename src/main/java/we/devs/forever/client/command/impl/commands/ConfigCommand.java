package we.devs.forever.client.command.impl.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import we.devs.forever.api.manager.impl.config.ConfigManager;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.command.api.SyntaxChunk;
import we.devs.forever.client.command.impl.chunks.ConfigChunk;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public
class ConfigCommand extends Command {

    public ConfigCommand() {
        super("config",new SyntaxChunk("<save/load/list>") {
            @Override
            public String predict(String currentArg) {
                if (currentArg.toLowerCase().startsWith("s")) {
                    return "save";
                } else if (currentArg.toLowerCase().startsWith("l")) {
                    return "load";
                } else if (currentArg.toLowerCase().startsWith("li")) {
                    return "list";
                }
                return currentArg;
            }
        }, new ConfigChunk("<name>"));
    }


    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            sendMessage(ConfigManager.currentConfig.getName().replace(".json",""));
            return;
        }

        if (commands.length == 2) {
            if ("list".equals(commands[0])) {
                StringBuilder builder = new StringBuilder();
                Objects.requireNonNull(ConfigManager.getConfigList()).forEach(config -> {
                    String temp = config.getName().replace(".json","");
                    builder.append("\n");
                    if(temp.equalsIgnoreCase(ConfigManager.currentConfig.getName().replace(".json",""))) {
                        builder.append(ChatFormatting.GREEN);
                    } else {
                        builder.append(ChatFormatting.GRAY);
                    }
                    builder.append(temp);
                });
                sendMessage(builder.toString());
            } else {
                sendMessage(TextUtil.RED + "Not a valid command... Possible usage: <list>");
            }
        }

        if (commands.length >= 3) {
            switch (commands[0].toLowerCase()) {
                case "save": {
                    File cfg = new File(ConfigManager.CONFIGS,commands[1] + ".json");
                    if(!cfg.exists()) {
                        try {
                            Files.copy(ConfigManager.currentConfig.toPath(),cfg.toPath());
                        } catch (IOException ignored) {
                            sendMessage(TextUtil.RED + "Can't create config file");
                        }
                    }
                    ConfigManager.save(cfg);
                    sendMessage(TextUtil.GREEN + "Config has been saved.");
                }
                    break;
                case "set":
                case "load": {
                    File cfg = new File(ConfigManager.CONFIGS,commands[1] + ".json");
                    File old = ConfigManager.currentConfig;
                    if(!cfg.exists()) {
                        try {
                            Files.copy(ConfigManager.currentConfig.toPath(),cfg.toPath());
                        } catch (IOException ignored) {
                            sendMessage(TextUtil.RED + "Can't create config file");
                        }
                    }
                    ConfigManager.save(old);
                    ConfigManager.load(cfg);
                    sendMessage(TextUtil.GREEN + "Config has been loaded.");
                    break;
                }
                default:
                    sendMessage(TextUtil.RED + "Not a valid command... Possible usage: <save/load>");
                    break;
            }
        }
    }
}
