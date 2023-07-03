package we.devs.forever.mixin.mixins.server;

import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InventoryPlayer.class)
public class FixMixinInventoryPlayer {

    @Shadow
    public int currentItem;

//    @Final
//    @Shadow
//    public final NonNullList<ItemStack> mainInventory = NonNullList.<ItemStack>withSize(36, ItemStack.EMPTY);


//    @Redirect(method = {"pickItem"}, at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/InventoryPlayer;currentItem:I", opcode = Opcodes.PUTFIELD))
//    public void pickItemHook(InventoryPlayer inventoryPlayer, int value) {
//    }

    @Redirect(method = {"pickItem"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;getBestHotbarSlot()I"))
    public int pickItem(InventoryPlayer inventoryPlayer, int value) {
        return inventoryPlayer.currentItem;
    }

    /**
     * @author HunterEagle_
     * @reason Need to fix default method PickItem
     */
}
