//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "D:\Rat Checker\1.12 stable mappings"!

/*
 * Decompiled with CFR 0.151.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 */
package we.devs.forever.api.manager.impl.player.holeManager.thread.holes;

import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.event.events.client.TickEvent;
import we.devs.forever.api.event.events.network.WorldClientEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.manager.api.AbstractManager;
import we.devs.forever.api.util.Util;
import we.devs.forever.api.util.hole.HoleUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class HoleManager
extends AbstractManager
implements Util, IHoleManager { private static final HoleObserver EMPTY = new EmptyHoleObserver();
    /** <tt>true</tt> if we can start a new calc. */
    private final AtomicBoolean finished = new AtomicBoolean(true);
    /** Registered HoleObservers. */
    private final Set<HoleObserver> observers = new HashSet<>();

    private List<BlockPos> safe      = Collections.emptyList();
    private List<BlockPos> unsafe    = Collections.emptyList();
    private List<BlockPos> longHoles = Collections.emptyList();
    private List<BlockPos> bigHoles  = Collections.emptyList();

    public HoleManager()
    {
        super("HoleM");
    }

    @EventListener
    public void onTick(TickEvent event) {
        runTick();
    }

    @EventListener
    public void onWorldLoad(WorldClientEvent.Load event) {
        synchronized (holeManager)
        {
            safe      = Collections.emptyList();
            unsafe    = Collections.emptyList();
            longHoles = Collections.emptyList();
            bigHoles  = Collections.emptyList();
        }
    }

    /**
     * @return Safe (Bedrock only) holes. (Immutable)
     */
    public List<BlockPos> getSafe()
    {
        return safe;
    }

    /**
     * @return Unsafe holes. (Immutable)
     */
    public List<BlockPos> getUnsafe()
    {
        return unsafe;
    }

    /**
     * @return 2x1 holes. (Immutable)
     */
    public List<BlockPos> getLongHoles()
    {
        return longHoles;
    }

    /**
     * Note that these positions are calculated with
     * {@link HoleUtil#is2x2Partial(BlockPos)}, so
     * you need to add (1, 0, 0), (0, 0, 1), (1, 0, 1)
     * to get the full hole.
     *
     * @return 2x2 holes. (Immutable)
     */
    public List<BlockPos> getBigHoles()
    {
        return bigHoles;
    }

    /**
     * Runs a new calculation, but only if
     * <p>- mc.player != null,
     * <p>- mc.world != null,
     * <p>- theres no calculation currently running,
     * <p>- and theres at least one Observer registered.
     */
    private void runTick()
    {
        if (mc.player != null
                && mc.world != null
                && finished.get()
                && observers.stream().anyMatch(HoleObserver::isThisHoleObserverActive))
        {
            double maxRange = getMaxRange();
            if (maxRange == 0)
            {
                return;
            }

            int safes   = observers.stream()
                    .max(Comparator.comparing(
                            HoleObserver::getSafeHoles))
                    .orElse(EMPTY)
                    .getSafeHoles();

            int unsafes = observers.stream()
                    .max(Comparator.comparing(
                            HoleObserver::getUnsafeHoles))
                    .orElse(EMPTY)
                    .getSafeHoles();

            int longs   = observers.stream()
                    .max(Comparator.comparing(
                            HoleObserver::get2x1Holes))
                    .orElse(EMPTY)
                    .getUnsafeHoles();

            int bigs    = observers.stream()
                    .max(Comparator.comparing(
                            HoleObserver::get2x2Holes))
                    .orElse(EMPTY)
                    .getUnsafeHoles();

            if (safes != 0 || unsafes != 0 || longs != 0 || bigs != 0)
            {
                finished.set(false);
                calc(maxRange, safes, unsafes, longs, bigs);
            }
        }
    }

    protected void calc(double maxRange, int safes, int unsafes, int longs, int bigs)
    {
        threadManager.submit(
                new HoleRunnable(this, maxRange, safes, unsafes, longs, bigs));
    }

    /**
     * @param safe sets the safe holes.
     */
    @Override
    public void setSafe(List<BlockPos> safe)
    {
        this.safe = safe;
    }

    /**
     * @param unsafe sets the unsafe holes.
     */
    @Override
    public void setUnsafe(List<BlockPos> unsafe)
    {
        this.unsafe = unsafe;
    }

    /**
     * @param longHoles sets the 2x2 holes.
     */
    @Override
    public void setLongHoles(List<BlockPos> longHoles)
    {
        this.longHoles = longHoles;
    }

    /**
     * @param bigHoles sets the 2x2 holes.
     */
    @Override
    public void setBigHoles(List<BlockPos> bigHoles)
    {
        this.bigHoles = bigHoles;
    }

    /**
     * Marks the current calculation as finished,
     * so that a new one can start.
     */
    @Override
    public void setFinished()
    {
        finished.set(true);
        synchronized (this)
        {
            this.notifyAll();
        }
    }

    /**
     * Adds an observer for this Manager. Only if
     * there's at least one observer present the
     * calculation will be run.
     *
     * Adding and removing Observers shouldn't
     * happen asynchronously.
     *
     * @param observer the Object to subscribe.
     * @return <tt>true</tt> if there are no other
     *             observers registered.
     */
    public boolean register(HoleObserver observer)
    {
        observers.add(observer);

        if (observers.size() == 1)
        {
            runTick();
            return true;
        }

        return false;
    }

    /**
     * Unregistering the observer when its not needed
     * is recommended, to remove the reference and make
     * this HoleManager not run unnecessary calculations.
     *
     * Adding and removing Observers shouldn't
     * happen asynchronously.
     *
     * @param observer the observer to unregister.
     */
    public void unregister(HoleObserver observer)
    {
        observers.remove(observer);
    }

    /**
     * @return <tt>true</tt> if there's no calc going on right now.
     */
    public boolean isFinished()
    {
        return finished.get();
    }

    /**
     * @return the biggest range of all the currently registered HoleObservers.
     */
    public double getMaxRange()
    {
        if (observers.isEmpty())
        {
            return 0.0;
        }

        try
        {
            return Collections.max(observers).getRange();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return 0.0;
    }

    public List<BlockPos> getHoleList() {
        List<BlockPos> blockPos = new ArrayList<>();
        blockPos.addAll(safe);
        blockPos.addAll(unsafe);
        blockPos.addAll(longHoles);
        blockPos.addAll(bigHoles);
        return blockPos;
    }

    @Override
    protected void onLoad() {

    }

    @Override
    protected void onUnload() {

    }
}

