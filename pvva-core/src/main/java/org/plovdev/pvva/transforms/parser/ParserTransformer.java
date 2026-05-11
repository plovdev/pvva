package org.plovdev.pvva.transforms.parser;

import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.parsers.MainParser;

public final class ParserTransformer {
    private ParserTransformer() {
    }

    public static @NonNull MainParser ofParser(String parser) {
        return new MainParser(parser);
    }

    public static String toParser(@NonNull MainParser parser) {
        return parser.rawScript();
    }
}