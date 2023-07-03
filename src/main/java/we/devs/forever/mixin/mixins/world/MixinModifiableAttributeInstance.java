package we.devs.forever.mixin.mixins.world;

import com.google.common.collect.Sets;
import net.minecraft.entity.ai.attributes.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = ModifiableAttributeInstance.class)
public abstract class MixinModifiableAttributeInstance {

    @Shadow
    @Final
    private AbstractAttributeMap attributeMap;
    @Shadow
    @Final
    private IAttribute genericAttribute;

    @Mutable
    @Shadow
    @Final
    private Map<Integer, Set<AttributeModifier>> mapByOperation;

    @Shadow
    public abstract double getBaseValue();

    @Shadow
    protected abstract Collection<AttributeModifier> getAppliedModifiers(int operation);

    @Inject(method = "<init>", at = @At("RETURN" ))
    public void fix(AbstractAttributeMap attributeMapIn, IAttribute genericAttributeIn, CallbackInfo ci) {
        mapByOperation = new ConcurrentHashMap<>();
        for (int i = 0; i < 3; ++i) {
            this.mapByOperation.put(i, Sets.newHashSet());
        }
    }

    @Inject(method = "computeValue", at = @At("HEAD" ), cancellable = true)
    public void fix(CallbackInfoReturnable<Double> cir) {
        try {
            double d0 = this.getBaseValue();
            Collection<AttributeModifier> temp = getAppliedModifiers(0);
            Collection<AttributeModifier> temp1 = getAppliedModifiers(1);
            Collection<AttributeModifier> temp2 = getAppliedModifiers(2);

            for (AttributeModifier attributemodifier : temp) {
                d0 += attributemodifier.getAmount();
            }

            double d1 = d0;

            for (AttributeModifier attributemodifier1 : temp1) {
                d1 += d0 * attributemodifier1.getAmount();
            }

            for (AttributeModifier attributemodifier2 : temp2) {
                d1 *= 1.0D + attributemodifier2.getAmount();
            }
            cir.setReturnValue(genericAttribute.clampValue(d1));
        }catch (ConcurrentModificationException exception){
            cir.setReturnValue(genericAttribute.clampValue(0));
        }


    }

    @Inject(method = "getAppliedModifiers", at = @At(value = "HEAD" ), cancellable = true)
    public void fix(int operation, CallbackInfoReturnable<Collection<AttributeModifier>> cir) {
        try {
            Set<AttributeModifier> set = mapByOperation.get(operation);
            AbstractAttributeMap map = attributeMap;
            IAttribute attribute = genericAttribute;

            for (IAttribute iattribute = attribute.getParent(); iattribute != null; iattribute = iattribute.getParent()) {
                IAttributeInstance iattributeinstance = map.getAttributeInstance(iattribute);
                if (iattributeinstance != null) {
                    set.addAll(iattributeinstance.getModifiersByOperation(operation));
                }
            }
            cir.setReturnValue(set);
        } catch (Throwable t) {
            cir.setReturnValue(new HashSet<>());
        }

    }

}
