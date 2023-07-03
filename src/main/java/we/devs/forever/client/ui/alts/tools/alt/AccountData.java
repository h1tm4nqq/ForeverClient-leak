package we.devs.forever.client.ui.alts.tools.alt;

import we.devs.forever.client.ui.alts.iasencrypt.EncryptionTools;

import java.io.Serializable;

public class AccountData
        implements Serializable {
    public static final long serialVersionUID = -147985492L;
    public final String user;
    public final String pass;
    public String alias;

    protected AccountData(String user, String pass, String alias) {
        this.user = EncryptionTools.encode(user);
        this.pass = EncryptionTools.encode(pass);
        this.alias = alias;
    }

    public boolean equalsBasic(AccountData obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        return this.user.equals(obj.user);
    }
}

