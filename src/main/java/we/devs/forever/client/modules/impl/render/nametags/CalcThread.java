package we.devs.forever.client.modules.impl.render.nametags;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import we.devs.forever.api.util.player.RotationUtil;
import we.devs.forever.client.modules.api.ModuleThread;
import we.devs.forever.client.ui.alts.tools.Pair;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CalcThread extends ModuleThread<Nametags> {
    public CalcThread(Nametags module) {
        super(module, 25);
    }

    @Override
    public void invoke() {
        if(fullNullCheck()) return;
        module.pairList  = mc.world.playerEntities.stream()
                .filter(Objects::nonNull)
                .filter(EntityLivingBase::isEntityAlive)
                .filter(x -> x.getDistance(mc.player.posX, mc.player.posY, mc.player.posZ) <= module.nameRange.getValue())
                .filter(x -> !x.equals(mc.player))
                .filter(x -> !module.onlyFov.getValue() || RotationUtil.isInFov(x))
                .sorted(Comparator.comparing(x -> x.getDistanceSq(mc.player)))
                .limit(module.maxEntity.getValue())
                .map(x -> new Pair<>(x, module.getDisplayTag(x)))
                .collect(Collectors.toList());
    }
}
