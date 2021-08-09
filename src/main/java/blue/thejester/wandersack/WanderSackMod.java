package blue.thejester.wandersack;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(WanderSackMod.MODID)
public class WanderSackMod
{
    // Directly reference a log4j logger.
    static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "wandersack";

    private static final ResourceLocation SACK_CAP = new ResourceLocation(WanderSackMod.MODID, "wandersack_dropin");

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> WANDERSACK = ITEMS.register("wandersack", () -> new WanderSack(new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_TOOLS)));

    public WanderSackMod() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::clientSetup);

        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        forgeBus.addGenericListener(ItemStack.class, WanderSackMod::onAttachCapabilityItem);
    }


    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(WanderSackMod::registerPropertyGetters);
    }

    private static void registerPropertyGetters() {
        ItemModelsProperties.register(WANDERSACK.get(), new ResourceLocation(MODID, "torn"), WanderSack::isTorn);
        ItemModelsProperties.register(WANDERSACK.get(), new ResourceLocation(MODID, "empty"), WanderSack::isEmpty);
    }

    public static void onAttachCapabilityItem(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() instanceof WanderSack) {
            event.addCapability(SACK_CAP, new SackDropInCap());
        }
    }
}
