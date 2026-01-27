package me.kiriyaga.nami.feature.command.impl;

import com.mojang.authlib.GameProfile;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class FakePlayerCommand extends Command {

    private RemotePlayer fakePlayer;

    public FakePlayerCommand() {
        super("fakeplayer", new CommandArgument[] {new CommandArgument.StringArg("name", 0, 25), new CommandArgument.DoubleArg("health", 0, 20)}, "fp");
    }

    @Override
    public void execute(Object[] args) {
        if (MC.level == null || MC.player == null)
            return;

        if (fakePlayer != null) {
            EVENT_MANAGER.unregister(this);
            MC.level.removeEntity(fakePlayer.getId(), Entity.RemovalReason.DISCARDED);
            fakePlayer = null;

            CHAT_MANAGER.sendPersistent(this.name, CAT_FORMAT.format("Fake player has been{red} removed{reset}."));
            return;
        }

        String name = args[0].toString();

        double health = 20.0f;
        health = (double) args[1];

        fakePlayer = new RemotePlayer(MC.level, new GameProfile(UUID.randomUUID(), name));
        fakePlayer.copyPosition(MC.player);
        fakePlayer.setYRot(MC.player.getYRot());
        fakePlayer.setXRot(MC.player.getXRot());
        fakePlayer.setId(-696969); // mint!!!!!!!!
        fakePlayer.setHealth((float)health);
        copy(MC.player, fakePlayer);
        MC.level.addEntity(fakePlayer);

        CHAT_MANAGER.sendPersistent(this.name, CAT_FORMAT.format("Fake player has been{green} added{reset}."));
        EVENT_MANAGER.register(this);
    }

    @SubscribeEvent
    public void onPreTickEvent(PreTickEvent event) {
        if (MC.level == null || MC.player == null || fakePlayer == null)
            return;

        if (fakePlayer.getHealth() <= 0.0f) {
            fakePlayer.setHealth(20.0f);
            fakePlayer.removeAllEffects();
        }

        damage(0.5f);
    }

    private void damage(float amount) {
        float newHealth = fakePlayer.getHealth() - amount;

        if (newHealth <= 0.0f) {
            fakePlayer.setHealth(0.5f);
            fakePlayer.removeAllEffects();
        } else {
            fakePlayer.setHealth(newHealth);
        }
    }

    private void copy(Player from, Player to) {
        to.setItemSlot(EquipmentSlot.MAINHAND, from.getItemBySlot(EquipmentSlot.MAINHAND).copy());
        to.setItemSlot(EquipmentSlot.OFFHAND, from.getItemBySlot(EquipmentSlot.OFFHAND).copy());
        to.setItemSlot(EquipmentSlot.HEAD, from.getItemBySlot(EquipmentSlot.HEAD).copy());
        to.setItemSlot(EquipmentSlot.CHEST, from.getItemBySlot(EquipmentSlot.CHEST).copy());
        to.setItemSlot(EquipmentSlot.LEGS, from.getItemBySlot(EquipmentSlot.LEGS).copy());
        to.setItemSlot(EquipmentSlot.FEET, from.getItemBySlot(EquipmentSlot.FEET).copy());
    }

}
