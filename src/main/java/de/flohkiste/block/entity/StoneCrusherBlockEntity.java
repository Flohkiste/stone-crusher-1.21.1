package de.flohkiste.block.entity;

import de.flohkiste.block.custom.StoneCrusherBlock;
import de.flohkiste.screen.StoneCrusherScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class StoneCrusherBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory{
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    int burnTime;
    int fuelTime;
    int crushTime;
    int crushTimeTotal;


    public StoneCrusherBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.STONE_CRUSHER_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, this.inventory, registryLookup);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, this.inventory, registryLookup);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public Text getDisplayName() {
        return Text.of("Stone Crusher");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new StoneCrusherScreenHandler(syncId, playerInventory, this);
    }

    public static void tick(World world, BlockPos blockPos, BlockState state, StoneCrusherBlockEntity blockEntity) {
        boolean isBurning = blockEntity.isBurning();
        boolean isDirty = false;

        if (isBurning) {
            --blockEntity.burnTime;
        }

        ItemStack fuelItemStack = blockEntity.inventory.get(StoneCrusherBlockEntity.FUEL_SLOT);
        ItemStack inputItemStack = blockEntity.inventory.get(StoneCrusherBlockEntity.INPUT_SLOT);
        ItemStack outputItemStack = blockEntity.inventory.get(StoneCrusherBlockEntity.OUTPUT_SLOT);

        if (isBurning || (!fuelItemStack.isEmpty() && !inputItemStack.isEmpty())) {
            if (!isBurning && canUseAsFuel(fuelItemStack)) {
                blockEntity.burnTime = getFuelTime(fuelItemStack);
                blockEntity.fuelTime = blockEntity.burnTime;

                if (blockEntity.isBurning()) {
                    isDirty = true;
                    if (!fuelItemStack.isEmpty()) {
                        fuelItemStack.decrement(1);
                    }
                }
            }

            if (isBurning && canUseAsInput(inputItemStack)) {
                ++blockEntity.crushTime;
                if (blockEntity.crushTime >= blockEntity.crushTimeTotal) {
                    blockEntity.crushTime = 0;
                    blockEntity.crushTimeTotal = 200;
                    processRecipe(inputItemStack, outputItemStack);
                    isDirty = true;
                }
            } else {
                blockEntity.crushTime = 0;
            }
        }

        if (isBurning != blockEntity.isBurning()) {
            isDirty = true;
            state = state.with(StoneCrusherBlock.LIT, blockEntity.isBurning());
            world.setBlockState(blockPos, state, 3);
        }

        if (isDirty) {
            markDirty(world, blockPos, state);
        }
    }

    private static void processRecipe(ItemStack inputItemStack, ItemStack outputItemStack) {
        Item outputItem = getOutputItem(inputItemStack);
        if (outputItemStack.isEmpty()) {
            outputItemStack = new ItemStack(outputItem, 1);
        } else {
            outputItemStack.increment(1);
        }
    }

    private static int getFuelTime(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        } else {
            Item item = fuel.getItem();
            return (Integer)AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(item, 0);
        }
    }

    private static boolean canAcceptRecipeOutput(ItemStack inputItemStack, ItemStack ouputItemStack) {
        if (ouputItemStack.isEmpty()) {
            return true;
        }

        if (getOutputItem(inputItemStack) != ouputItemStack.getItem()) {
            return false;
        }

        if (ouputItemStack.getCount() + 1 > ouputItemStack.getMaxCount()){
            return false;
        }

        return true;
    }

    private static boolean isValidRecipe(ItemStack fuelItemStack, ItemStack inputItemStack) {
        return canUseAsFuel(fuelItemStack) && canUseAsInput(inputItemStack);
    }

    private static boolean canUseAsInput(ItemStack inputItemStack) {
        Item item = inputItemStack.getItem();
        if (item == Items.COBBLESTONE || item == Items.STONE || item == Items.GRAVEL) {
            return true;
        } else {
            return false;
        }
    }

    private static Item getOutputItem(ItemStack inputItemStack) {
        Item item = inputItemStack.getItem();
        if (item == Items.COBBLESTONE) {
            return Items.GRAVEL;
        } else if (item == Items.STONE) {
            return Items.COBBLESTONE;
        } else if (item == Items.GRAVEL) {
            return Items.SAND;
        } else {
            return Items.AIR;
        }
    }

    private static boolean canUseAsFuel(ItemStack fuelItemStack) {
        return AbstractFurnaceBlockEntity.canUseAsFuel(fuelItemStack);
    }


    private boolean isBurning() {
        return this.burnTime > 0;
    }
}
