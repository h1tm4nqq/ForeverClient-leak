package we.devs.forever.client.command.impl.commands;

import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import we.devs.forever.api.util.Util;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.command.api.Command;

public
class ReloadSoundCommand extends Command {

    public ReloadSoundCommand() {
        super("sound");
    }

    @Override
    public void execute(String[] commands) {
        try {
            SoundManager sndManager = ObfuscationReflectionHelper.getPrivateValue(net.minecraft.client.audio.SoundHandler.class, Util.mc.getSoundHandler(), new String[]{"sndManager", "field_147694_f"});
            sndManager.reloadSoundSystem();
            sendMessage(TextUtil.GREEN + "Reloaded Sound System.");
        } catch (Exception e) {
            System.out.println("Could not restart sound manager: " + e);
            e.printStackTrace();
            sendMessage(TextUtil.RED + "Couldnt Reload Sound System!");
        }
    }
}
