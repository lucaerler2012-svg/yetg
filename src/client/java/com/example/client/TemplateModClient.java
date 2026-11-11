package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class TemplateModClient implements ClientModInitializer {

	private static boolean active = false;
	private static BlockPos crystalPos;
	private static float savedXRot;
	private static float savedYRot;
	private static int ticksWaited;

	@Override
	public void onInitializeClient() {
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			if (!level.isClientSide() || active) {
				return InteractionResult.PASS;
			}
			if (!player.getItemInHand(hand).is(Items.END_CRYSTAL)) {
				return InteractionResult.PASS;
			}
			BlockPos pos = hitResult.getBlockPos();
			if (!level.getBlockState(pos).is(Blocks.OBSIDIAN)) {
				return InteractionResult.PASS;
			}

			savedXRot = player.getXRot();
			savedYRot = player.getYRot();
			crystalPos = pos.relative(hitResult.getDirection());
			ticksWaited = 0;
			active = true;

			player.setXRot(-90.0f);
			return InteractionResult.PASS;
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!active) {
				return;
			}
			Minecraft mc = client;
			if (mc.player == null || mc.level == null || mc.gameMode == null) {
				active = false;
				return;
			}

			List<EndCrystal> crystals = mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(crystalPos).inflate(0.5));
			if (!crystals.isEmpty()) {
				mc.gameMode.attack(mc.player, crystals.get(0));
				mc.player.swing(InteractionHand.MAIN_HAND);
				restore(mc);
				return;
			}

			ticksWaited++;
			if (ticksWaited > 10) {
				restore(mc);
			}
		});
	}

	private static void restore(Minecraft mc) {
		mc.player.setXRot(savedXRot);
		mc.player.setYRot(savedYRot);
		active = false;
	}
}
