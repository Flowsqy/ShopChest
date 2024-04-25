package de.epiceric.shopchest.nms;

import de.epiceric.shopchest.nms.reflection.PlatformImpl;
import de.epiceric.shopchest.nms.reflection.ShopChestDebug;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PlatformLoader {

    private final ShopChestDebug debug;

    public PlatformLoader(ShopChestDebug debug) {
        this.debug = debug;
    }

    public Platform loadPlatform() {
        final String nmsVersion = Utils.getServerVersion();

        Platform platform = getReflectionPlatform(nmsVersion);
        if (platform != null) {
            return platform;
        }
        final String mappingsVersion = getMappingsVersion();
        if (mappingsVersion == null) {
            throw new RuntimeException("Could not retrieve the mappings version. The server version might be too old (" + nmsVersion + ").");
        }
        platform = getSpecificPlatform(mappingsVersion);
        if (platform == null) {
            throw new RuntimeException("Server version not officially supported. Version: '" + nmsVersion + "', Mappings : " + "'" + mappingsVersion + "'");
        }
        return platform;
    }

    private Platform getReflectionPlatform(String nmsVersion) {
        switch (nmsVersion) {
            case "v1_8_R1":
            case "v1_8_R2":
            case "v1_8_R3":
            case "v1_9_R1":
            case "v1_9_R2":
            case "v1_10_R1":
            case "v1_11_R1":
            case "v1_12_R1":
            case "v1_13_R1":
            case "v1_13_R2":
            case "v1_14_R1":
            case "v1_15_R1":
            case "v1_16_R1":
            case "v1_16_R2":
            case "v1_16_R3":
                return (Platform) new PlatformImpl(debug);
            default:
                return null;
        }
    }

    private String getMappingsVersion() {
        try {
            final String craftMagicNumbersClassName = Bukkit.getServer().getClass().getPackage().getName() + ".util.CraftMagicNumbers";
            final Class<?> craftMagicNumbersClass = Class.forName(craftMagicNumbersClassName);
            final Method method = craftMagicNumbersClass.getDeclaredMethod("getMappingsVersion");
            method.setAccessible(true);
            final Field instanceField = craftMagicNumbersClass.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            return (String) method.invoke(instanceField.get(null));
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private Platform getSpecificPlatform(String mappingsVersion) {
        switch (mappingsVersion) {
            case "acd6e6c27e5a0a9440afba70a96c27c9": // 1.17 (v1_17_R1)
                return new de.epiceric.shopchest.nms.v1_17_R1.PlatformImpl();
            case "f0e3dfc7390de285a4693518dd5bd126": // 1.17.1 (v1_17_R1)
                return new de.epiceric.shopchest.nms.v1_17_R2.PlatformImpl();
            case "9e9fe6961a80f3e586c25601590b51ec": // 1.18
            case "20b026e774dbf715e40a0b2afe114792": // 1.18.1 (v1_18_R1)
                return new de.epiceric.shopchest.nms.v1_18_R1.PlatformImpl();
            case "eaeedbff51b16ead3170906872fda334": // 1.18.2 (v1_18_R2)
                return new de.epiceric.shopchest.nms.v1_18_R2.PlatformImpl();
            case "7b9de0da1357e5b251eddde9aa762916": // 1.19
            case "4cc0cc97cac491651bff3af8b124a214": // 1.19.1
            case "69c84c88aeb92ce9fa9525438b93f4fe": // 1.19.2 (v1_19_R1)
                return new de.epiceric.shopchest.nms.v1_19_R1.PlatformImpl();
            case "1afe2ffe8a9d7fc510442a168b3d4338": // 1.19.3 (v1_19_R2)
                return new de.epiceric.shopchest.nms.v1_19_R2.PlatformImpl();
            case "3009edc0fff87fa34680686663bd59df": // 1.19.4 (v1_19_R3)
                return new de.epiceric.shopchest.nms.v1_19_R3.PlatformImpl();
            case "34f399b4f2033891290b7f0700e9e47b": // 1.20
            case "bcf3dcb22ad42792794079f9443df2c0": // 1.20.1 (v1_20_R1)
                return new de.epiceric.shopchest.nms.v1_20_R1.PlatformImpl();
            case "3478a65bfd04b15b431fe107b3617dfc": //1.20.2
                return new de.epiceric.shopchest.nms.v1_20_R2.PlatformImpl();
            case "60a2bb6bf2684dc61c56b90d7c41bddc": //1.20.4 (1.20.3 virtually does not exist)
                return new de.epiceric.shopchest.nms.v1_20_R3.PlatformImpl();
            case"ad1a88fd7eaf2277f2507bf34d7b994c": // 1.20.5
                return new de.epiceric.shopchest.nms.v1_20_R4.PlatformImpl();
            default:
                return null;
        }
    }

}
