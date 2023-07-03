package we.devs.forever.client.command.api;

import we.devs.forever.client.Client;

public class SyntaxChunk extends Client {
    private final String name;
    private String value;
    private SyntaxGroup group;
    private Command command;

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public SyntaxChunk(String name) {
        this.name = name;
    }

    public String predict(String currentArg) {
        return currentArg;
    }

    public String getName() {
        return name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public final SyntaxGroup getGroup() {
        return group;
    }

    public final void setGroup(SyntaxGroup group) {
        this.group = group;
    }

    public String getValue() {
        return value;
    }
}
