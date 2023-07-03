package we.devs.forever.client.modules.impl.movement.fastfall;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.MoveEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.util.HashMap;
import java.util.Map;

public class FastFall extends Module {
    public static FastFall INSTANCE;


    public Setting<Float> speed = (new Setting<>("Speed", 2.0F, 1.0F, 10.0F));
    public Setting<Integer> height = (new Setting<>("Height", 2, 1, 20));
    public Setting<Boolean> stop = (new Setting<>("Stop", false));
    private final Setting<Boolean> noLag = (new Setting<>("AntiLag", true));

    private final TimerUtil lagTimer = new TimerUtil(1000);
    public float TICK_TIMER;

    public FastFall() {
        super("FastFall", "aka ReverseStep", Category.MOVEMENT);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        TICK_TIMER = 1;
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) return;
        if (mc.player.isInWater() || mc.player.isInLava() || mc.player.isInWeb || (noLag.getValue() && !lagTimer.passedMs())) return;
        if (height.getValue() > 0 && (traceDown() > height.getValue())) return;
        if (mc.player.onGround){
            mc.player.motionY -=  speed.getValue() / 10;
        }

    }

    @EventListener
    public void onMove(MoveEvent event) {
        if (mc.player.isInWater() || mc.player.isInLava() || fullNullCheck()) return;
        if ((double) height.getValue() > 0 && (traceDown() > height.getValue())) return;

        if (stop.getValue() && trace() && mc.player.onGround) {
            event.setX(event.getX() * 0.05);
            event.setZ(event.getZ() * 0.05);
            //event.motionY -= speed / 10;
        }
    }
    @EventListener
    public void onPacket(PacketEvent.Receive event) {
        if (!fullNullCheck()) {
            if (event.getPacket() instanceof SPacketPlayerPosLook) {
                lagTimer.reset();
            }
        }
    }
    public int traceDown() {
        int ret = 0;

        int y = (int) Math.round(mc.player.posY) - 1;

        for (int tracey = y; tracey >= 0; tracey--) {
            RayTraceResult trace = mc.world.rayTraceBlocks(
                    mc.player.getPositionVector(),
                    new Vec3d(mc.player.posX, tracey, mc.player.posZ),
                    false
            );

            if (trace != null && trace.typeOfHit == RayTraceResult.Type.BLOCK)
                return ret;

            ret++;
        }

        return ret;
    }

    private boolean trace() {
        AxisAlignedBB bbox = mc.player.getEntityBoundingBox();
        Vec3d basepos = bbox.getCenter();

        double minX = bbox.minX;
        double minZ = bbox.minZ;
        double maxX = bbox.maxX;
        double maxZ = bbox.maxZ;

        Map<Vec3d, Vec3d> positions = new HashMap<>();

        positions.put(
                basepos,
                new Vec3d(basepos.x, basepos.y - 1, basepos.z));

        positions.put(
                new Vec3d(minX, basepos.y, minZ),
                new Vec3d(minX, basepos.y - 1, minZ));

        positions.put(
                new Vec3d(maxX, basepos.y, minZ),
                new Vec3d(maxX, basepos.y - 1, minZ));

        positions.put(
                new Vec3d(minX, basepos.y, maxZ),
                new Vec3d(minX, basepos.y - 1, maxZ));

        positions.put(
                new Vec3d(maxX, basepos.y, maxZ),
                new Vec3d(maxX, basepos.y - 1, maxZ));

        for (Vec3d key : positions.keySet()) {
            RayTraceResult result = mc.world.rayTraceBlocks(key, positions.get(key), true);
            if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK)
                return false;
        }

        IBlockState state = mc.world.getBlockState(new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ));

        return state.getBlock() == Blocks.AIR;
    }
}
