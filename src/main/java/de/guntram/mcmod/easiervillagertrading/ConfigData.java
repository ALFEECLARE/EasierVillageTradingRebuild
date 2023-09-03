package de.guntram.mcmod.easiervillagertrading;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigData {

    public static final ClientConfig CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static boolean shiftSwapped;
    public static boolean ctrlSwapped;

    static {
        final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static void refreshClient() {
        ctrlSwapped = CLIENT.shiftSwapped.get();
        shiftSwapped = CLIENT.shiftSwapped.get();
    }

    public static class ClientConfig {

        public final ForgeConfigSpec.BooleanValue shiftSwapped;
        public final ForgeConfigSpec.BooleanValue ctrlSwapped;

        ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.push("general");
            shiftSwapped = builder.comment("Make 'trade all' default").translation("easiervillagertrading.config.tt.swapshift").define("shiftSwapped", false);
            ctrlSwapped = builder.comment("Make 'trade Immediatry' default").translation("easiervillagertrading.config.tt.ctrlshift").define("ctrlSwapped", false);
            builder.pop();
        }
    }
}
