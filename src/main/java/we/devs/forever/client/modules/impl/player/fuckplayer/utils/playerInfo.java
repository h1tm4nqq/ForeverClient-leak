package we.devs.forever.client.modules.impl.player.fuckplayer.utils;

public class playerInfo   {

    public final String name;
    public int tickPop;
    public int tickRegen;

    public playerInfo(final String name) {
        this.tickPop = -1;
        this.tickRegen = 0;
        this.name = name;
    }

    public boolean update() {
//        if (this.tickPop != -1 && ++this.tickPop >= AntiStackOverFlow.fakePlayer.vulnerabilityTick.getValue()) {
//            this.tickPop = -1;
//        }
//        if (++this.tickRegen >= AntiStackOverFlow.fakePlayer.tickRegenVal.getValue()) {
//            this.tickRegen = 0;
//            return true;
//        }
        return false;
    }

    public boolean canPop() {
        return this.tickPop == -1;
    }
}
