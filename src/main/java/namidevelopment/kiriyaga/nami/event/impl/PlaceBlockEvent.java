package namidevelopment.kiriyaga.nami.event.impl;

import namidevelopment.kiriyaga.nami.event.Event;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public class PlaceBlockEvent extends Event {
    private final LocalPlayer player;
    private final InteractionHand hand;
    private final BlockHitResult hitResult;


    public PlaceBlockEvent(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult) {
        this.player = player;
        this.hand = hand;
        this.hitResult = hitResult;
    }
    public LocalPlayer getPlayer()
    {
        return player;
    }

    public InteractionHand getHand()
    {
        return hand;
    }

    public BlockHitResult getHitResult()
    {
        return hitResult;
    }
}
