package we.devs.forever.mixin.mixins.player;


import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import we.devs.forever.api.event.events.player.ItemUsedEvent;
import we.devs.forever.main.ForeverClient;

@Mixin(value = {ItemFood.class})
public class MixinItemFood {
    @Inject(method = {"onItemUseFinish"}, at = {@At(value = "HEAD")})
    public void onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving, CallbackInfoReturnable<ItemStack> info) {
        ForeverClient.EVENT_BUS.post(new ItemUsedEvent(entityLiving, stack));
    }
}
