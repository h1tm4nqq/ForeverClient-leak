package we.devs.forever.api.manager.impl.player;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import we.devs.forever.api.manager.api.AbstractManager;
import we.devs.forever.api.util.Util;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.modules.impl.client.hud.HUD;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public
class PotionManager extends AbstractManager {
    public PotionManager() {
        super("PotionManager");
    }

    @Override
    protected void onLoad() {

    }

    @Override
    protected void onUnload() {

    }
    private final Map<EntityPlayer, PotionList> potions = new ConcurrentHashMap<>();

    public void onLogout() {
        this.potions.clear();
    }

    public void updatePlayer() {
        PotionList list = new PotionList();
        for (PotionEffect effect : Util.mc.player.getActivePotionEffects()) {
            list.addEffect(effect);
        }
        potions.put(Util.mc.player, list);
    }

    public void update() {
        updatePlayer();
        if (HUD.getInstance().isEnabled() && HUD.getInstance().textRadar.getValue()) {
            ArrayList<EntityPlayer> removeList = new ArrayList<>();
            for (Map.Entry<EntityPlayer, PotionList> potionEntry : potions.entrySet()) {
                boolean notFound = true;
                for (EntityPlayer player : Util.mc.world.playerEntities) {
                    if (potions.get(player) == null) {
                        PotionList list = new PotionList();
                        for (PotionEffect effect : player.getActivePotionEffects()) {
                            list.addEffect(effect);
                        }
                        potions.put(player, list);
                        notFound = false;
                    }

                    if (potionEntry.getKey().equals(player)) {
                        notFound = false;
                    }
                }

                if (notFound) {
                    removeList.add(potionEntry.getKey());
                }
            }

            for (EntityPlayer player : removeList) {
                potions.remove(player);
            }
        }
    }

    public List<PotionEffect> getOwnPotions() {
        return getPlayerPotions(Util.mc.player);
    }

    public List<PotionEffect> getPlayerPotions(EntityPlayer player) {
        PotionList list = this.potions.get(player);
        List<PotionEffect> potions = new ArrayList<>();
        if (list != null) {
            potions = list.getEffects();
        }
        return potions;
    }

    public PotionEffect[] getImportantPotions(EntityPlayer player) {
        PotionEffect[] array = new PotionEffect[3];
        for (PotionEffect effect : getPlayerPotions(player)) {
            Potion potion = effect.getPotion();
            switch ((I18n.format(potion.getName())).toLowerCase()) {
                case "strength":
                    array[0] = effect;
                    break;
                case "weakness":
                    array[1] = effect;
                    break;
                case "speed":
                    array[2] = effect;
                    break;
                default:
            }
        }
        return array;
    }

    public String getPotionString(PotionEffect effect) {
        Potion potion = effect.getPotion();
        return I18n.format(potion.getName()) + " " + (effect.getAmplifier() + 1) + " "/* + TextUtil.WHITE*/ + Potion.getPotionDurationString(effect, 1.0f);
    }

    //bit chinese? There must be a way to make this better...
    public String getColoredPotionString(PotionEffect effect) {
        Potion potion = effect.getPotion();
        switch (I18n.format(potion.getName())) {
            case "Jump Boost":
            case "Speed":
                return TextUtil.AQUA + getPotionString(effect);
            case "Resistance":
            case "Strength":
                return TextUtil.RED + getPotionString(effect);
            case "Wither":
            case "Slowness":
            case "Weakness":
                return TextUtil.BLACK + getPotionString(effect);
            case "Absorption":
                return TextUtil.BLUE + getPotionString(effect);
            case "Haste":
            case "Fire Resistance":
                return TextUtil.GOLD + getPotionString(effect);
            case "Regeneration":
                return TextUtil.LIGHT_PURPLE + getPotionString(effect);
            case "Night Vision":
            case "Poison":
                return TextUtil.GREEN + getPotionString(effect);
            default:
                return TextUtil.WHITE + getPotionString(effect);
        }
    }

    public String getTextRadarPotionWithDuration(EntityPlayer player) {
        PotionEffect[] array = getImportantPotions(player);
        PotionEffect strength = array[0];
        PotionEffect weakness = array[1];
        PotionEffect speed = array[2];
        return "" + (strength != null ? TextUtil.RED + " S" + (strength.getAmplifier() + 1) + " " + Potion.getPotionDurationString(strength, 1.0f) : "")
                + (weakness != null ? TextUtil.DARK_GRAY + " W " + Potion.getPotionDurationString(weakness, 1.0f) : "")
                + (speed != null ? TextUtil.AQUA + " S" + (speed.getAmplifier() + 1) + " " + Potion.getPotionDurationString(weakness, 1.0f) : "");
    }

    public String getTextRadarPotion(EntityPlayer player) {
        PotionEffect[] array = getImportantPotions(player);
        PotionEffect strength = array[0];
        PotionEffect weakness = array[1];
        PotionEffect speed = array[2];
        return "" + (strength != null ? TextUtil.RED + " S" + (strength.getAmplifier() + 1) + " " : "")
                + (weakness != null ? TextUtil.DARK_GRAY + " W " : "")
                + (speed != null ? TextUtil.AQUA + " S" + (speed.getAmplifier() + 1) + " " : "");
    }

    public static
    class PotionList {
        private final List<PotionEffect> effects = new ArrayList<>();

        public void addEffect(PotionEffect effect) {
            if (effect != null) {
                this.effects.add(effect);
            }
        }

        public List<PotionEffect> getEffects() {
            return this.effects;
        }
    }

}
