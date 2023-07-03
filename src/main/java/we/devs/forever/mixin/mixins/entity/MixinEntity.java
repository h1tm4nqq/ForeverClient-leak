package we.devs.forever.mixin.mixins.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.forever.api.event.events.player.PushEvent;
import we.devs.forever.api.event.events.player.StepEvent;
import we.devs.forever.api.event.events.player.TurnEvent;
import we.devs.forever.main.ForeverClient;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    public double posX;
    @Shadow
    public double posY;
    @Shadow
    public double posZ;
    @Shadow
    public double motionX;
    @Shadow
    public double motionY;
    @Shadow
    public double motionZ;
    @Shadow
    public float rotationYaw;
    @Shadow
    public float rotationPitch;
    @Shadow
    public boolean onGround;
    @Shadow
    public World world;
    @Shadow
    public double prevPosX;
    @Shadow
    public double prevPosY;
    @Shadow
    public double prevPosZ;
    @Shadow
    public double lastTickPosX;
    @Shadow
    public double lastTickPosY;
    @Shadow
    public double lastTickPosZ;
    @Shadow
    protected EntityDataManager dataManager;
    @Shadow
    public float stepHeight;
    @Shadow
    public boolean isDead;
    @Shadow
    public float width;
    @Shadow
    public float prevRotationYaw;
    @Shadow
    public float prevRotationPitch;
    @Shadow
    public float height;

    @Unique
    private long oldServerX;
    @Unique
    private long oldServerY;
    @Unique
    private long oldServerZ;

    private Float prevHeight;
    private boolean pseudoDead;
    private long stamp;
    private boolean dummy;

    @Shadow
    public abstract boolean isSprinting();

    @Shadow
    public abstract boolean isRiding();

    @Shadow
    public abstract boolean isSneaking();

    @Shadow
    public abstract AxisAlignedBB getEntityBoundingBox();

    @Shadow
    public abstract void setEntityBoundingBox(AxisAlignedBB var1);

    @Shadow
    public abstract void resetPositionToBB();

    @Shadow
    protected abstract void updateFallState(double var1, boolean var3, IBlockState var4, BlockPos var5);

    @Shadow
    protected abstract boolean canTriggerWalking();

    @Shadow
    public abstract boolean isInWater();

    @Shadow
    public abstract boolean isBeingRidden();

    @Shadow
    public abstract Entity getControllingPassenger();

    @Shadow
    public abstract void playSound(SoundEvent var1, float var2, float var3);

    @Shadow
    protected abstract void doBlockCollisions();

    @Shadow
    public abstract boolean isWet();

    @Shadow
    protected abstract void playStepSound(BlockPos var1, Block var2);

    @Shadow
    protected abstract SoundEvent getSwimSound();

    @Shadow
    protected abstract float playFlySound(float var1);

    @Shadow
    protected abstract boolean makeFlySound();

    @Shadow
    public abstract void addEntityCrashInfo(CrashReportCategory var1);

    @Shadow
    protected abstract void dealFireDamage(int var1);

    @Shadow
    public abstract void setFire(int var1);

    @Shadow
    protected abstract int getFireImmuneTicks();

    @Shadow
    public abstract boolean isBurning();

    @Shadow
    public abstract int getMaxInPortalTime();

    public MixinEntity(World worldIn) {
    }

    @Inject(method = "move", at = @At(value = "FIELD", target = "net/minecraft/entity/Entity.stepHeight:F", ordinal = 3, shift = At.Shift.BEFORE))
    public void onGroundHookComp(MoverType type, double x, double y, double z, CallbackInfo info) {
        //noinspection ConstantConditions
        Entity _this = (Entity) (Object) this;
        StepEvent event = new StepEvent(0, _this);
        ForeverClient.EVENT_BUS.post(event);
        this.prevHeight = this.stepHeight;
        this.stepHeight = event.getHeight();
    }


    /**
     * target = {@link Entity#setEntityBoundingBox(AxisAlignedBB)}
     */
    @Inject(method = "move", at = @At(value = "INVOKE", target = "net/minecraft/entity/Entity.setEntityBoundingBox(Lnet/minecraft/util/math/AxisAlignedBB;)V", ordinal = 6, shift = At.Shift.AFTER))
    public void setEntityBoundingBoxHook(MoverType type, double x, double y, double z, CallbackInfo info) {
        //noinspection ConstantConditions
        Entity entity = (Entity) (Object) this;
        StepEvent event = new StepEvent(1, entity);
        ForeverClient.EVENT_BUS.post(event);
    }


    @Redirect(method = "applyEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    public void addVelocityHook(Entity entity, double x, double y, double z) {
        PushEvent event = new PushEvent(entity, x, y, z, true);
        ForeverClient.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            entity.motionX += event.x;
            entity.motionY += event.y;
            entity.motionZ += event.z;
            entity.isAirBorne = event.airbone;
        }
    }

    @Inject(method = {"turn"}, at = {@At("HEAD")}, cancellable = true)
    public void onTurn(final float yaw, final float pitch, final CallbackInfo ci) {
        final TurnEvent event = new TurnEvent(yaw, pitch);
        ForeverClient.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
