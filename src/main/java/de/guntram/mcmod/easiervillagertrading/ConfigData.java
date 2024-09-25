package de.guntram.mcmod.easiervillagertrading;

import org.apache.commons.lang3.tuple.Pair;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ConfigData {

    public static ClientConfig CLIENT;
    public static ModConfigSpec CLIENT_SPEC;
    public static boolean shiftSwapped;
    public static boolean ctrlSwapped;

    static {
        final Pair<ClientConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static void refreshClient() {
        ctrlSwapped = CLIENT.ctrlSwapped.get();
        shiftSwapped = CLIENT.shiftSwapped.get();
    }

    public static class ClientConfig {

        public final ModConfigSpec.BooleanValue shiftSwapped;
        public final ModConfigSpec.BooleanValue ctrlSwapped;

        ClientConfig(ModConfigSpec.Builder builder) {
            builder.push("general");
            shiftSwapped = builder.comment("Make 'trade all' default").translation("easiervillagertrading.config.tt.swapshift").define("shiftSwapped", false);
            ctrlSwapped = builder.comment("Make 'trade Immediatry' default").translation("easiervillagertrading.config.tt.ctrlshift").define("ctrlSwapped", false);
            builder.pop();
        }
    }
}
