package we.devs.forever.client.command.impl.commands;

import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.command.api.SyntaxChunk;
import we.devs.forever.client.modules.impl.client.ClickGui;

public
class PrefixCommand extends Command {

    public PrefixCommand() {
        super("prefix", new SyntaxChunk("<prefix>"));
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            Command.sendMessage(TextUtil.RED + "Specify a new prefix.");
            return;
        }

        (moduleManager.getModuleByClass(ClickGui.class)).prefix.setValue(commands[0]);
        commandManager.setPrefix(commands[0]);
        Command.sendMessage("Prefix set to " + TextUtil.GREEN + commandManager.getPrefix());
    }
}
