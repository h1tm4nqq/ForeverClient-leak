package we.devs.forever.client.command.impl.commands;

import org.lwjgl.input.Keyboard;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.command.impl.chunks.KeyChunk;
import we.devs.forever.client.command.impl.chunks.ModuleChunk;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Bind;

public
class BindCommand extends Command {

    public BindCommand() {
        super("bind", new ModuleChunk("<module>"), new KeyChunk( "<bind>"));
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            sendMessage("Please specify a module.");
            return;
        }

        String rkey = commands[1];
        String moduleName = commands[0];

        Module module = moduleManager.getModuleByName(moduleName);

        if (module == null) {
            sendMessage("Unknown module '" + module + "'!");
            return;
        }

        if (rkey == null) {
            sendMessage(module.getName() + " is bound to &b" + module.getBind().toString());
            return;
        }

        int key = Keyboard.getKeyIndex(rkey.toUpperCase());

        if (rkey.equalsIgnoreCase("none")) {
            key = -1;
        }

        if (key == 0) {
            sendMessage("Unknown key '" + rkey + "'!");
            return;
        }

        module.bind.setValue(new Bind(key));
        sendMessage("Bind for &b" + module.getName() + "&r set to &b" + rkey.toUpperCase());
    }
}
