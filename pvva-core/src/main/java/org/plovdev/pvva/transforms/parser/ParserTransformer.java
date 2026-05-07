package org.plovdev.pvva.transforms.parser;

import groovy.lang.GroovyShell;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.parsers.MainParser;

public final class ParserTransformer {
    private ParserTransformer() {
    }

    public static @NonNull MainParser ofParser(String parser) {
        GroovyShell shell = new GroovyShell();
        return new MainParser(shell.parse(parser), parser);
    }

    public static String toParser(@NonNull MainParser parser) {
        return parser.rawScript();
    }
}