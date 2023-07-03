package we.devs.forever.client.modules.impl.chat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.DeathEvent;
import we.devs.forever.api.event.events.render.TotemPopEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.manager.impl.player.TargetManager;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.util.Random;


public
class AutoTox extends Module {
    private final Setting<Boolean> log = (new Setting<>("Log", true, "When the enemy logged"));
    private final Setting<Boolean> pop = (new Setting<>("Pop", true, "When the enemy popped"));
    private final Setting<Boolean> ez = (new Setting<>("EZ", true, "When the enemy died"));
    private final Setting<Boolean> onAll = (new Setting<>("onAll", true, "Toxs to everybody who popped, logged, died in your render"));
    private final Setting<Boolean> onFakePlayer = (new Setting<>("OnFakePlayer", false));

    private final Setting<Integer> delay = (new Setting<>("Delay", 10, 0, 30));
    private final TimerUtil timer = new TimerUtil();
    private final Random random = new Random();
    public AutoTox() {
        super("AutoTox", "Automatically Toxs targets", Category.CHAT);

    }


    @EventListener
    public void onTargetKill(DeathEvent event) {
        if (ez.getValue()) {
            sendEZ(event.player.getName(),event.pops);
        }
    }
    @EventListener
    public void onPOP(TotemPopEvent event) {
        if (event.getEntity().equals(mc.player)) return;
        if (friendManager.isFriend(event.getEntity()) ) return;
        if(EntityUtil.isFakePlayer(event.getEntity()) && !onFakePlayer.getValue()) return;
        if (pop.getValue() && (targetManager.isTarget(event.getEntity()) || onAll.getValue())) {
            sendPOP(event.getEntity().getName(), event.getPops());
        }
    }
    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null || mc.world == null) return;
        if (event.getPacket() instanceof SPacketPlayerListItem) {
            SPacketPlayerListItem packet = event.getPacket();
            if (packet.getAction() == SPacketPlayerListItem.Action.REMOVE_PLAYER) {
                for (SPacketPlayerListItem.AddPlayerData data : packet.getEntries()) {
                    EntityPlayer player = mc.world.getPlayerEntityByUUID(data.getProfile().getId());
                    if (player != null && log.getValue() && mc.player.ticksExisted > 100 && (targetManager.isTarget(player)||onAll.getValue())) {
                        sendLOG(player.getName(), TargetManager.getPops(player.getName()));
                    }
                }
            }
        }
    }

    private void sendEZ(String name, int pops) {
        if(timer.passedMs(delay.getValue() * 50)) {
            mc.player.sendChatMessage(dethMessages[random.nextInt(dethMessages.length)].replace("<player>", name).replace("<pop>",String.valueOf(pops)));
            timer.reset();
        }
    }
    private void sendLOG(String name, int pops) {
        if(timer.passedMs(delay.getValue() * 50)) {
            mc.player.sendChatMessage(logMessages[random.nextInt(logMessages.length)].replace("<player>", name).replace("<pop>",String.valueOf(pops)));
            timer.reset();
        }
    }
    private void sendPOP(String name, int pops) {
        if(timer.passedMs(delay.getValue() * 50)) {
            mc.player.sendChatMessage(popMessages[random.nextInt(popMessages.length)].replace("<player>",name).replace("<pop>",String.valueOf(pops)));
            timer.reset();
        }
    }
    String[] logMessages = new String[]{
            "<player> ez log lmao",
            "<player> nigga scared of pvp with me",
            "<player> say Why can't I take control of this bot? he always leaves",
            "<player> EEEEEZZZZZZZ LOG AFTER POPPED <pop> TIMES",
            "<player> RETARD LOGGED RIGHT IN FRONT OF MY EYES AND IT WAS SO FAN",
            "<player> LMFAO SUBHUMAN LOGGED it means that RUSSIAN OWNS HE AND ALL",
            "<player> HE DODGED PVP WITH ME LOL",
            "<player> SUBHUMAN DODGED PVP AFTER <pop> TIMES",
            "<player>  MY FAN DID WANT TO PVP WITH ME SO JUST LOGGED AND STARTED SUCK MY DICK",
            "<player> BITCH SUCKED UP MY DICK AND LOGGED",
            "<player> HAHAHAHAAH THIS NIGGA JUST LOGGED FROM PVP WITH POWER OF Forever Client",
            "<player> U SHOULD BUY Forever Client POOR GUY",
            "<player> EZZZZZ LITTLE BITCH SCARED TO PVP WITH ME AFTER <pop> TIMES",
            "<player> EEEEZZZZZZZZZ DUMB RANDOM",
            "<player> U LOGGED TO RUN TO UR MOTHER WHO SUCKS MY DICK RIGHT NOW???",
            "<player> GET GOOD PIECE OF SHIT LMAO",
            "<player> EEEEZZZZZ LOG FUCKIN SUBHUMAN",
            "<player> DUDE JUST LOGGED NOT BECAUSE HE WON AND BECAUSE HIS FACE IS JUST DIFFICULT TO LOOK AT",
            "<player> guys i joined the server to pvp with REAL PEOPLE but why i see bots like this one",
            "<player> It was a sad day at the hospital when he crawled out of the abortion bucket"
    };

    String[] dethMessages = new String[]{
            "<player> man u r fuckin retard just stfu subhuman",
            "<player> sit nn",
            "<player> brainless subhuman wants to be more clever than he is",
            "<player> retard go fuck ur self",
            "<player> nigga u r just a piece of shit",
            "<player> kid listen u r a random so don't talk to me",
            "<player> retard blasted",
            "<player> one more nn owned",
            "<player> don't be my fan fuckin random",
            "<player> you should finish at least the first grade to talk with me",
            "<player> another retard blasted",
            "<player> russian own u and all",
            "<player> poor without Forever client",
            "<player> pig ill kill u this night",
            "<player> stupid kid i killed ur familly u r the next)",
            "<player> suck my dick bitch",
            "<player> go cry random",
            "<player> stop spending your lunch money on shit cheats retard",
            "<player> u should buy Forever client to be good same like me",
            "<player> Forever client u and all",
            "<player> nigga u r trash did u know it???",
            "<player> do u know what problem do ur parents have? right its u",
            "<player> go suicide nigga trash",
            "<player> poor thirdworlder without friend sucks me so fan))))",
            "<player> random fans me because im so good",
            "<player> is it a family thing for you to die?",
            "<player> don't be so stupid like ur mother and just buy the Forever client poor nigga",
            "<player> u need help to buy a gun to die?",
            "<player> ez owned",
            "<player> owned with power of Forever client",
            "<player> poor man died so fast",
            "<player> u have the same problems with brain as ur family?",
            "<player> break ur computer kid",
            "<player> iq?",
            "<player> iq issue",
            "<player> ez popped nigga",
            "<player> u r so bad player looser",
            "<player> easiest kill in my life",
            "<player> did you skip tutorial button LOL?",
            "<player> What the fuck did you just fucking say about me you little shit?",
            "<player> i didnt think that being so bad like u was possible",
            "<player> ur brain slower than firefox",
            "<player> hahaha ez i won",
            "<player> bro needs help because he cant pvp",
            "<player> stupid roach",
            "<player> keep crying subhuman",
            "<player> why do you even try to kill me? im better than u and its a fact dumb girl",
            "<player> cry more my fan",
            "<player> Thanks for the laugh kick, your existence is very amusing",
            "<player> Quiet you high school dropout.",
            "<player> Can you tell your diary first how bad you are in HVH and after that go pvp vs me.",
            "<player> Single-celled kid go suicide",
            "<player> how is it live with mental impairments?",
            "<player> What do you use? Ah yes, free cheats. U don't have money to buy future or smth like that, poor nigga",
            "<player> Your father begs for milk because he sist in my basement without water",
            "<player> Your poorness pisses me off."
    };
    String[] popMessages = new String[]{
            "<player> EZZZ POP <pop> TIMES PIECE OF SHIT GET GOOD",
            "<player> ez pop <pop> times with power of Forever client",
            "<player> pop <pop> times get good kiddo ",
            "<player> EZZZZZZZ pop <pop> times GO LEARN PVP PUSSY",
            "<player> piece of shit popped <pop> times so ez",
            "<player> easiest pop <pop> times in my life",
            "<player> HAHAHAHA BRO POPPED <pop> TIMES SO EZ LMAO",
            "<player> POP <pop> TIMES OMG MAN UR SO BAD LMAO",
            "<player> my grandma has more skill than you nigga pop <pop> times",
            "<player> trash pop <pop> times retard ",
            "<player> ezz no skill dog pop <pop> times",
            "<player> lame dude tryes to pvp with me but dyes) hahah pop <pop> times",
            "<player> get better tbh bruh pop <pop> times",
            "<player> pop <pop> times ur eyes don't work right? ",
            "<player> You stink of poverty because u don't have Forever client pop <pop> times",
            "<player> Cringelord popped <pop> times so ez "
    };

}

