package we.devs.forever.mixin.mixins;


import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.crash.CrashReport;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.forever.api.event.events.client.KeyEvent;
import we.devs.forever.api.event.events.client.TickEvent;
import we.devs.forever.api.event.events.network.WorldClientEvent;
import we.devs.forever.api.util.test.LoggerException;
import we.devs.forever.client.modules.impl.client.CustomMainMenu;
import we.devs.forever.client.modules.impl.player.MultiTask;
import we.devs.forever.client.modules.impl.render.NoRender;
import we.devs.forever.client.ui.customScreen.GuiCustomMainScreen;
import we.devs.forever.client.ui.customScreen.SplashProgress;
import we.devs.forever.main.ForeverClient;

@Mixin(value = {Minecraft.class}, priority = 2000)
public class MixinMinecraft {
    @Shadow
    public WorldClient world;
//    @Shadow
//    public void displayGuiScreen(@Nullable GuiScreen var1) {
//
//    }

//
//    @Inject(method = "Lnet/minecraft/client/Minecraft;getLimitFramerate()I", at = @At("HEAD"), cancellable = true)
//    public void getLimitFramerateHook(CallbackInfoReturnable<Integer> callbackInfoReturnable) {
//        try {
//            if (Management.getInstance().unfocusedCpu.getValue() && !Display.isActive()) {
//                callbackInfoReturnable.setReturnValue(Management.getInstance().cpuFPS.getValue());
//            }
//        } catch (NullPointerException ignored) {
//        }
//    }

    @Inject(method={"runTick"}, at={@At(value="INVOKE", target="Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V", ordinal=0, shift=At.Shift.BEFORE)})
    public void runTickHook(CallbackInfo info) {
        ForeverClient.EVENT_BUS.post(new TickEvent());
    }

    @Inject(method = {"runTick()V"}, at = {@At(value = "RETURN")})
    private void runTick(CallbackInfo callbackInfo) {
        ForeverClient.EVENT_BUS.post(new TickEvent.Post());
        if (Minecraft.getMinecraft().currentScreen instanceof GuiMainMenu && CustomMainMenu.INSTANCE.isEnabled()) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiCustomMainScreen());
        }
    }

    //    @Inject(method={"displayGuiScreen"}, at={@At(value="HEAD")})
//    private void displayGuiScreen(GuiScreen screen, CallbackInfo ci) {
//        if (screen instanceof GuiMainMenu) {
//            displayGuiScreen(new GuiCustomMainScreen());
//        }
//    }
    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayCrashReport(Lnet/minecraft/crash/CrashReport;)V"))
    public void displayCrashReportHook(Minecraft minecraft, CrashReport crashReport) {
        unload();
    }

    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;doVoidFogParticles(III)V"))
    public void doVoidFogParticlesHook(WorldClient world, int x, int y, int z) {
        NoRender.getInstance().doVoidFogParticles(x, y, z);
    }

    @Redirect(method = {"rightClickMouse"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;getIsHittingBlock()Z", ordinal = 0))
    private boolean isHittingBlockHook(PlayerControllerMP playerControllerMP) {
        return !MultiTask.getInstance().isEnabled() && playerControllerMP.getIsHittingBlock();
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    public void shutdownHook(CallbackInfo info) {
        unload();
    }

    private void unload() {
        System.out.println("Shutting down: saving configuration");
        ForeverClient.managers.unload(true);
        System.out.println("Configuration saved.");
    }

    @Redirect(method = "sendClickBlockToController", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isHandActive()Z"))
    private boolean isHandActiveWrapper(EntityPlayerSP playerSP) {
        return !MultiTask.getInstance().isEnabled() && playerSP.isHandActive();
    }

    @Inject(method = "init", at = @At("HEAD"))
    public void onInitMinecraft(CallbackInfo ci) {
        ForeverClient.EVENT_BUS.post(ForeverClient.INSTANCE);
    }

    @Inject(method = {"drawSplashScreen"}, at = {@At(value = "HEAD")}, cancellable = true)
    public void drawSplashScreen(TextureManager textureManager, CallbackInfo callbackInfo) {
        callbackInfo.cancel();
        SplashProgress.drawSplash(textureManager);
        SplashProgress.setProgress(1, "Starting Game...");
    }

    @Inject(method={"loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V"}, at={@At(value="HEAD")})
    public void loadWorldHook(WorldClient worldClient, String loadingMessage, CallbackInfo info) {
        if (this.world != null) {
            ForeverClient.EVENT_BUS.post(new WorldClientEvent.Unload(this.world));
        }
    }

    @Inject(method = {"init"}, at = {@At(value = "INVOKE", remap = false, target = "Lnet/minecraft/client/renderer/texture/TextureMap;<init>(Ljava/lang/String;)V", shift = At.Shift.BEFORE)})
    private void onLoadingTextureMap(CallbackInfo callbackInfo) {
        SplashProgress.setProgress(2, "Loading Texture Map...");

    }

    @Inject(method = {"init"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/ModelManager;<init>(Lnet/minecraft/client/renderer/texture/TextureMap;)V", shift = At.Shift.AFTER)})
    private void onLoadingModelManager(CallbackInfo callbackInfo) {
        SplashProgress.setProgress(3, "Loading Model Manager...");

    }

    @Inject(method = {"init"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderItem;<init>(Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/client/renderer/block/model/ModelManager;Lnet/minecraft/client/renderer/color/ItemColors;)V", shift = At.Shift.AFTER)})
    private void onLoadingItemRenderer(CallbackInfo callbackInfo) {
        LoggerException.testMethod(() -> {
            SplashProgress.setProgress(4, "Loading Item Renderer...");
        });

    }

    @Inject(method = {"init"}, at = {@At(value = "INVOKE", remap = false, target = "Lnet/minecraft/client/renderer/EntityRenderer;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/resources/IResourceManager;)V", shift = At.Shift.AFTER)})
    private void onLoadingEntityRenderer(CallbackInfo callbackInfo) {
        LoggerException.testMethod(() -> {
            SplashProgress.setProgress(5, "Loading Entity Renderer...");
        });

    }

    @Inject(method = "runTickKeyboard", at = @At(value = "INVOKE_ASSIGN", target = "org/lwjgl/input/Keyboard.getEventKeyState()Z", remap = false))
    public void runTickKeyboardHook(CallbackInfo callbackInfo) {
        ForeverClient.EVENT_BUS.post(new KeyEvent(Keyboard.getEventKeyState(),
                Keyboard.getEventKey(),
                Keyboard.getEventCharacter()));
    }

    @Inject(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/FMLCommonHandler;fireMouseInput()V"))
    public void mouseInject(CallbackInfo ci) {
       // if(Mouse.getEventButtonState()) {
            KeyEvent event = new KeyEvent(Mouse.getEventButtonState(), Mouse.getEventButton() - 100, (char) Mouse.getEventButton());
            ForeverClient.EVENT_BUS.post(event);
      //  }
    }

    @Inject(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/IReloadableResourceManager;registerReloadListener(Lnet/minecraft/client/resources/IResourceManagerReloadListener;)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER))
    public void onPreInit(CallbackInfo callbackInfo) {
//        ForeverClient.INSTANCE.preInit();
        LoggerException.testMethod(ForeverClient.INSTANCE::preInit);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/ProgressManager$ProgressBar;step(Ljava/lang/String;)V", ordinal = 1, shift = At.Shift.BEFORE))
    public void onInit(CallbackInfo ci) {
//        ForeverClient.INSTANCE.init();
        LoggerException.testMethod(ForeverClient.INSTANCE::init);

    }

    @Inject(method = "init", at = @At("RETURN"))
    public void onPostInit(CallbackInfo ci) {
        LoggerException.testMethod(ForeverClient.INSTANCE::postInit);
//        ForeverClient.INSTANCE.postInit();

    }


}