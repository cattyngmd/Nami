package me.kiriyaga.nami.feature.module.impl.misc;

import com.google.common.collect.Sets;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.Set;

@RegisterModule
public class NoSoundLagModule extends Module {

    public final BoolSetting always = addSetting(new BoolSetting("always", false));
    public final BoolSetting armor = addSetting(new BoolSetting("armor", true));
    public final BoolSetting withers = addSetting(new BoolSetting("withers", true));
    public final BoolSetting firework = addSetting(new BoolSetting("firework", false));
    public final BoolSetting elytra = addSetting(new BoolSetting("elytra", true));

    private static final Set<RegistryEntry<SoundEvent>> ARMOR_SOUNDS = Sets.newHashSet(
            SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
            SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA,
            SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,
            SoundEvents.ITEM_ARMOR_EQUIP_IRON,
            SoundEvents.ITEM_ARMOR_EQUIP_GOLD,
            SoundEvents.ITEM_ARMOR_EQUIP_CHAIN,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER
    );

    private static final Set<SoundEvent> FIREWORK_SOUNDS = Sets.newHashSet(
            SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH,
            SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST,
            SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE,
            SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST,
            SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR,
            SoundEvents.ENTITY_FIREWORK_ROCKET_SHOOT,
            SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR,
            SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST_FAR
    );

    private static final Set<SoundEvent> ELYTRA_SOUNDS = Sets.newHashSet(
            SoundEvents.ITEM_ELYTRA_FLYING
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
        super("no sound lag", "Prevents lag caused by stacked sounds.", ModuleCategory.of("misc"), "nosoundlag");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!isEnabled()) return;

        if (event.getPacket() instanceof PlaySoundS2CPacket packet) {
            SoundEvent sound = packet.getSound().comp_349();

            boolean cancel = false;

            if (always.get()) {
                if (armor.get() && ARMOR_SOUNDS.contains(packet.getSound())) {
                    cancel = true;
                }
                if (firework.get() && FIREWORK_SOUNDS.contains(sound)) {
                    cancel = true;
                }
                if (elytra.get() && ELYTRA_SOUNDS.contains(sound)) {
                    cancel = true;
                }
                if (withers.get() && WITHER_SOUNDS.contains(sound)) {
                    cancel = true;
                }
            } else {
                if (armor.get() && ARMOR_SOUNDS.contains(packet.getSound())) {
                    cancel = true;
                } else if (firework.get() && FIREWORK_SOUNDS.contains(sound)) {
                    cancel = true;
                } else if (elytra.get() && ELYTRA_SOUNDS.contains(sound)) {
                    cancel = true;
                } else if (withers.get() && WITHER_SOUNDS.contains(sound)) {
                    cancel = true;
                }
            }

            if (cancel) {
                event.cancel();
            }
        }
    }
}
