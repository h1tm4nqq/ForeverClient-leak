package we.devs.forever.client.modules.impl.render.holeesp;

import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.util.hole.TwoHole;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.modules.api.ModuleThread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class UpdateThread extends ModuleThread<HoleESP> {
    public UpdateThread(HoleESP module) {
        super(module, "HoleThread", 50);
    }

    @Override
    public void invoke() {

        if (mc.world == null || mc.player == null) return;

        List<BlockPos> obiHoles = new ArrayList<>();
        List<BlockPos> bedrockHoles = new ArrayList<>();

        List<TwoHole> obiHolesTwoBlock = new ArrayList<>();
        List<TwoHole> bedrockHolesTwoBlock = new ArrayList<>();
        Iterable<BlockPos> blocks = BlockPos.getAllInBox(mc.player.getPosition().add(-module.rangeXZ.getValue(), -module.rangeY.getValue(), -module.rangeXZ.getValue()), mc.player.getPosition().add(module.rangeXZ.getValue(), module.rangeY.getValue(), module.rangeXZ.getValue()));
        for (BlockPos pos : blocks) {
            if (!(
                    mc.world.getBlockState(pos).getMaterial().blocksMovement() &&
                            mc.world.getBlockState(pos.add(0, 1, 0)).getMaterial().blocksMovement() &&
                            mc.world.getBlockState(pos.add(0, 2, 0)).getMaterial().blocksMovement()
            )) {


                if (BlockUtil.validObi(pos) && module.obsidian.getValue()) {
                    obiHoles.add(pos);
                } else {
                    TwoHole validTwoBlock = TwoHole.validTwoBlockObiXZ(pos,true,true);
                    if (validTwoBlock != null && module.obsidian.getValue() && module.twoBlock.getValue()) {
                        obiHolesTwoBlock.add(validTwoBlock);
                    }
                }

                if (BlockUtil.validBedrock(pos) && module.bedrock.getValue()) {
                    bedrockHoles.add(pos);
                } else {
                    TwoHole validTwoBlock = TwoHole.validTwoBlockBedrockXZ(pos,true);
                    if (validTwoBlock != null && module.bedrock.getValue() && module.twoBlock.getValue()) {
                        bedrockHolesTwoBlock.add(validTwoBlock);
                    }
                }
            }

        }
        module.obiHoles = obiHoles;
        module.bedrockHoles = bedrockHoles;
        module.obiHolesTwoBlock = obiHolesTwoBlock;
        module.bedrockHolesTwoBlock = bedrockHolesTwoBlock;

    }


}
