package we.devs.forever.client.command.impl.commands;

import we.devs.forever.client.command.api.Command;
import we.devs.forever.main.ForeverClient;

public
class ReloadCommand extends Command {

    public ReloadCommand() {
        super("reload");
    }

    @Override
    public void execute(String[] commands) {
        ForeverClient.reload();
    }
}
