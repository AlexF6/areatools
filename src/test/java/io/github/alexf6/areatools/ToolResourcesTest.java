package io.github.alexf6.areatools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Cada herramienta debe traer lo que el juego le pide fuera del código: ítem, modelo, textura, receta, nombre en
 * los tres idiomas y su entrada en el tag. Sin esto, lo que falte solo se descubre jugando (ítem sin nombre,
 * textura magenta, receta inexistente).
 */
class ToolResourcesTest {

    private static final List<String> MATERIALS = List.of("copper", "iron", "golden", "diamond", "netherite");
    private static final List<String> KINDS = List.of("hammer", "excavator");
    private static final List<String> LANGS = List.of("en_us", "es_es", "es_ar");
    private static final List<String> ADVANCEMENTS = List.of("hammer", "excavator", "netherite_hammer", "netherite_excavator");

    private static List<String> toolIds() {
        List<String> ids = new ArrayList<>();
        for (String kind : KINDS) for (String material : MATERIALS) ids.add(material + "_" + kind);
        return ids;
    }

    private static InputStream open(String path) {
        return ToolResourcesTest.class.getClassLoader().getResourceAsStream(path);
    }

    private static JsonObject json(String path) throws Exception {
        try (InputStream in = open(path)) {
            assertNotNull(in, "falta " + path);
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    private static void assertExists(String path) throws Exception {
        try (InputStream in = open(path)) {
            assertNotNull(in, "falta " + path);
            assertTrue(in.available() > 0, "vacío: " + path);
        }
    }

    @Test
    void everyToolHasItsFiles() throws Exception {
        for (String id : toolIds()) {
            assertExists("assets/areatools/items/" + id + ".json");
            assertExists("assets/areatools/models/item/" + id + ".json");
            assertExists("assets/areatools/textures/item/" + id + ".png");
            assertExists("data/areatools/recipe/" + id + ".json");
        }
    }

    @Test
    void itemModelAndRecipePointAtTheSameTool() throws Exception {
        for (String id : toolIds()) {
            assertEquals("areatools:item/" + id,
                    json("assets/areatools/items/" + id + ".json").getAsJsonObject("model").get("model").getAsString(), id);
            assertEquals("areatools:item/" + id,
                    json("assets/areatools/models/item/" + id + ".json").getAsJsonObject("textures").get("layer0").getAsString(), id);
            assertEquals("areatools:" + id,
                    json("data/areatools/recipe/" + id + ".json").getAsJsonObject("result").get("id").getAsString(), id);
        }
    }

    @Test
    void tagsListEveryToolOfTheirKind() throws Exception {
        for (String kind : KINDS) {
            Set<String> listed = new HashSet<>();
            JsonArray values = json("data/areatools/tags/item/" + kind + "s.json").getAsJsonArray("values");
            for (JsonElement value : values) listed.add(value.getAsString());
            Set<String> expected = new HashSet<>();
            for (String material : MATERIALS) expected.add("areatools:" + material + "_" + kind);
            assertEquals(expected, listed, "tag de " + kind + "s");
        }
    }

    @Test
    void everyToolAndModeHasANameInEveryLanguage() throws Exception {
        for (String lang : LANGS) {
            JsonObject strings = json("assets/areatools/lang/" + lang + ".json");
            List<String> keys = new ArrayList<>();
            for (String id : toolIds()) keys.add("item.areatools." + id);
            for (String mode : List.of("flat", "wide", "deep")) keys.add("areatools.mode." + mode);
            keys.add("areatools.tooltip.mode");
            for (String key : keys) {
                assertTrue(strings.has(key), lang + " no tiene " + key);
                assertFalse(strings.get(key).getAsString().isBlank(), lang + ": " + key + " vacío");
            }
        }
    }

    @Test
    void everyAdvancementIsTranslatedInEveryLanguage() throws Exception {
        for (String name : ADVANCEMENTS) {
            JsonObject display = json("data/areatools/advancement/" + name + ".json").getAsJsonObject("display");
            String title = display.getAsJsonObject("title").get("translate").getAsString();
            String description = display.getAsJsonObject("description").get("translate").getAsString();
            for (String lang : LANGS) {
                JsonObject strings = json("assets/areatools/lang/" + lang + ".json");
                assertTrue(strings.has(title), lang + " no tiene " + title);
                assertTrue(strings.has(description), lang + " no tiene " + description);
            }
        }
    }

    @Test
    void allLanguagesHaveTheSameKeys() throws Exception {
        Set<String> base = json("assets/areatools/lang/en_us.json").keySet();
        for (String lang : LANGS) {
            Set<String> keys = new HashSet<>(json("assets/areatools/lang/" + lang + ".json").keySet());
            Set<String> missing = new HashSet<>(base);
            missing.removeAll(keys);
            Set<String> extra = new HashSet<>(keys);
            extra.removeAll(base);
            assertTrue(missing.isEmpty(), lang + " no tiene: " + missing);
            assertTrue(extra.isEmpty(), lang + " tiene de más: " + extra);
        }
    }

    @Test
    void theEnglishModDescriptionMatchesFabricModJson() throws Exception {
        String inModJson = json("fabric.mod.json").get("description").getAsString();
        String inLang = json("assets/areatools/lang/en_us.json").get("modmenu.descriptionTranslation.areatools").getAsString();
        assertEquals(inModJson, inLang);
        for (String lang : LANGS) {
            String translated = json("assets/areatools/lang/" + lang + ".json").get("modmenu.descriptionTranslation.areatools").getAsString();
            assertFalse(translated.isBlank(), lang);
        }
    }
}
