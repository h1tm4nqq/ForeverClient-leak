package we.devs.forever.client.ui.alts.ias.account;

import we.devs.forever.client.ui.alts.ias.enums.EnumBool;
import we.devs.forever.client.ui.alts.ias.tools.JavaTools;
import we.devs.forever.client.ui.alts.tools.alt.AccountData;

import java.util.Arrays;

public class ExtendedAccountData
        extends AccountData {
    private static final long serialVersionUID = -909128662161235160L;
    public EnumBool premium;
    public int[] lastused;
    public int useCount;

    public ExtendedAccountData(String user, String pass, String alias) {
        super(user, pass, alias);
        this.useCount = 0;
        this.lastused = JavaTools.getJavaCompat().getDate();
        this.premium = EnumBool.UNKNOWN;
    }

    public ExtendedAccountData(String user, String pass, String alias, int useCount, int[] lastused, EnumBool premium) {
        super(user, pass, alias);
        this.useCount = useCount;
        this.lastused = lastused;
        this.premium = premium;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        ExtendedAccountData other = (ExtendedAccountData) obj;
        if (!Arrays.equals(this.lastused, other.lastused)) {
            return false;
        }
        if (this.premium != other.premium) {
            return false;
        }
        if (this.useCount != other.useCount) {
            return false;
        }
        return this.user.equals(other.user) && this.pass.equals(other.pass);
    }
}

