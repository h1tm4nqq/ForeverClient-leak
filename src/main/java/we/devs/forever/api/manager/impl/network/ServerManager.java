package we.devs.forever.api.manager.impl.network;

import we.devs.forever.api.manager.api.AbstractManager;
import we.devs.forever.api.util.client.TimerUtil;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;

public
class ServerManager extends AbstractManager {

    private final float[] tpsCounts = new float[10];
    private final DecimalFormat format = new DecimalFormat("##.00#");
    private final TimerUtil timerUtil = new TimerUtil();
    private float TPS = 20.0f;
    private long lastUpdate = -1;
    private String serverBrand = "";

    public ServerManager() {
        super("ServerManager");
    }

    public void onPacketReceived() {
        timerUtil.reset();
    }

    public boolean isServerNotResponding() {
        return timerUtil.passedMs(500);
    }

    public long serverRespondingTime() {
        return timerUtil.getPassedTimeMs();
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        if (lastUpdate == -1) {
            lastUpdate = currentTime;
            return;
        }
        long timeDiff = currentTime - lastUpdate;
        float tickTime = timeDiff / 20.0f;
        if (tickTime == 0) {
            tickTime = 50;
        }
        float tps = 1000 / tickTime;
        if (tps > 20.0f) {
            tps = 20.00f;
        }
        System.arraycopy(tpsCounts, 0, tpsCounts, 1, tpsCounts.length - 1);
        tpsCounts[0] = tps;
        double total = 0.0;
        for (float f : tpsCounts) {
            total += f;
        }
        total /= tpsCounts.length;

        if (total > 20.0) {
            total = 20.0;
        }

        TPS = Float.parseFloat(format.format(total));
        lastUpdate = currentTime;
    }

    public void reset() {
        Arrays.fill(tpsCounts, 20.0f);
        TPS = 20.0f;
    }

    public float getTpsFactor() {
        return 20.0f / this.TPS;
    }

    public float getTPS() {
        return this.TPS;
    }

    public String getServerBrand() {
        return this.serverBrand;
    }

    public void setServerBrand(String brand) {
        this.serverBrand = brand;
    }

    public int getPing() {
        if (fullNullCheck()) {
            return 0;
        }

        try {
            return Objects.requireNonNull(mc.getConnection()).getPlayerInfo(mc.getConnection().getGameProfile().getId()).getResponseTime();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    protected void onLoad() {

    }

    @Override
    protected void onUnload() {

    }
}
