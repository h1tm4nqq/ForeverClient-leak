package we.devs.forever.mixin.mixins.accessor;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({PlayerControllerMP.class})
public interface IPlayerControllerMP {

    @Invoker(value = "syncCurrentPlayItem")
    void syncItem();

    @Accessor(value = "isHittingBlock")
    void setHittingBlock(final boolean isHittingBlock);

    @Accessor(value = "blockHitDelay")
    void setBlockHitDelay(int blockHitDelay);

    @Accessor("currentPlayerItem")
    int getCurrentPlayerItem();

    @Accessor("currentPlayerItem")
    void setCurrentPlayerItem(int currentPlayerItem);
}