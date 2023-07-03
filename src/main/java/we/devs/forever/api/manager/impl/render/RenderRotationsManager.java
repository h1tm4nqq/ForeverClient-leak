package we.devs.forever.api.manager.impl.render;

import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.MathHelper;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.event.eventsys.handler.ListenerPriority;
import we.devs.forever.api.manager.api.AbstractManager;

public class RenderRotationsManager extends AbstractManager {

    private int ticksExisted;
    private float prevRenderYawOffset;
    private float prevPitch;
    private float renderPitch;
    private float renderYawOffset;
    private float prevRotationYawHead;
    private float rotationYawHead;

    public RenderRotationsManager() {
        super("Render rotations manager");
    }

    public float getPrevPitch() {
        return prevPitch;
    }

    private float getRenderYawOffset(float yaw, float offsetIn) {
        float result = offsetIn;
        float offset;

        double xDif = mc.player.posX - mc.player.prevPosX;
        double zDif = mc.player.posZ - mc.player.prevPosZ;

        if (xDif * xDif + zDif * zDif > 0.0025000002f) {
            offset = (float) MathHelper.atan2(zDif, xDif) * 57.295776f - 90.0f;
            float wrap = MathHelper.abs(MathHelper.wrapDegrees(yaw) - offset);
            if (95.0F < wrap && wrap < 265.0F) {
                result = offset - 180.0F;
            } else {
                result = offset;
            }
        }

        if (mc.player.swingProgress > 0.0F) {
            result = yaw;
        }

        result = offsetIn + MathHelper.wrapDegrees(result - offsetIn) * 0.3f;
        offset = MathHelper.wrapDegrees(yaw - result);

        if (offset < -75.0f) {
            offset = -75.0f;
        } else if (offset >= 75.0f) {
            offset = 75.0f;
        }

        result = yaw - offset;
        if (offset * offset > 2500.0f) {
            result += offset * 0.2f;
        }

        return result;
    }

    public boolean check() {
        return true;
    }


    public float getRenderYawOffset() {
        return renderYawOffset;
    }

    public void set(float yaw, float f2) {
        if (mc.player.ticksExisted == this.ticksExisted) {
            return;
        }
        this.ticksExisted = mc.player.ticksExisted;
        float f = rotationManager.injectYawStep(yaw, 1.0F);
        prevPitch = renderPitch;
        prevRenderYawOffset = renderYawOffset;
        renderYawOffset = this.getRenderYawOffset(f, prevRenderYawOffset);
        prevRotationYawHead = rotationYawHead;
        rotationYawHead = f;
        renderPitch = f2;
    }

    public float getRotationYawHead() {
        return rotationYawHead;
    }

    @EventListener(priority= ListenerPriority.LOWEST)
    public void invoke(MotionEvent.Pre motionEvent) {
        this.set(motionEvent.getYaw(), motionEvent.getPitch());
    }

    public float getRenderPitch() {
        return renderPitch;
    }

    public float getPrevRenderYawOffset() {
        return prevRenderYawOffset;
    }

    public float getPrevRotationYawHead() {
        return prevRotationYawHead;
    }

    @EventListener(priority=ListenerPriority.LOWEST)
    public void onPacketSend(PacketEvent.Send send) {
        if (send.getPacket() instanceof CPacketPlayer && ((CPacketPlayer)send.getPacket()).rotating) {
            this.set(((CPacketPlayer)send.getPacket()).yaw, ((CPacketPlayer)send.getPacket()).pitch);
        }
    }

    @EventListener(priority=ListenerPriority.HIGH)
    public void onReceivePacket(PacketEvent.Receive receive) {
        if (receive.isCanceled() || fullNullCheck()) {
            return;
        }
        if (receive.getStage() == 0 && receive.getPacket() instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook sPacketPlayerPosLook = receive.getPacket();
            this.set(sPacketPlayerPosLook.yaw, sPacketPlayerPosLook.pitch);
        }
    }

    @Override
    protected void onLoad() {

    }

    @Override
    protected void onUnload() {

    }
}
