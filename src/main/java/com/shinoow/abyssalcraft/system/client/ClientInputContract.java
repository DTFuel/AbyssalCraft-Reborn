package com.shinoow.abyssalcraft.system.client;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Frozen M/N/V/B/G client-input contract and its existing server-bound packet ids. */
public final class ClientInputContract {

    public enum Action {
        STAFF_MODE("staff_mode", 77, 4),
        USE_CAGE("use_cage", 78, 9),
        TABLET_MODE("spirit_tablet_mode", 86, 5),
        TABLET_FILTER("spirit_tablet_filter", 66, 5),
        TABLET_PATH("spirit_tablet_path", 71, 5);

        private final String id;
        private final int defaultKey;
        private final int packetId;

        Action(String id, int defaultKey, int packetId) {
            this.id = id;
            this.defaultKey = defaultKey;
            this.packetId = packetId;
        }

        public String id() {
            return id;
        }

        public int defaultKey() {
            return defaultKey;
        }

        public int packetId() {
            return packetId;
        }

        public String translationKey() {
            return "key.abyssalcraft." + id;
        }
    }

    public static final String CATEGORY = "key.categories.abyssalcraft";
    public static final List<Action> ACTIONS = List.of(Action.values());

    private ClientInputContract() {}

    public static void validate() {
        require(ACTIONS.size() == 5, "client input action count changed");
        Set<String> ids = new HashSet<>();
        Set<Integer> keys = new HashSet<>();
        for (Action action : ACTIONS) {
            require(ids.add(action.id()), "duplicate client input id " + action.id());
            require(keys.add(action.defaultKey()), "duplicate default client key " + action.defaultKey());
        }
        require(keys.equals(Set.of(77, 78, 86, 66, 71)), "M/N/V/B/G default keys changed");
        require(Arrays.equals(ACTIONS.stream().mapToInt(Action::packetId).toArray(),
            new int[] {4, 9, 5, 5, 5}), "client input packet mapping changed");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}