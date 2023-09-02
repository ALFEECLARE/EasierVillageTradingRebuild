package de.guntram.mcmod.easiervillagertrading.event;

import de.guntram.mcmod.easiervillagertrading.BetterGuiMerchant;
import de.guntram.mcmod.easiervillagertrading.EasierVillagerTrading;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT, modid = EasierVillagerTrading.MODID)
public class OpenGuiEvent {

    @SubscribeEvent
    public static void openGui(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof MerchantScreen original) {
            event.setNewScreen(new BetterGuiMerchant(original.getMenu(), Minecraft.getInstance().player.getInventory(), original.getTitle()));
        }
    }
}
