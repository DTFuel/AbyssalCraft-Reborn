package com.shinoow.abyssalcraft.platform;

import com.shinoow.abyssalcraft.content.entity.legacy.EntityCatalogInvariant;

//? if forge {
import net.minecraftforge.event.server.ServerStartedEvent;
//?} else {
/*import net.neoforged.neoforge.event.server.ServerStartedEvent;
*///?}

public final class EntityCatalogValidationCompat {

    private EntityCatalogValidationCompat() {}

    public static void attach() {
        EventBuses.game().addListener((ServerStartedEvent event) -> EntityCatalogInvariant.validate());
    }
}
