package we.devs.forever.api.manager.impl.player;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import we.devs.forever.api.manager.api.AbstractManager;

import java.util.HashMap;

public
class SpeedManager extends AbstractManager {
    public SpeedManager() {
        super("SpeedManager");
    }

    @Override
    protected void onLoad() {

    }

    @Override
    protected void onUnload() {

    }

    public static final double LAST_JUMP_INFO_DURATION_DEFAULT = 3.0F;
    public static boolean didJumpThisTick = false;
    public static boolean isJumping = false;
    private final int distancer = 20;
    public double firstJumpSpeed = 0.0F;
    public double lastJumpSpeed = 0.0F;
    public double percentJumpSpeedChanged = 0.0F;
    public double jumpSpeedChanged = 0.0F;
    public boolean didJumpLastTick = false;
    public long jumpInfoStartTime = 0;
    public boolean wasFirstJump = true;
    public double speedometerCurrentSpeed = 0.0D;
    public HashMap<EntityPlayer, Double> playerSpeeds = new HashMap<>();

    public static void setDidJumpThisTick(boolean val) {
        didJumpThisTick = val;
    }

    public static void setIsJumping(boolean val) {
        isJumping = val;
    }

    public float lastJumpInfoTimeRemaining() {
        return (Minecraft.getSystemTime() - this.jumpInfoStartTime) / 1000.0F;
    }

    public void updateValues() {
        double distTraveledLastTickX = mc.player.posX - mc.player.prevPosX;
        double distTraveledLastTickZ = mc.player.posZ - mc.player.prevPosZ;
        this.speedometerCurrentSpeed = distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ;
        if ((didJumpThisTick) && !(mc.player.onGround && !isJumping)) {
            if (!this.didJumpLastTick) {
                this.wasFirstJump = this.lastJumpSpeed == 0.0D;
                this.percentJumpSpeedChanged = (this.speedometerCurrentSpeed != 0.0D) ? (this.speedometerCurrentSpeed / this.lastJumpSpeed - 1.0D) : -1.0D;
                this.jumpSpeedChanged = this.speedometerCurrentSpeed - this.lastJumpSpeed;
                this.jumpInfoStartTime = Minecraft.getSystemTime();
                this.lastJumpSpeed = this.speedometerCurrentSpeed;
                this.firstJumpSpeed = this.wasFirstJump ? this.lastJumpSpeed : 0.0D;
            }

            this.didJumpLastTick = didJumpThisTick;
        } else {
            this.didJumpLastTick = false;
            this.lastJumpSpeed = 0.0F;
        }

            updatePlayers();
    }

    public void updatePlayers() {
        for (EntityPlayer player : mc.world.playerEntities) {
            if (mc.player.getDistanceSq(player) < distancer * distancer) {
                double distTraveledLastTickX = player.posX - player.prevPosX;
                double distTraveledLastTickZ = player.posZ - player.prevPosZ;
                double playerSpeed = distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ;
                playerSpeeds.put(player, playerSpeed);
            }
        }
    }

    public double getPlayerSpeed(EntityPlayer player) {
        if (playerSpeeds.get(player) == null) {
            return 0.0;
        }
        return turnIntoKpH(playerSpeeds.get(player));
    }

    public double turnIntoKpH(double input) {
        return MathHelper.sqrt(input) * 71.2729367892;
    }

    public double getSpeedKpH() {
        double speedometerkphdouble = turnIntoKpH(speedometerCurrentSpeed);
        speedometerkphdouble = Math.round(10.0 * speedometerkphdouble) / 10.0;
        return speedometerkphdouble;
    }

    public double getSpeedMpS() {
        double speedometerMpsdouble = turnIntoKpH(speedometerCurrentSpeed) / 3.6;
        speedometerMpsdouble = Math.round(10.0 * speedometerMpsdouble) / 10.0;
        return speedometerMpsdouble;
    }

}
