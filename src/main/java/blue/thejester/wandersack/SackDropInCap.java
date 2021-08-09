package blue.thejester.wandersack;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import vazkii.arl.util.AbstractDropIn;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This whole class is heavily based on Bundles Plus
 * So is the whole project, but this in particular has a lot of copied code in dropItemIn and getItemsFromBundle.
 * getItemStackFor and getItemStackIndex are verbatim
 * */
public class SackDropInCap extends AbstractDropIn {

    public static final ITag.INamedTag<Item> FORBIDDEN = ItemTags.createOptional(new ResourceLocation(WanderSackMod.MODID, "forbidden"));

    @Override
    public boolean canDropItemIn(PlayerEntity player, ItemStack stack, ItemStack incoming, Slot slot) {
        return !WanderSack.isTorn(stack);
    }

    @Override
    public ItemStack dropItemIn(PlayerEntity player, ItemStack wandersack, ItemStack incoming, Slot slot) {
        if (incoming.getItem().is(FORBIDDEN)) {
            return wandersack;
        }

        CompoundNBT bundleTag = wandersack.getOrCreateTag();
        ListNBT items = bundleTag.getList(WanderSack.ITEMS_TAG, 10);
        CompoundNBT itemStackNbt = new CompoundNBT();
        ItemStack stackFromBundle = getItemStackFor(wandersack, incoming);
        int index = getItemStackIndex(wandersack, stackFromBundle);
        if (!stackFromBundle.isEmpty() && incoming.getMaxStackSize() > 1) {
            stackFromBundle.setCount(stackFromBundle.getCount() + incoming.getCount());
        }
        if (index != -1 && incoming.getMaxStackSize() > 1) {
            int count = stackFromBundle.getCount();
            stackFromBundle.save(itemStackNbt);
            itemStackNbt.putInt("truecount", count);
            items.set(index, itemStackNbt);
        } else {
            int count = incoming.getCount();
            incoming.save(itemStackNbt);
            itemStackNbt.putInt("truecount", count);
            items.add(itemStackNbt);
        }
        bundleTag.put(WanderSack.ITEMS_TAG, items);
        wandersack.setTag(bundleTag);
        incoming.setCount(0);
        return wandersack;
    }

    private static ItemStack getItemStackFor(ItemStack bundle, ItemStack stack) {
        return getItemsFromBundle(bundle).stream().filter(x -> ItemStack.isSame(x, stack)
                && ItemStack.tagMatches(x, stack)).findFirst().orElse(ItemStack.EMPTY);
    }

    private static int getItemStackIndex(ItemStack bundle, ItemStack stack) {
        List<ItemStack> items = getItemsFromBundle(bundle);
        return IntStream.range(0, items.size())
                .filter(i -> stack.sameItem(items.get(i)) && ItemStack.tagMatches(stack, items.get(i)))
                .findFirst().orElse(-1);
    }

    public static List<ItemStack> getItemsFromBundle(ItemStack bundle) {
        CompoundNBT bundleTag = bundle.getOrCreateTag();
        ListNBT items = bundleTag.getList(WanderSack.ITEMS_TAG, 10);
        return items.stream().map(x -> {
            ItemStack stackLoaded = ItemStack.of((CompoundNBT) x);
            stackLoaded.setCount(((CompoundNBT) x).getInt("truecount"));
            return stackLoaded;
        }).collect(Collectors.toList());
    }
}
