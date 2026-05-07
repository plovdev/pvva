package org.plovdev.pvva.utils.vars;

import org.jspecify.annotations.NonNull;

import java.util.NoSuchElementException;

public interface VariableModifier {
    enum Modifier {
        CHAR_AT("CHRAT"),
        TO_UPPER_CASE("TO_UPP"),
        TO_LOWER_CASE("TO_LOW"),
        REPLACE("REPL"),
        SUBSTRING("SSTR");

        public static @NonNull Modifier getByName(String name) {
            for (Modifier modifer : values()) {
                if (modifer.name.equals(name)) {
                    return modifer;
                }
            }
            throw new NoSuchElementException("Modifier not found");
        }

        private final String name;

        Modifier(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    String process(String raw);
}