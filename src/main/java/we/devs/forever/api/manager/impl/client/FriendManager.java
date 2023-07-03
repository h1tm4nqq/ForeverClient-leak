package we.devs.forever.api.manager.impl.client;

import net.minecraft.entity.player.EntityPlayer;
import we.devs.forever.api.manager.api.AbstractManager;
import we.devs.forever.api.util.player.PlayerUtil;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.impl.client.Friends;
import we.devs.forever.client.setting.Setting;

import java.awt.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public
class FriendManager extends AbstractManager {
    public Color friendColor = new Color(-11157267);
    private Set<Friend> friends = new HashSet<>();

    public FriendManager() {
        super("Friends");
    }

    public boolean isFriend(String name) {
        if (Friends.friends.isDisabled()) return false;
        cleanFriends();
        return this.friends.stream().anyMatch(friend -> friend.username.equalsIgnoreCase(name));
    }

    public boolean isFriend(EntityPlayer player) {
        return this.isFriend(player.getName());
    }

    public void addFriend(String name) {
        Friend friend = getFriendByName(name);
        if (friend != null) {
            this.friends.add(friend);
            Client.targetManager.resetTargets();
        }
        cleanFriends();
    }

    public void removeFriend(String name) {
        cleanFriends();
        for (Friend friend : this.friends) {
            if (friend.getUsername().equalsIgnoreCase(name)) {
                friends.remove(friend);
                Client.targetManager.resetTargets();
                break;
            }
        }
    }




    public void saveFriends() {
        cleanFriends();
        for (Friend friend : this.friends) {
            new Setting<>(friend.getUuid().toString(), friend.getUsername());
        }
    }

    public void cleanFriends() {
        friends = this.friends.parallelStream().filter(Objects::nonNull).filter(friend -> friend.getUsername() != null).collect(Collectors.toSet());
    }

    public Set<Friend> getFriends() {
        cleanFriends();
        return this.friends;
    }

    public Friend getFriendByName(String input) {
        UUID uuid = PlayerUtil.getUUIDFromName(input);
        if (uuid != null) {
            return new Friend(input, uuid);
        }
        return null;
    }

    public void addFriend(Friend friend) {
        this.friends.add(friend);
    }

    @Override
    protected void onLoad() {
        //Всем похуй
    }

    @Override
    protected void onUnload() {

    }

    public static
    class Friend {

        private final String username;
        private final UUID uuid;

        public Friend(String username, UUID uuid) {
            this.username = username;
            this.uuid = uuid;
        }

        public String getUsername() {
            return username;
        }

        public UUID getUuid() {
            return uuid;
        }
    }
}
