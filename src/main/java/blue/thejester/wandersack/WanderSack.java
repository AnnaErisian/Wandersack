package blue.thejester.wandersack;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;

public class WanderSack extends Item {

    static final String ITEMS_TAG = "items";
    static final String TORN_TAG = "torn";

    public WanderSack(Properties props) {
        super(props);
    }

    public void onUseTick(World world, LivingEntity user, ItemStack wandersack, int ticksInUse) {
        // if we're on a %8 usage tick
        if(!world.isClientSide && ticksInUse % 2 == 1) {
    //     get the tag
            CompoundNBT bundleTag = wandersack.getOrCreateTag();
    //     get the list
            ListNBT items = bundleTag.getList(WanderSack.ITEMS_TAG, 10);
            if (items.size() != 0) {
        //     pick X 2-min(6,listsize)
                int count = Math.min(2 + random.nextInt(4), items.size());
        //     make a collection
                ArrayList<CompoundNBT> things = new ArrayList<>();
        //     X times
                for (int i = 0; i < count; i++) {
            //         remove an item from the tag list, add it to the list we made
                    things.add((CompoundNBT) items.remove(random.nextInt(items.size())));
                }
        //     for each item in our list
                things.forEach(itemTag -> {
                    int realCount = (itemTag).getInt("truecount");
            //         pick 2-min(5,realcount)
                    int throwCount = Math.min(2 + random.nextInt(4), realCount);
            //         toss that many items out
                    ItemStack stack = ItemStack.of(itemTag);
                    stack.setCount(throwCount);
                    ItemEntity entityitem = new ItemEntity(world, user.getX(), user.getY() + 0.5, user.getZ(), stack);
                    entityitem.setPickUpDelay(40);
                    entityitem.setDeltaMovement(entityitem.getDeltaMovement().multiply(0, 1, 0));
                    world.addFreshEntity(entityitem);
            //         reduce realcount by that much
                    realCount -= throwCount;
            //         if realcount > 0 put it back in the tag list
                    if (realCount > 0) {
                        itemTag.putInt("truecount", realCount);
                        items.add(itemTag);
                    }
                });
        //     put the tag list back on the tag
                bundleTag.put(ITEMS_TAG, items);
        //     save it to the stack
                wandersack.setTag(bundleTag);
            }
        }
    }

    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (isEmpty(itemstack)) {
            return ActionResult.fail(itemstack);
        } else {
            itemstack.addTagElement(TORN_TAG, ByteNBT.ONE);
            player.startUsingItem(hand);
            return ActionResult.success(itemstack);
        }
    }

    @Override
    public int getUseDuration(ItemStack p_77626_1_) {
        return 72000;
    }


    public static float isTorn(ItemStack itemStack, ClientWorld clientWorld, LivingEntity livingEntity) {
        return isTorn(itemStack) ? 1 : 0;
    }

    public static boolean isTorn(ItemStack itemStack) {
        if(itemStack.getTag() == null) return false;
        return itemStack.getTag().getByte(WanderSack.TORN_TAG) == 1;
    }

    public static float isEmpty(ItemStack itemStack, ClientWorld clientWorld, LivingEntity livingEntity) {
        return isEmpty(itemStack) ? 1 : 0;
    }

    private static boolean isEmpty(ItemStack itemstack) {
        CompoundNBT tag = itemstack.getOrCreateTag();
        if (!tag.contains(ITEMS_TAG)) return true;
        ListNBT items = tag.getList(ITEMS_TAG, 10);
        return items.isEmpty();
    }
}
