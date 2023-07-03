package we.devs.forever.client.ui.alts.ias.gui;

import we.devs.forever.client.ui.alts.ias.account.ExtendedAccountData;
import we.devs.forever.client.ui.alts.ias.enums.EnumBool;
import we.devs.forever.client.ui.alts.ias.tools.JavaTools;
import we.devs.forever.client.ui.alts.iasencrypt.EncryptionTools;
import we.devs.forever.client.ui.alts.tools.alt.AccountData;
import we.devs.forever.client.ui.alts.tools.alt.AltDatabase;

class GuiEditAccount
        extends AbstractAccountGui {
    private final ExtendedAccountData data;
    private final int selectedIndex;

    public GuiEditAccount(int index) {
        super("ias.editaccount");
        this.selectedIndex = index;
        AccountData data = AltDatabase.getInstance().getAlts().get(index);
        this.data = data instanceof ExtendedAccountData ? (ExtendedAccountData) data : new ExtendedAccountData(data.user, data.pass, data.alias, 0, JavaTools.getJavaCompat().getDate(), EnumBool.UNKNOWN);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.setUsername(EncryptionTools.decode(this.data.user));
        this.setPassword(EncryptionTools.decode(this.data.pass));
    }

    @Override
    public void complete() {
        AltDatabase.getInstance().getAlts().set(this.selectedIndex, new ExtendedAccountData(this.getUsername(), this.getPassword(), this.hasUserChanged ? this.getUsername() : this.data.alias, this.data.useCount, this.data.lastused, this.data.premium));
    }
}

