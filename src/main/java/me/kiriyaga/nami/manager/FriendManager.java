package me.kiriyaga.nami.manager;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static me.kiriyaga.nami.Nami.CHAT_MANAGER;
import static me.kiriyaga.nami.Nami.LOGGER;

public class FriendManager {

    private final Set<String> friends = new HashSet<>();
    private final File file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public FriendManager() {
        File dir = new File(FabricLoader.getInstance().getGameDir().toFile(), "2bEssentials");
        this.file = new File(dir, "friends.json");
    }

    public void load() {
        friends.clear();

        if (!file.exists()) {
            LOGGER.info("Friend list not found, starting with empty list.");
            return;
        }

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);
            if (!element.isJsonArray()) return;

            for (JsonElement friend : element.getAsJsonArray()) {
                if (friend.isJsonPrimitive() && friend.getAsJsonPrimitive().isString()) {
                    friends.add(friend.getAsString().toLowerCase());
                }
            }

            LOGGER.info("Loaded " + friends.size() + " friends.");
        } catch (Exception e) {
            LOGGER.error("Failed to load friends.json", e);
        }
    }

    public void save() {
        try {
            JsonArray array = new JsonArray();
            for (String name : friends) {
                array.add(name);
            }

            file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                gson.toJson(array, writer);
            }

            LOGGER.info("Saved friends.json");
        } catch (Exception e) {
            LOGGER.error("Failed to save friends.json", e);
        }
    }

    public boolean isFriend(String name) {
        return name != null && friends.contains(name.toLowerCase());
    }

    public void addFriend(String name) {
        if (name == null) return;
        if (friends.add(name.toLowerCase())) {
            CHAT_MANAGER.sendPersistent(FriendManager.class.getName(), "Added friend: §7" + name);
            save();
        }
    }

    public void removeFriend(String name) {
        if (name == null) return;
        if (friends.remove(name.toLowerCase())) {
            CHAT_MANAGER.sendPersistent(FriendManager.class.getName(), "Removed friend: §7" + name);
            save();
        }
    }

    public Set<String> getFriends() {
        return Collections.unmodifiableSet(friends);
    }
}
