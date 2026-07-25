package com.shinoow.abyssalcraft.data.gen;

import com.shinoow.abyssalcraft.platform.DataGenCompat;
import com.shinoow.abyssalcraft.platform.ItemModelGen;

/** Layered item models for the thirty-two legacy ritual charms. */
public final class EnergyItemData extends ItemModelGen {

    private static final String[] FAMILIES = {
        "charm", "cthulhucharm", "hasturcharm", "jzaharcharm",
        "azathothcharm", "nyarlathotepcharm", "yogsothothcharm", "shubniggurathcharm"
    };
    private static final String[] DEITY_TEXTURES = {
        null, "cthulhu", "hastur", "jzahar", "azathoth", "nyarlathotep", "yogsothoth", "shubniggurath"
    };
    private static final String[] AMPLIFIERS = {null, "range", "duration", "power"};

    public EnergyItemData(DataGenCompat.Gen gen) {
        super(gen);
    }

    @Override
    public String getName() {
        return "AbyssalCraft Energy Item Models";
    }

    @Override
    protected void generate() {
        for (int family = 0; family < FAMILIES.length; family++) {
            for (String amplifier : AMPLIFIERS) {
                String id = FAMILIES[family] + (amplifier == null ? "" : "_" + amplifier);
                var model = withExistingParent(id, mcLoc("item/generated"));
                if (family == 0) {
                    model.texture("layer0", modLoc("item/charms/ritualcharm"));
                    if (amplifier != null) {
                        model.texture("layer1", modLoc("item/charms/" + amplifier));
                    }
                } else {
                    model.texture("layer0", modLoc("item/charms/charm"));
                    model.texture("layer1", modLoc("item/charms/" + DEITY_TEXTURES[family]));
                }
            }
        }
    }
}