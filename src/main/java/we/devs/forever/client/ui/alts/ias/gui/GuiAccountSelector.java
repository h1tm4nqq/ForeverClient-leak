package we.devs.forever.client.ui.alts.ias.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import we.devs.forever.client.ui.alts.ias.account.AlreadyLoggedInException;
import we.devs.forever.client.ui.alts.ias.account.ExtendedAccountData;
import we.devs.forever.client.ui.alts.ias.config.ConfigValues;
import we.devs.forever.client.ui.alts.ias.enums.EnumBool;
import we.devs.forever.client.ui.alts.ias.tools.HttpTools;
import we.devs.forever.client.ui.alts.ias.tools.JavaTools;
import we.devs.forever.client.ui.alts.ias.tools.SkinTools;
import we.devs.forever.client.ui.alts.iasencrypt.EncryptionTools;
import we.devs.forever.client.ui.alts.tools.Config;
import we.devs.forever.client.ui.alts.tools.Tools;
import we.devs.forever.client.ui.alts.tools.alt.AccountData;
import we.devs.forever.client.ui.alts.tools.alt.AltDatabase;
import we.devs.forever.client.ui.alts.tools.alt.AltManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class GuiAccountSelector
        extends GuiScreen {
    private int selectedAccountIndex;
    private int prevIndex;
    private Throwable loginfailed;
    private ArrayList<ExtendedAccountData> queriedaccounts = this.convertData();
    private List accountsgui;
    private GuiButton login;
    private GuiButton loginoffline;
    private GuiButton delete;
    private GuiButton edit;
    private GuiButton reloadskins;
    private String query;
    private GuiTextField search;

    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.accountsgui = new List(this.mc);
        this.accountsgui.registerScrollButtons(5, 6);
        this.query = I18n.format("Search", new Object[0]);
        this.buttonList.clear();
        this.reloadskins = new GuiButton(8, this.width / 2 - 154 - 10, this.height - 76 - 8, 120, 20, I18n.format("Reload Skins", new Object[0]));
        this.buttonList.add(this.reloadskins);
        this.buttonList.add(new GuiButton(0, this.width / 2 + 4 + 40, this.height - 52, 120, 20, I18n.format("Add Account", new Object[0])));
        this.login = new GuiButton(1, this.width / 2 - 154 - 10, this.height - 52, 120, 20, I18n.format("Login", new Object[0]));
        this.buttonList.add(this.login);
        this.edit = new GuiButton(7, this.width / 2 - 40, this.height - 52, 80, 20, I18n.format("Edit", new Object[0]));
        this.buttonList.add(this.edit);
        this.loginoffline = new GuiButton(2, this.width / 2 - 154 - 10, this.height - 28, 110, 20, I18n.format("Login", new Object[0]) + " " + I18n.format("Offline", new Object[0]));
        this.buttonList.add(this.loginoffline);
        this.buttonList.add(new GuiButton(3, this.width / 2 + 4 + 50, this.height - 28, 110, 20, I18n.format("Cancel", new Object[0])));
        this.delete = new GuiButton(4, this.width / 2 - 50, this.height - 28, 100, 20, I18n.format("Delete", new Object[0]));
        this.buttonList.add(this.delete);
        this.search = new GuiTextField(8, this.fontRenderer, this.width / 2 - 80, 14, 160, 16);
        this.search.setText(this.query);
        this.updateButtons();
        if (!this.queriedaccounts.isEmpty()) {
            SkinTools.buildSkin(this.queriedaccounts.get(this.selectedAccountIndex).alias);
        }
    }

    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.accountsgui.handleMouseInput();
    }

    public void updateScreen() {
        this.search.updateCursorCounter();
        this.updateText();
        this.updateButtons();
        if (this.prevIndex != this.selectedAccountIndex) {
            this.updateShownSkin();
            this.prevIndex = this.selectedAccountIndex;
        }
    }

    private void updateShownSkin() {
        if (!this.queriedaccounts.isEmpty()) {
            SkinTools.buildSkin(this.queriedaccounts.get(this.selectedAccountIndex).alias);
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        boolean flag = this.search.isFocused();
        this.search.mouseClicked(mouseX, mouseY, mouseButton);
        if (!flag && this.search.isFocused()) {
            this.query = "";
            this.updateText();
            this.updateQueried();
        }
    }

    private void updateText() {
        this.search.setText(this.query);
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        Config.save();
    }

    public void drawScreen(int par1, int par2, float par3) {
        this.accountsgui.drawScreen(par1, par2, par3);
        this.drawCenteredString(this.fontRenderer, I18n.format("Select Account", new Object[0]), this.width / 2, 4, -1);
        if (this.loginfailed != null) {
            this.drawCenteredString(this.fontRenderer, this.loginfailed.getLocalizedMessage(), this.width / 2, this.height - 62, 0xFF6464);
        }
        this.search.drawTextBox();
        super.drawScreen(par1, par2, par3);
        if (!this.queriedaccounts.isEmpty()) {
            SkinTools.javDrawSkin(8, this.height / 2 - 64 - 16, 64, 128);
            Tools.drawBorderedRect(this.width - 8 - 64, this.height / 2 - 64 - 16, this.width - 8, this.height / 2 + 64 - 16, 2, -5855578, -13421773);
            if (this.queriedaccounts.get(this.selectedAccountIndex).premium == EnumBool.TRUE) {
                this.drawString(this.fontRenderer, I18n.format("Premium", new Object[0]), this.width - 8 - 61, this.height / 2 - 64 - 13, 0x64FF64);
            } else if (this.queriedaccounts.get(this.selectedAccountIndex).premium == EnumBool.FALSE) {
                this.drawString(this.fontRenderer, I18n.format("Not Premium", new Object[0]), this.width - 8 - 61, this.height / 2 - 64 - 13, 0xFF6464);
            }
            this.drawString(this.fontRenderer, I18n.format("Times Used", new Object[0]), this.width - 8 - 61, this.height / 2 - 64 - 15 + 12, -1);
            this.drawString(this.fontRenderer, String.valueOf(this.queriedaccounts.get(this.selectedAccountIndex).useCount), this.width - 8 - 61, this.height / 2 - 64 - 15 + 21, -1);
            if (this.queriedaccounts.get(this.selectedAccountIndex).useCount > 0) {
                this.drawString(this.fontRenderer, I18n.format("Last Used", new Object[0]), this.width - 8 - 61, this.height / 2 - 64 - 15 + 30, -1);
                this.drawString(this.fontRenderer, JavaTools.getJavaCompat().getFormattedDate(), this.width - 8 - 61, this.height / 2 - 64 - 15 + 39, -1);
            }
        }
    }

    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == 3) {
                this.escape();
            } else if (button.id == 0) {
                this.add();
            } else if (button.id == 4) {
                this.delete();
            } else if (button.id == 1) {
                this.login(this.selectedAccountIndex);
            } else if (button.id == 2) {
                this.logino(this.selectedAccountIndex);
            } else if (button.id == 7) {
                this.edit();
            } else if (button.id == 8) {
                this.reloadSkins();
            } else {
                this.accountsgui.actionPerformed(button);
            }
        }
    }

    private void reloadSkins() {
        Config.save();
        SkinTools.cacheSkins();
        this.updateShownSkin();
    }

    private void escape() {
        this.mc.displayGuiScreen(null);
    }

    private void delete() {
        AltDatabase.getInstance().getAlts().remove(this.getCurrentAsEditable());
        if (this.selectedAccountIndex > 0) {
            --this.selectedAccountIndex;
        }
        this.updateQueried();
        this.updateButtons();
    }

    private void add() {
        this.mc.displayGuiScreen(new GuiAddAccount());
    }

    private void logino(int selected) {
        ExtendedAccountData data = this.queriedaccounts.get(selected);
        AltManager.getInstance().setUserOffline(data.alias);
        this.loginfailed = null;
        Minecraft.getMinecraft().displayGuiScreen(null);
        ExtendedAccountData current = this.getCurrentAsEditable();
        ++Objects.requireNonNull(current).useCount;
        current.lastused = JavaTools.getJavaCompat().getDate();
    }

    private void login(int selected) {
        ExtendedAccountData data = this.queriedaccounts.get(selected);
        this.loginfailed = AltManager.getInstance().setUser(data.user, data.pass);
        if (this.loginfailed == null) {
            Minecraft.getMinecraft().displayGuiScreen(null);
            ExtendedAccountData current = this.getCurrentAsEditable();
            Objects.requireNonNull(current).premium = EnumBool.TRUE;
            ++current.useCount;
            current.lastused = JavaTools.getJavaCompat().getDate();
        } else if (this.loginfailed instanceof AlreadyLoggedInException) {
            Objects.requireNonNull(this.getCurrentAsEditable()).lastused = JavaTools.getJavaCompat().getDate();
        } else if (HttpTools.ping("https://minecraft.net")) {
            Objects.requireNonNull(this.getCurrentAsEditable()).premium = EnumBool.FALSE;
        }
    }

    private void edit() {
        this.mc.displayGuiScreen(new GuiEditAccount(this.selectedAccountIndex));
    }

    private void updateQueried() {
        this.queriedaccounts = this.convertData();
        if (!this.query.equals(I18n.format("Search", new Object[0])) && !this.query.equals("")) {
            for (int i = 0; i < this.queriedaccounts.size(); ++i) {
                if (!this.queriedaccounts.get(i).alias.contains(this.query) && ConfigValues.CASESENSITIVE) {
                    this.queriedaccounts.remove(i);
                    --i;
                    continue;
                }
                if (this.queriedaccounts.get(i).alias.toLowerCase().contains(this.query.toLowerCase()) || ConfigValues.CASESENSITIVE)
                    continue;
                this.queriedaccounts.remove(i);
                --i;
            }
        }
        if (!this.queriedaccounts.isEmpty()) {
            while (this.selectedAccountIndex >= this.queriedaccounts.size()) {
                --this.selectedAccountIndex;
            }
        }
    }

    protected void keyTyped(char character, int keyIndex) {
        if (keyIndex == 200 && !this.queriedaccounts.isEmpty()) {
            if (this.selectedAccountIndex > 0) {
                --this.selectedAccountIndex;
            }
        } else if (keyIndex == 208 && !this.queriedaccounts.isEmpty()) {
            if (this.selectedAccountIndex < this.queriedaccounts.size() - 1) {
                ++this.selectedAccountIndex;
            }
        } else if (keyIndex == 1) {
            this.escape();
        } else if (keyIndex == 211 && this.delete.enabled) {
            this.delete();
        } else if (character == '+') {
            this.add();
        } else if (character == '/' && this.edit.enabled) {
            this.edit();
        } else if (!this.search.isFocused() && keyIndex == 19) {
            this.reloadSkins();
        } else if (keyIndex == 28 && !this.search.isFocused() && (this.login.enabled || this.loginoffline.enabled)) {
            if ((Keyboard.isKeyDown(54) || Keyboard.isKeyDown(42)) && this.loginoffline.enabled) {
                this.logino(this.selectedAccountIndex);
            } else if (this.login.enabled) {
                this.login(this.selectedAccountIndex);
            }
        } else if (keyIndex == 14) {
            if (this.search.isFocused() && this.query.length() > 0) {
                this.query = this.query.substring(0, this.query.length() - 1);
                this.updateText();
                this.updateQueried();
            }
        } else if (keyIndex == 63) {
            this.reloadSkins();
        } else if (character != '\u0000' && this.search.isFocused()) {
            if (keyIndex == 28) {
                this.search.setFocused(false);
                this.updateText();
                this.updateQueried();
                return;
            }
            this.query = this.query + character;
            this.updateText();
            this.updateQueried();
        }
    }

    private ArrayList<ExtendedAccountData> convertData() {
        ArrayList<AccountData> tmp = (ArrayList) AltDatabase.getInstance().getAlts().clone();
        ArrayList<ExtendedAccountData> converted = new ArrayList<ExtendedAccountData>();
        int index = 0;
        for (AccountData data : tmp) {
            if (data instanceof ExtendedAccountData) {
                converted.add((ExtendedAccountData) data);
            } else {
                converted.add(new ExtendedAccountData(EncryptionTools.decode(data.user), EncryptionTools.decode(data.pass), data.alias));
                AltDatabase.getInstance().getAlts().set(index, new ExtendedAccountData(EncryptionTools.decode(data.user), EncryptionTools.decode(data.pass), data.alias));
            }
            ++index;
        }
        return converted;
    }

    private ArrayList<AccountData> getAccountList() {
        return AltDatabase.getInstance().getAlts();
    }

    private ExtendedAccountData getCurrentAsEditable() {
        for (AccountData dat : this.getAccountList()) {
            if (!(dat instanceof ExtendedAccountData) || !dat.equals(this.queriedaccounts.get(this.selectedAccountIndex)))
                continue;
            return (ExtendedAccountData) dat;
        }
        return null;
    }

    private void updateButtons() {
        this.login.enabled = !this.queriedaccounts.isEmpty() && !EncryptionTools.decode(this.queriedaccounts.get(this.selectedAccountIndex).pass).equals("");
        this.loginoffline.enabled = !this.queriedaccounts.isEmpty();
        this.delete.enabled = !this.queriedaccounts.isEmpty();
        this.edit.enabled = !this.queriedaccounts.isEmpty();
        this.reloadskins.enabled = !AltDatabase.getInstance().getAlts().isEmpty();
    }

    class List
            extends GuiSlot {
        public List(Minecraft mcIn) {
            super(mcIn, GuiAccountSelector.this.width, GuiAccountSelector.this.height, 32, GuiAccountSelector.this.height - 64, 14);
        }

        protected int getSize() {
            return GuiAccountSelector.this.queriedaccounts.size();
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            GuiAccountSelector.this.selectedAccountIndex = slotIndex;
            GuiAccountSelector.this.updateButtons();
            if (isDoubleClick && GuiAccountSelector.this.login.enabled) {
                GuiAccountSelector.this.login(slotIndex);
            }
        }

        protected boolean isSelected(int slotIndex) {
            return slotIndex == GuiAccountSelector.this.selectedAccountIndex;
        }

        protected int getContentHeight() {
            return GuiAccountSelector.this.queriedaccounts.size() * 14;
        }

        protected void drawBackground() {
            GuiAccountSelector.this.drawDefaultBackground();
        }

        protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks) {
            ExtendedAccountData data = GuiAccountSelector.this.queriedaccounts.get(slotIndex);
            String s = data.alias;
            if (StringUtils.isEmpty(s)) {
                s = I18n.format("Alt", new Object[0]) + " " + (slotIndex + 1);
            }
            int color = 0xFFFFFF;
            if (Minecraft.getMinecraft().getSession().getUsername().equals(data.alias)) {
                color = 65280;
            }
            GuiAccountSelector.this.drawString(GuiAccountSelector.this.fontRenderer, s, xPos + 2, yPos + 1, color);
        }
    }
}

