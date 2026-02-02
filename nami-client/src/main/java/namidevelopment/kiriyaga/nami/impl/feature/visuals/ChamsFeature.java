package namidevelopment.kiriyaga.nami.impl.feature.visuals;

import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.feature.client.ColorFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;
import namidevelopment.kiriyaga.nami.util.ColorUtils;
import namidevelopment.kiriyaga.nami.util.entity.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.awt.*;

@RegisterFeature
public class ChamsFeature extends Feature {

    public final IntSetting alpha = addSetting(new IntSetting("Alpha", 90, 0, 255));
    public final BoolSetting showPlayers = addSetting(new BoolSetting("Players", true));
    public final BoolSetting showPeacefuls = addSetting(new BoolSetting("Peacefuls", true));
    public final BoolSetting showNeutrals = addSetting(new BoolSetting("Neutrals", false));
    public final BoolSetting showHostiles = addSetting(new BoolSetting("Hostiles", false));

    public ChamsFeature() {
        super("Chams", "csgo.", FeatureCategory.of("Render"));
    }

    public Color getESPColor(Entity entity) {
        if (entity == null || entity.isRemoved() || !entity.isAlive()) return null;

        if (entity instanceof Player player) {
            if (!showPlayers.get()) return null;
            return (FRIEND_SERVICE.isFriend(player.getName().getString()) ? FEATURE_SERVICE.getStorage().getByClass(ColorFeature.class).getFriendTextColor(alpha.get()) : FEATURE_SERVICE.getStorage().getByClass(ColorFeature.class).getStyledGlobalColor(alpha.get()));
        }

        if (showPeacefuls.get() && EntityUtils.getEntities(EntityUtils.EntityTypeCategory.PASSIVE).contains(entity)) return ColorUtils.COLOR_PASSIVE;
        if (showNeutrals.get() && EntityUtils.getEntities(EntityUtils.EntityTypeCategory.NEUTRAL).contains(entity)) return ColorUtils.COLOR_NEUTRAL;
        if (showHostiles.get() && EntityUtils.getEntities(EntityUtils.EntityTypeCategory.HOSTILE).contains(entity)) return ColorUtils.COLOR_HOSTILE;
        return null;
    }
}
