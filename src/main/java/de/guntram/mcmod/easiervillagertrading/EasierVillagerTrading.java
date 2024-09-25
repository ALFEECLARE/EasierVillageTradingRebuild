package de.guntram.mcmod.easiervillagertrading;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.guntram.mcmod.easiervillagertrading.event.OpenGuiEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(EasierVillagerTrading.MODID)
public class EasierVillagerTrading {

    public static final String MODID = "easiervillagertrading";
    private static final Logger log = LogManager.getLogger(MODID);

    public EasierVillagerTrading(IEventBus modEventBus, ModContainer modContainer) {
    	NeoForge.EVENT_BUS.addListener(OpenGuiEvent::openGui);

    	modEventBus.addListener(this::modConfig);
    	modContainer.registerConfig(ModConfig.Type.CLIENT, ConfigData.CLIENT_SPEC);

    }

    public void modConfig(ModConfigEvent event) {
        if (event.getConfig().getSpec() == ConfigData.CLIENT_SPEC)
            ConfigData.refreshClient();
    }

    public static void log(String message) {
    	if (log == null)
    		return;
    	log.info("[{}] {}", log.getName(), message);
      }
}
