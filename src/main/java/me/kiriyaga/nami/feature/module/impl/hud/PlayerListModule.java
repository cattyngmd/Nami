package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.util.entity.EntityUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.text.DecimalFormat;
import java.util.*;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class PlayerListModule extends HudElementModule {

    public enum SortMode { ASCENDING, DESCENDING }

    private final EnumSetting<SortMode> sortMode = addSetting(new EnumSetting<>("Sort", SortMode.DESCENDING));
    private final BoolSetting showDistance = addSetting(new BoolSetting("Distance", true));
    private final BoolSetting showHealth = addSetting(new BoolSetting("Health", true));

    private final List<TextElement> elements = new ArrayList<>();
    private final DecimalFormat dec = new DecimalFormat("0.#");

    public PlayerListModule() {
        super("PlayerList", "Shows nearby players", 0, 0, 80, 10);
    }

    @Override
    public List<TextElement> getTextElements() {
        elements.clear();

        if (MC.level == null || MC.player == null)
            return elements;

        List<Entity> players = EntityUtils.getEntities(EntityUtils.EntityTypeCategory.PLAYERS);;
        sort(players);

        int offset = 0;
        int w = 0;

        for (Entity player : players) {
            Component t = text(player);
            int width = FONT_MANAGER.getWidth(t);
            elements.add(new TextElement(t, 0, offset));
            w = Math.max(w, width);
            offset += FONT_MANAGER.getHeight();
        }
        this.width = w;
        this.height = offset;

        return elements;
    }

    private Component text(Entity entity) {
        StringBuilder sb = new StringBuilder();

        if (showHealth.get() && entity instanceof Player player) {
            double hp = player.getHealth() + player.getAbsorptionAmount();
            double health = Math.round(hp * 2.0) / 2.0;
            if (health >= 19) sb.append("{green}");
            else if (health >= 13) sb.append("{yellow}");
            else if (health >= 8) sb.append("{gold}");
            else if (health >= 6) sb.append("{red}");
            else sb.append("{dark_red}");

            sb.append(dec.format(hp)).append(" ");
        }

        String name = entity.getName().getString();
        if (FRIEND_MANAGER.isFriend(name)) sb.append("{bg}").append(name);
        else sb.append("{bw}").append(name);

        if (showDistance.get()) {
            double dist = Math.round(Math.sqrt(MC.player.distanceToSqr(entity)));
            if (dist <= 15) sb.append(" {dark_red}");
            else if (dist <= 25) sb.append(" {red}");
            else if (dist <= 40) sb.append(" {yellow}");
            else if (dist <= 60) sb.append(" {dark_green}");
            else sb.append(" {green}");

            sb.append(dec.format(dist));
        }
        return CAT_FORMAT.format(sb.toString());
    }

    private void sort(List<Entity> list) {
        switch (sortMode.get()) {
            case ASCENDING -> list.sort(Comparator.comparingInt(p -> FONT_MANAGER.getWidth(text(p))));
            case DESCENDING -> list.sort((a, b) -> Integer.compare(FONT_MANAGER.getWidth(text(b)), FONT_MANAGER.getWidth(text(a))));
        }
    }
}
