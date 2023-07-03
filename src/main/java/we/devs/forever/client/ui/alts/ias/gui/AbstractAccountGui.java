package we.devs.forever.client.ui.alts.ias.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import we.devs.forever.client.ui.alts.iasencrypt.EncryptionTools;
import we.devs.forever.client.ui.alts.tools.alt.AccountData;
import we.devs.forever.client.ui.alts.tools.alt.AltDatabase;

import java.io.IOException;

public abstract class AbstractAccountGui
        extends GuiScreen {
    private final String actionString;
    protected boolean hasUserChanged;
    private GuiTextField username;
    private GuiTextField password;
    private GuiButton complete;

    public AbstractAccountGui(String actionString) {
        this.actionString = actionString;
    }

    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.complete = new GuiButton(2, this.width / 2 - 152, this.height - 28, 150, 20, I18n.format(this.actionString, new Object[0]));
        this.buttonList.add(this.complete);
        this.buttonList.add(new GuiButton(3, this.width / 2 + 2, this.height - 28, 150, 20, I18n.format("Cancel", new Object[0])));
        this.username = new GuiTextField(0, this.fontRenderer, this.width / 2 - 100, 60, 200, 20);
        this.username.setFocused(true);
        this.username.setMaxStringLength(64);
        this.password = new GuiPasswordField(1, this.fontRenderer, this.width / 2 - 100, 90, 200, 20);
        this.password.setMaxStringLength(64);
        this.complete.enabled = false;
    }

    public void drawScreen(int par1, int par2, float par3) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, I18n.format(this.actionString, new Object[0]), this.width / 2, 7, -1);
        this.drawCenteredString(this.fontRenderer, I18n.format("Username", new Object[0]), this.width / 2 - 130, 66, -1);
        this.drawCenteredString(this.fontRenderer, I18n.format("Password", new Object[0]), this.width / 2 - 130, 96, -1);
        this.username.drawTextBox();
        this.password.drawTextBox();
        super.drawScreen(par1, par2, par3);
    }

    protected void keyTyped(char character, int keyIndex) {
        if (keyIndex == 1) {
            this.escape();
        } else if (keyIndex == 28) {
            if (this.username.isFocused()) {
                this.username.setFocused(false);
                this.password.setFocused(true);
            } else if (this.password.isFocused() && this.complete.enabled) {
                this.complete();
                this.escape();
            }
        } else if (keyIndex == 15) {
            this.username.setFocused(!this.username.isFocused());
            this.password.setFocused(!this.password.isFocused());
        } else {
            this.username.textboxKeyTyped(character, keyIndex);
            this.password.textboxKeyTyped(character, keyIndex);
            if (this.username.isFocused()) {
                this.hasUserChanged = true;
            }
        }
    }

    public void updateScreen() {
        this.username.updateCursorCounter();
        this.password.updateCursorCounter();
        this.complete.enabled = this.canComplete();
    }

    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == 2) {
                this.complete();
                this.escape();
            } else if (button.id == 3) {
                this.escape();
            }
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.username.mouseClicked(mouseX, mouseY, mouseButton);
        this.password.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private void escape() {
        this.mc.displayGuiScreen(new GuiAccountSelector());
    }

    public String getUsername() {
        return this.username.getText();
    }

    public void setUsername(String username) {
        this.username.setText(username);
    }

    public String getPassword() {
        return this.password.getText();
    }

    public void setPassword(String password) {
        this.password.setText(password);
    }

    protected boolean accountNotInList() {
        for (AccountData data : AltDatabase.getInstance().getAlts()) {
            if (!EncryptionTools.decode(data.user).equals(this.getUsername())) continue;
            return false;
        }
        return true;
    }

    public boolean canComplete() {
        return this.getUsername().length() > 0 && this.accountNotInList();
    }

    public abstract void complete();
}

