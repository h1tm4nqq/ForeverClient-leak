package we.devs.forever.client.ui.newGui.api;

public abstract class AbstractButton {
    private final String name;

    public AbstractButton(String name) {

        this.name = name;
    }

    public abstract void drawScreen(int mouseX, int mouseY, float partialTicks);

    public abstract void mouseClicked(int mouseX, int mouseY, int mouseButton);

    public abstract void mouseReleased(int mouseX, int mouseY, int state);
    
    public String getName() {
        return name;
    }
}
