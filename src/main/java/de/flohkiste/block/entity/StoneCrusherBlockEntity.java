package de.flohkiste.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class StoneCrusherBlockEntity extends BlockEntity {
    private int counter;

    public StoneCrusherBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.STONE_CRUSHER_BLOCK_ENTITY, pos, state);
    }

    public int getCounter() {
        return counter;
    }

    public void incrementCounter() {
        this.counter++;
        markDirty();

        if(!(this.world instanceof ServerWorld sWorld))
            return;

        if(this.counter % 10 == 0) {
            EntityType.PIG.spawn(sWorld, this.pos.up(), SpawnReason.TRIGGERED);
        }

        if(this.counter % 100 == 0) {
            EntityType.LIGHTNING_BOLT.spawn(sWorld, this.pos.up(), SpawnReason.TRIGGERED);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putInt("counter", this.counter);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        this.counter = nbt.contains("counter", NbtElement.INT_TYPE) ? nbt.getInt("counter") : 0;
    }
}
