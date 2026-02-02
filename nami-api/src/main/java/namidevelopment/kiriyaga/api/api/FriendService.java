package namidevelopment.kiriyaga.api.api;

import namidevelopment.kiriyaga.api.api.config.ConfigService;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static namidevelopment.kiriyaga.api.NamiApi.*;

public class FriendService {

    private final ConfigService configService;
    private Set<String> friends = new HashSet<>();

    public FriendService(ConfigService configService) {
        this.configService = configService;
        load();
    }

    public void load() {
        this.friends = new HashSet<>(configService.loadFriends());
    }

    public void addFriend(String name) {
        if (name == null) return;
        if (friends.add(name.toLowerCase())) {
            configService.saveFriends(friends);
        }
    }

    public void removeFriend(String name) {
        if (name == null) return;
        if (friends.remove(name.toLowerCase())) {
            configService.saveFriends(friends);
        }
    }

    public void addFriend(Component name) {
        if (name == null) return;
        addFriend(name.getString());
    }

    public void removeFriend(Component name) {
        if (name == null) return;
        removeFriend(name.getString());
    }

    public boolean isFriend(String name) {
        return configService.isFriend(name);
    }

    public Set<String> getFriends() {
        return Collections.unmodifiableSet(friends);
    }
}
