package de.guntram.mcmod.easiervillagertrading.event;

import de.guntram.mcmod.easiervillagertrading.BetterGuiMerchant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;

public class OpenGuiEvent {

    public static void openGui(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof MerchantScreen original) {
            event.setNewScreen(new BetterGuiMerchant(original.getMenu(), Minecraft.getInstance().player.getInventory(), original.getTitle()));
        }
    }
}
