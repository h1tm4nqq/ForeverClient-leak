package we.devs.forever.client.modules.impl.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.BossInfoClient;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.util.*;

import static net.minecraft.client.gui.GuiBossOverlay.GUI_BARS_TEXTURES;

public
class NoRender extends Module {

    private static NoRender INSTANCE = new NoRender();
    public Setting<Boolean> fire = (new Setting<>("Fire", false, "Removes the portal overlay."));
    public Setting<Boolean> portal = (new Setting<>("Portal", false, "Removes the portal overlay."));
    public Setting<Boolean> pumpkin = (new Setting<>("Pumpkin", false, "Removes the pumpkin overlay."));
    public Setting<Boolean> fastRender = (new Setting<>("FastRender", false, "Makes your tile render faster."));
    public Setting<Boolean> totemPops = (new Setting<>("TotemPop", false, "Removes the Totem overlay."));
    public Setting<Boolean> items = (new Setting<>("Items", false, "Removes items on the ground."));
    public Setting<Boolean> nausea = (new Setting<>("Nausea", false, "Removes Portal Nausea."));
    public Setting<Boolean> hurtcam = (new Setting<>("HurtCam", false, "Removes shaking after taking damage."));
    public Setting<Boolean> tileEntity = (new Setting<>("TileEntity", false));
    public Setting<Double> tileRange = (new Setting<>("TileRange", 10D, 1D, 60D, v -> tileEntity.getValue()));
    public Setting<Boolean> enchantmentTable = (new Setting<>("EnchantmentTable", false, v -> tileEntity.getValue()));
    public Setting<Boolean> tnt = (new Setting<>("TNT", false, v -> tileEntity.getValue()));
    public Setting<Boolean> witherHead = (new Setting<>("WitherHead", false, v -> tileEntity.getValue()));
    public Setting<Boolean> parrots = (new Setting<>("Parrots", false, v -> tileEntity.getValue()));
    public Setting<Boolean> leans = (new Setting<>("Vine", false, v -> tileEntity.getValue()));
    public Setting<Boolean> chest = (new Setting<>("Chests", false, v -> tileEntity.getValue()));
    public Setting<Boolean> water = (new Setting<>("Water", false, v -> tileEntity.getValue()));
    public Setting<Boolean> lava = (new Setting<>("Lava", false, v -> tileEntity.getValue()));
    private final Setting<Boolean> block = (new Setting<>("Block",  true));
    private final Setting<Boolean> lava1 = (new Setting<>("Lava1",  true));
    final Setting<Boolean> water1 = (new Setting<>("Water1",  true));
    public Setting<Boolean> explosions = (new Setting<>("Explosions", false, "Removes crystal explosions."));
    public Setting<Fog> fog = (new Setting<>("Fog", Fog.None, "Removes Fog."));
    public Setting<Boolean> noWeather = (new Setting<>("Weather", false, "AntiWeather"));
    public Setting<Boss> boss = (new Setting<>("BossBars", Boss.None, "Modifies the bossbars."));
    public Setting<Float> scale = (new Setting<>("Scale", 0.0f, 0.5f, 1.0f, "Scale of the bars.", v -> boss.getValue() == Boss.Minimize || boss.getValue() != Boss.Stack));
    public Setting<Boolean> bats = (new Setting<>("Bats", false, "Removes bats."));
    public Setting<NoArmor> noArmor = (new Setting<>("NoArmor", NoArmor.None, "Doesnt Render Armor on players."));
    public Setting<Skylight> skylight = (new Setting<>("Skylight", Skylight.None));
    public Setting<Boolean> barriers = (new Setting<>("Barriers", false, "Barriers"));
    public Setting<Boolean> blocks = (new Setting<>("Blocks", false, "Blocks"));
    // public Setting<Boolean> advancements = (new Setting<>("Advancements", false));
    //  public Setting<Boolean> pigmen = (new Setting<>("Pigmen", false));

    public NoRender() {
        super("NoRender", "Allows you to stop rendering stuff", Category.RENDER);
        setInstance();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static NoRender getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NoRender();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (items.getValue()) {
            mc.world.loadedEntityList.stream().filter(EntityItem.class::isInstance).map(EntityItem.class::cast).forEach(Entity::setDead);
        }
        if (fastRender.getValue()) {
            mc.world.loadedTileEntityList.stream().filter(Objects::nonNull).map(TileEntity.class::cast).forEach(TileEntity::hasFastRenderer);
        }
        if (noWeather.getValue() && mc.world.isRaining()) {
            mc.world.setRainStrength(0);
        }
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketExplosion & explosions.getValue()) {
            event.cancel();
        }
    }

    @SubscribeEvent
    public void Method6254(RenderBlockOverlayEvent renderBlockOverlayEvent) {
        if (NoRender.mc.player == null) return;
        if (NoRender.mc.world == null) {
            return;
        }
        RenderBlockOverlayEvent.OverlayType overlayType = renderBlockOverlayEvent.getOverlayType();
        if (block.getValue() && overlayType == RenderBlockOverlayEvent.OverlayType.BLOCK) {
            renderBlockOverlayEvent.setCanceled(true);
        }
        if (lava1.getValue() && renderBlockOverlayEvent.getBlockForOverlay().getBlock().equals(Blocks.LAVA)) {
            renderBlockOverlayEvent.setCanceled(true);
        }
        if (water1.getValue() && overlayType != RenderBlockOverlayEvent.OverlayType.WATER) {
            renderBlockOverlayEvent.setCanceled(true);
        }

    }


    public void doVoidFogParticles(int posX, int posY, int posZ) {
        int i = 32;
        Random random = new Random();
        ItemStack itemstack = mc.player.getHeldItemMainhand();
        boolean flag = !barriers.getValue() || (mc.playerController.getCurrentGameType() == GameType.CREATIVE && !itemstack.isEmpty() && itemstack.getItem() == Item.getItemFromBlock(Blocks.BARRIER));
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int j = 0; j < 667; ++j) {
            showBarrierParticles(posX, posY, posZ, 16, random, flag, blockpos$mutableblockpos);
            showBarrierParticles(posX, posY, posZ, 32, random, flag, blockpos$mutableblockpos);
        }
    }

    public void showBarrierParticles(int x, int y, int z, int offset, Random random, boolean holdingBarrier, BlockPos.MutableBlockPos pos) {
        int i = x + mc.world.rand.nextInt(offset) - mc.world.rand.nextInt(offset);
        int j = y + mc.world.rand.nextInt(offset) - mc.world.rand.nextInt(offset);
        int k = z + mc.world.rand.nextInt(offset) - mc.world.rand.nextInt(offset);
        pos.setPos(i, j, k);
        IBlockState iblockstate = mc.world.getBlockState(pos);
        iblockstate.getBlock().randomDisplayTick(iblockstate, mc.world, pos, random);

        if (!holdingBarrier && iblockstate.getBlock() == Blocks.BARRIER) {
            mc.world.spawnParticle(EnumParticleTypes.BARRIER, (float) i + 0.5F, (float) j + 0.5F, (float) k + 0.5F, 0.0D, 0.0D, 0.0D);
        }
    }

    @EventListener
    public void onRenderPre(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.BOSSINFO && boss.getValue() != Boss.None) {
            event.setCanceled(true);
        }
    }

    @EventListener
    public void onRenderPost(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.BOSSINFO && boss.getValue() != Boss.None) {
            if (boss.getValue() == Boss.Minimize) {
                Map<UUID, BossInfoClient> map = mc.ingameGUI.getBossOverlay().mapBossInfos;
                if (map == null) return;
                ScaledResolution scaledresolution = new ScaledResolution(mc);
                int i = scaledresolution.getScaledWidth();
                int j = 12;
                for (Map.Entry<UUID, BossInfoClient> entry : map.entrySet()) {
                    BossInfoClient info = entry.getValue();
                    String text = info.getName().getFormattedText();
                    int k = (int) ((i / scale.getValue()) / 2 - 91);
                    GL11.glScaled(scale.getValue(), scale.getValue(), 1);
                    if (!event.isCanceled()) {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        mc.getTextureManager().bindTexture(GUI_BARS_TEXTURES);
                        mc.ingameGUI.getBossOverlay().render(k, j, info);
                        mc.fontRenderer.drawStringWithShadow(text, (i / scale.getValue()) / 2 - mc.fontRenderer.getStringWidth(text) / 2, (float) (j - 9), 16777215);
                    }
                    GL11.glScaled(1d / scale.getValue(), 1d / scale.getValue(), 1);
                    j += 10 + mc.fontRenderer.FONT_HEIGHT;
                }
            } else if (boss.getValue() == Boss.Stack) {
                Map<UUID, BossInfoClient> map = mc.ingameGUI.getBossOverlay().mapBossInfos;
                HashMap<String, Pair<BossInfoClient, Integer>> to = new HashMap<>();
                for (Map.Entry<UUID, BossInfoClient> entry : map.entrySet()) {
                    String s = entry.getValue().getName().getFormattedText();
                    Pair<BossInfoClient, Integer> p;
                    if (to.containsKey(s)) {
                        p = to.get(s);
                        p = new Pair<>(p.getKey(), p.getValue() + 1);
                    } else {
                        p = new Pair<>(entry.getValue(), 1);
                    }
                    to.put(s, p);
                }
                ScaledResolution scaledresolution = new ScaledResolution(mc);
                int i = scaledresolution.getScaledWidth();
                int j = 12;
                for (Map.Entry<String, Pair<BossInfoClient, Integer>> entry : to.entrySet()) {
                    String text = entry.getKey();
                    BossInfoClient info = entry.getValue().getKey();
                    int a = entry.getValue().getValue();
                    text += " x" + a;
                    int k = (int) ((i / scale.getValue()) / 2 - 91);
                    GL11.glScaled(scale.getValue(), scale.getValue(), 1);
                    if (!event.isCanceled()) {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        mc.getTextureManager().bindTexture(GUI_BARS_TEXTURES);
                        mc.ingameGUI.getBossOverlay().render(k, j, info);
                        mc.fontRenderer.drawStringWithShadow(text, (i / scale.getValue()) / 2 - mc.fontRenderer.getStringWidth(text) / 2, (float) (j - 9), 16777215);
                    }
                    GL11.glScaled(1d / scale.getValue(), 1d / scale.getValue(), 1);
                    j += 10 + mc.fontRenderer.FONT_HEIGHT;
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Pre<?> event) {
        if (bats.getValue() && event.getEntity() instanceof EntityBat) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundAtEntityEvent event) {
        if (bats.getValue() && event.getSound().equals(SoundEvents.ENTITY_BAT_AMBIENT)
                || event.getSound().equals(SoundEvents.ENTITY_BAT_DEATH)
                || event.getSound().equals(SoundEvents.ENTITY_BAT_HURT)
                || event.getSound().equals(SoundEvents.ENTITY_BAT_LOOP)
                || event.getSound().equals(SoundEvents.ENTITY_BAT_TAKEOFF)) {
            event.setVolume(0.f);
            event.setPitch(0.f);
            event.setCanceled(true);
        }
    }

//    @EventListener
//    public void tileEntity(RenderEntityEvent event) {
//        if (event.getEntity() != null) {
//            if (witherHead.getValue() && event.getEntity() instanceof EntityWitherSkull ||
//                    tnt.getValue() && event.getEntity() instanceof EntityTNTPrimed ||
//                    parrots.getValue() && event.getEntity() instanceof EntityParrot
//            ) {
//                if (tileRange.getValue() > event.getEntity().getDistance(mc.player)) {
//                    event.cancel();
//                }
//            }
//        }
//    }
//
//    @EventListener
//    public void tileEntity(BlockRenderEvent event) {
//        if (leans.getValue() && event.getBlock() instanceof BlockVine ||
//                chest.getValue() && (event.getBlock() instanceof BlockChest || event.getBlock() instanceof BlockEnderChest)
//        ) {
//            if(tileRange.getValue() > event.getPos().getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ)) {
//                event.cancel();
//            }
//        }
//
//    }


    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    public
    enum Skylight {
        None,
        World,
        Entity,
        All
    }

    public
    enum Fog {
        None,
        Air,
        NoFog
    }

    public
    enum Boss {
        None,
        Remove,
        Stack,
        Minimize
    }

    public
    enum NoArmor {
        None,
        All,
        Helmet
    }

    public static
    class Pair<T, S> {

        private T key;
        private S value;

        public Pair(T key, S value) {
            key = key;
            value = value;
        }

        public T getKey() {
            return key;
        }

        public void setKey(T key) {
            key = key;
        }

        public S getValue() {
            return value;
        }

        public void setValue(S value) {
            value = value;
        }
    }
}
