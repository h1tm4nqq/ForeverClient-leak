//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "D:\Rat Checker\1.12 stable mappings"!

/*
 * Decompiled with CFR 0.151.
 *
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$MutableBlockPos
 *  net.minecraft.util.math.Vec3i
 */
package we.devs.forever.api.manager.impl.player.holeManager.thread.holes;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import we.devs.forever.api.util.Util;
import we.devs.forever.api.util.hole.HoleUtil;
import we.devs.forever.api.util.math.PositionUtil;
import we.devs.forever.api.util.math.Sphere;
import we.devs.forever.api.util.thread.SafeRunnable;

import java.util.ArrayList;
import java.util.List;

public class HoleRunnable implements SafeRunnable, Util {
    private final IHoleManager manager;
    private final List<BlockPos> safe;
    private final List<BlockPos> unsafe;
    private final List<BlockPos> longOnes;
    private final List<BlockPos> bigHoles;
    private final double holeRange;
    private final int longs;
    private final int big;
    private final int safes;
    private final int unsafes;

    public HoleRunnable(IHoleManager manager, HoleObserver observer) {
        this(manager, observer.getRange(), observer.getSafeHoles(), observer.getUnsafeHoles(), observer.get2x1Holes(), observer.get2x2Holes());
    }

    /**
     * Constructs a new HoleRunnable.
     *
     * @param holeRange the range to check holes in around the player.
     * @param longs     if we should check 2x1 holes.
     * @param big       if we should check 2x2 holes.
     */
    public HoleRunnable(IHoleManager manager, double holeRange, int safe, int unsafe, int longs, int big) {
        this.manager = manager;
        this.holeRange = holeRange;
        this.safes = safe;
        this.big = big;
        this.unsafes = unsafe;
        this.longs = longs;
        this.safe = new ArrayList<>(safe);
        this.unsafe = new ArrayList<>(unsafe);
        this.longOnes = new ArrayList<>(longs);
        this.bigHoles = new ArrayList<>(big);
    }

    @Override
    public void runSafely() {
        try {
            BlockPos middle = PositionUtil.getPosition();
            int mX = middle.getX();
            int mY = middle.getY();
            int mZ = middle.getZ();
            int maxRadius = Sphere.getRadius(holeRange);
            BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
            for (int i = 0; i < maxRadius; i++) {
                Vec3i vec3i = Sphere.get(i);
                mPos.setPos(mX + vec3i.getX(), mY + vec3i.getY(), mZ + vec3i.getZ());
                boolean done = true;
                if (safe.size() < safes || unsafe.size() < unsafes) {
                    done = false;
                    boolean[] isHole = HoleUtil.isHole(mPos, true);
                    if (isHole[0]) {
                        if (isHole[1]) {
                            safe.add(mPos.toImmutable());
                        } else {
                            unsafe.add(mPos.toImmutable());
                        }

                        continue;
                    }
                }

                if (longOnes.size() < longs) {
                    done = false;
                    if (HoleUtil.is2x1(mPos.toImmutable())) {
                        longOnes.add(mPos.toImmutable());
                        continue;
                    }
                }

                if (bigHoles.size() < big) {
                    done = false;
                    if (HoleUtil.is2x2Partial(mPos.toImmutable())) {
                        bigHoles.add(mPos.toImmutable());
                        continue;
                    }
                }

                if (done) {
                    break;
                }
            }

            manager.setSafe(safe);
            manager.setUnsafe(unsafe);
            manager.setLongHoles(longOnes);
            manager.setBigHoles(bigHoles);
        } finally {
            manager.setFinished();
        }
    }
}

