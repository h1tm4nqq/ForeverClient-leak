package we.devs.forever.client.command.impl.commands;

import we.devs.forever.client.command.api.Command;
import we.devs.forever.main.ForeverClient;

public
class UnloadCommand extends Command {

    public UnloadCommand() {
        super("unload");
    }

    @Override
    public void execute(String[] commands) {
      ForeverClient.managers.unload(true);
    }
}
