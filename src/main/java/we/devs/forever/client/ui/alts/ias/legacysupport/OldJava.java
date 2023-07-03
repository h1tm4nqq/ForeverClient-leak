package we.devs.forever.client.ui.alts.ias.legacysupport;

import net.minecraft.client.resources.I18n;

public class OldJava
        implements ILegacyCompat {
    @Override
    public int[] getDate() {
        return new int[3];
    }

    @Override
    public String getFormattedDate() {
        return I18n.format("updatejava", new Object[0]);
    }
}

