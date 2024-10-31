package de.flohkiste.block.entity;

import de.flohkiste.StoneCrusher;
import de.flohkiste.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemGroups;

public class BlockEntities {
    public static final BlockEntityType<StoneCrusherBlockEntity> STONE_CRUSHER_BLOCK_ENTITY = BlockEntityType.Builder.create(StoneCrusherBlockEntity::new, ModBlocks.STONE_CRUSHER).build();

    public static void registerModBlocks() {
        StoneCrusher.LOGGER.info("Registering Mod Block Entities for " + StoneCrusher.MOD_ID);
    }
}
