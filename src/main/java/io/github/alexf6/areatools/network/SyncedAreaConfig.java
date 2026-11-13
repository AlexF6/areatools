package io.github.alexf6.areatools.network;

import org.jspecify.annotations.Nullable;

import io.github.alexf6.areatools.AreaToolsConfig;

/**
 * La config que el servidor al que está conectado este cliente le mandó, o la local si no hay conexión. Solo el
 * cliente la escribe (al recibir {@link AreaConfigSyncPayload} y al desconectarse) y solo el cliente la lee (el
 * dibujo del contorno), así que un mundo integrado no comparte estado con su servidor: el servidor siempre usa su
 * propia config.
 */
public final class SyncedAreaConfig {

    private static volatile @Nullable AreaToolsConfig remote;

    /** La config que el servidor dictó, o {@code local} si no hay ninguna. */
    public static AreaToolsConfig effective(AreaToolsConfig local) {
        AreaToolsConfig synced = remote;
        return synced != null ? synced : local;
    }

    public static void set(AreaToolsConfig config) {
        remote = config;
    }

    public static void clear() {
        remote = null;
    }

    private SyncedAreaConfig() {}
}
