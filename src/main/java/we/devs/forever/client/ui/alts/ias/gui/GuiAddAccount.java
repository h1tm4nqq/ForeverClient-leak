package we.devs.forever.client.ui.alts.ias.gui;

import we.devs.forever.client.ui.alts.ias.account.ExtendedAccountData;
import we.devs.forever.client.ui.alts.tools.alt.AltDatabase;

public class GuiAddAccount
        extends AbstractAccountGui {
    public GuiAddAccount() {
        super("Add Account");
    }

    @Override
    public void complete() {
        AltDatabase.getInstance().getAlts().add(new ExtendedAccountData(this.getUsername(), this.getPassword(), this.getUsername()));
    }
}

