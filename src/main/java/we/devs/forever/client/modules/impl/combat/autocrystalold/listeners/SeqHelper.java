package we.devs.forever.client.modules.impl.combat.autocrystalold.listeners;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.client.modules.api.ModuleHelper;
import we.devs.forever.client.modules.impl.combat.autocrystalold.AutoCrystal;

public class SeqHelper extends ModuleHelper<AutoCrystal> {
    public volatile BlockPos expecting;
    public volatile Vec3d crystalPos;

    public SeqHelper(AutoCrystal module) {
        super(module);
    }

    public boolean isBlockingPlacement() {
        return module.sequential.getValue()
                && expecting != null
                && !module.seqTimer.passedMs(module.seqTime.getValue());
    }

    public void setExpecting(BlockPos expecting) {
        module.seqTimer.reset();
        this.expecting = expecting;
        crystalPos = null;
    }
}
