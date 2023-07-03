//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "D:\Rat Checker\1.12 stable mappings"!

/*
 * Decompiled with CFR 0.151.
 *
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.init.Blocks
 *  net.minecraft.network.play.server.SPacketBlockChange
 *  net.minecraft.network.play.server.SPacketChunkData
 *  net.minecraft.network.play.server.SPacketMultiBlockChange
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Vec3i
 *  net.minecraft.world.chunk.Chunk
 */
package we.devs.forever.api.manager.impl.player.holeManager;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.chunk.Chunk;
import we.devs.forever.api.event.events.client.TickEvent;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.world.BlockStateChangeEvent;
import we.devs.forever.api.event.events.world.UnloadChunkEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.manager.api.AbstractManager;
import we.devs.forever.api.util.Util;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.hole.HoleUtil;
import we.devs.forever.api.util.thread.GlobalExecutor;
import we.devs.forever.mixin.ducks.IChunk;

import java.util.*;

public class InvalidationHoleManager extends AbstractManager implements Util, HoleManager {
    private static final Vec3i[] AIR_OFFSETS = new Vec3i[]{new Vec3i(0, 1, 0), new Vec3i(-1, -1, 0), new Vec3i(1, 0, 1), new Vec3i(1, 0, -1), new Vec3i(-1, 0, -1)};
    private static final Vec3i[] BLOCK_OFFSETS = new Vec3i[]{new Vec3i(0, 0, 0), new Vec3i(0, -1, 0)};
    private final MutPos mut_pos = new MutPos();
    private final Map<BlockPos, Hole> holes = new HashMap<BlockPos, Hole>();
    private final List<Hole> _1x1_safe = new ArrayList<Hole>();
    private final List<Hole> _1x1_unsafe = new ArrayList<Hole>();
    private final List<Hole> _2x1 = new ArrayList<Hole>();
    private final List<Hole> _2x2 = new ArrayList<Hole>();
    private final AirHoleFinder onAirAdded = new AirHoleFinder(this);
    private final BlockHoleFinder onBlockAdded = new BlockHoleFinder(this);
    private final TimerUtil removeTimer = new TimerUtil();
    private final TimerUtil sortTimer = new TimerUtil();
    private final InvalidationConfig config;
    private List<Runnable> callbacks = null;
    ArrayList<Runnable> packetCallbacks = new ArrayList<>();

    public InvalidationHoleManager(InvalidationConfig config) {
        super("Hole manager");
        this.config = config;
    }

    @EventListener
    public void onUnloadChunk(UnloadChunkEvent event) {
        if (fullNullCheck()) return;

        ((IChunk) event.getChunk()).setHoleVersion(((IChunk) event.getChunk()).getHoleVersion() + 1);
        if (config.isUsingInvalidationHoleManager() && mc.world != null)
        {
            holes.entrySet().removeIf(e ->
            {
                if (!mc.world.isBlockLoaded(e.getKey()))
                {
                    e.getValue().invalidate();
                    return true;
                }

                return false;
            });
        }
    }

    @EventListener
    public void onBlockStateChange(BlockStateChangeEvent event) {
        if (fullNullCheck()) return;

        if (config.isUsingInvalidationHoleManager())
        {
            BlockPos pos = event.getPos();
            IBlockState state = event.getState();
            IChunk chunk = event.getChunk();
            chunk.addHoleTask(() ->
            {
                mut_pos.setX(pos.getX());
                mut_pos.setY(pos.getY());
                mut_pos.setZ(pos.getZ());
                invalidate(state.getBlock());
            });

            if (callbacks == null)
            {
                addPostCompilationTask(pos, state, chunk);
            }
            else
            {
                callbacks.add(() -> addPostCompilationTask(pos, state, chunk));
            }
        }
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if (fullNullCheck()) return;

        if (event.getPacket() instanceof SPacketBlockChange) {
            handleBlockChangePacket(event, 1);
        }

        if (event.getPacket() instanceof SPacketMultiBlockChange) {
            this.handleBlockChangePacket((PacketEvent.Receive) event, ((SPacketMultiBlockChange) event.getPacket()).getChangedBlocks().length);
        }

        if (event.getPacket() instanceof SPacketChunkData) {
            if (config.isUsingInvalidationHoleManager())
            {
                SPacketChunkData p = event.getPacket();
                Chunk chunk = mc.world.getChunk(p.getChunkX(), p.getChunkZ());
                HoleFinder compiler = new HoleFinder(chunk, config.getHeight(), this);
                ((IChunk) chunk).setCompilingHoles(true);
                ((IChunk) chunk).setHoleVersion(((IChunk) chunk).getHoleVersion() + 1);
                if (config.shouldCalcChunksAsnyc())
                {
                    if (config.limitChunkThreads())
                    {
                        GlobalExecutor.FIXED_EXECUTOR.submit(compiler);
                    }
                    else
                    {
                        GlobalExecutor.EXECUTOR.submit(compiler);
                    }
                }
                else
                {
                    compiler.run();
                }
            }
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (!config.isUsingInvalidationHoleManager()) {
            return;
        }
        threadManager.submit(() -> {
        if (InvalidationHoleManager.mc.player == null || InvalidationHoleManager.mc.world == null) {
            this.reset();
        } else {
            if (this.sortTimer.passedMs(config.getSortTime())) {
                double x = InvalidationHoleManager.mc.player.posX;
                double y = InvalidationHoleManager.mc.player.posY;
                double z = InvalidationHoleManager.mc.player.posZ;
                this._1x1_safe.sort(Comparator.comparingDouble(h -> h.getDistanceSq(x, y, z)));
                this._1x1_unsafe.sort(Comparator.comparingDouble(h -> h.getDistanceSq(x, y, z)));
                this._2x1.sort(Comparator.comparingDouble(h -> h.getDistanceSq(x, y, z)));
                this._2x2.sort(Comparator.comparingDouble(h -> h.getDistanceSq(x, y, z)));
                this.sortTimer.reset();
            }
            if (this.removeTimer.passedMs(config.getRemoveTime())) {
                this.holes.entrySet().removeIf(e -> !((Hole) e.getValue()).isValid());
                this._1x1_safe.removeIf(h -> !h.isValid());
                this._1x1_unsafe.removeIf(h -> !h.isValid());
                this._2x1.removeIf(h -> !h.isValid());
                this._2x2.removeIf(h -> !h.isValid());
                this.removeTimer.reset();
            }
        }
        });

    }

    private void handleBlockChangePacket(PacketEvent.Receive event, int amount) {
        if (config.isUsingInvalidationHoleManager()) {
            List<Runnable> packetCallbacks = new ArrayList<>(amount);
            mc.addScheduledTask(() ->
            {
                this.callbacks = packetCallbacks;
            });
            threadManager.submit(() ->
            {
                if (packetCallbacks != this.callbacks) {
//                    Earthhack.getLogger().error(
//                            "Callbacks have changed while processing " +
//                                    "a BlockChange packet!");
                    return;
                }

                this.callbacks = null;
                packetCallbacks.forEach(Runnable::run);
            });
        }
    }

    private void addPostCompilationTask(BlockPos pos, IBlockState state, IChunk chunk) {
        chunk.addHoleTask(() -> {
            Block block = state.getBlock();
            if (HoleUtil.NO_BLAST.contains(block)) {
                this.onBlockAdded.setPos(pos);
                this.onBlockAdded.setChunk(chunk);
                this.onBlockAdded.calcHoles();
            } else if (block == Blocks.AIR) {
                this.onAirAdded.setPos(pos);
                this.onAirAdded.setChunk(chunk);
                this.onAirAdded.calcHoles();
            }
        });
    }

    private void invalidate(Block block) {
        if (HoleUtil.NO_BLAST.contains(block)) {
            this.invalidate(BLOCK_OFFSETS);
        } else if (block == Blocks.AIR) {
            this.invalidate(AIR_OFFSETS);
        } else {
            int x = this.mut_pos.getX();
            int y = this.mut_pos.getY();
            int z = this.mut_pos.getZ();
            this.invalidate(AIR_OFFSETS);
            this.mut_pos.setPos(x, y, z);
            this.invalidate(BLOCK_OFFSETS);
        }
    }

    private void invalidate(Vec3i... offsets) {
        for (Vec3i vec3i : offsets) {
            this.mut_pos.incrementX(vec3i.getX());
            this.mut_pos.incrementY(vec3i.getY());
            this.mut_pos.incrementZ(vec3i.getZ());
            Hole hole = this.holes.get((Object) this.mut_pos);
            if (hole == null || !hole.isAirPart((BlockPos) this.mut_pos)) continue;
            this.holes.remove((Object) this.mut_pos);
            hole.invalidate();
        }
    }

    @Override
    public Map<BlockPos, Hole> getHoles() {
        return this.holes;
    }

    @Override
    public List<Hole> get1x1() {
        return this._1x1_safe;
    }

    @Override
    public List<Hole> get1x1Unsafe() {
        return this._1x1_unsafe;
    }

    @Override
    public List<Hole> get2x1() {
        return this._2x1;
    }

    @Override
    public List<Hole> get2x2() {
        return this._2x2;
    }

    @Override
    public List<Hole> getHolesList() {
        return new ArrayList<>(holes.values());
    }

    @Override
    protected void onLoad() {

    }

    @Override
    protected void onUnload() {

    }
}

