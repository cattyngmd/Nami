package me.kiriyaga.essentials.feature.module.impl.misc;

import com.google.common.collect.Sets;
import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.PacketReceiveEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.Set;

public class NoSoundLagModule extends Module {

    public final BoolSetting armor = addSetting(new BoolSetting("armor", true));
    public final BoolSetting withers = addSetting(new BoolSetting("withers", false));

    private static final Set<RegistryEntry<SoundEvent>> ARMOR_SOUNDS = Sets.newHashSet(
            SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
            SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA,
            SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,
            SoundEvents.ITEM_ARMOR_EQUIP_IRON,
            SoundEvents.ITEM_ARMOR_EQUIP_GOLD,
            SoundEvents.ITEM_ARMOR_EQUIP_CHAIN,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER
    );

    private static final Set<SoundEvent> WITHER_SOUNDS = Sets.newHashSet(
            SoundEvents.ENTITY_WITHER_AMBIENT,
            SoundEvents.ENTITY_WITHER_DEATH,
            SoundEvents.ENTITY_WITHER_BREAK_BLOCK,
            SoundEvents.ENTITY_WITHER_HURT,
            SoundEvents.ENTITY_WITHER_SPAWN,
            SoundEvents.ENTITY_WITHER_SHOOT
    );

    public NoSoundLagModule() {
        super("NoSoundLag", "Prevents lag caused by armor and wither sounds.", Category.misc, "nosoundlag");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!isEnabled()) return;

        if (event.getPacket() instanceof PlaySoundS2CPacket packet) {
            SoundEvent sound = packet.getSound().value();

            boolean cancel = false;

            if (armor.get() && ARMOR_SOUNDS.contains(packet.getSound())) {
                cancel = true;
            } else if (withers.get() && WITHER_SOUNDS.contains(sound)) {
                cancel = true;
            }

            if (cancel) {
                event.cancel();
            }
        }
    }
}
