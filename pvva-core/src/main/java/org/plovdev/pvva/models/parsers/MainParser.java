package org.plovdev.pvva.models.parsers;

import groovy.lang.Script;
import org.jspecify.annotations.NonNull;

public record MainParser(@NonNull Script script, /* For serialize */ @NonNull String rawScript) {
}