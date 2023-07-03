package we.devs.forever.client.command.impl.commands;

import we.devs.forever.client.command.api.Command;

public
class HelpCommand extends Command {

    public HelpCommand() {
        super("help");
    }

    @Override
    public void execute(String[] commands) {
        sendMessage("You can use following commands: ");
        for (Command command : commandManager.getCommands()) {
            sendMessage(commandManager.getPrefix() + command.getName());
        }
    }
}
