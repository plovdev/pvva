package org.plovdev.pvva.models.configs.httpconfig;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public final class HeadersConfig {
    private static final Random RANDOM = new Random();

    private final boolean random;
    private final List<Map<String, String>> headerSets;

    public HeadersConfig(boolean random, @Nullable List<Map<String, String>> headerSets) {
        this.random = random;
        this.headerSets = headerSets;
    }

    public boolean random() {
        return random;
    }

    public @NonNull Optional<List<Map<String, String>>> headerSets() {
        return Optional.ofNullable(headerSets);
    }

    public @NonNull Optional<Map<String, String>> headerSet() {
        if (headerSets == null || headerSets.isEmpty()) {
            return Optional.empty();
        } else {
            if (random) {
                return Optional.ofNullable(headerSets.get(RANDOM.nextInt(0, headerSets.size())));
            } else {
                return Optional.ofNullable(headerSets.getFirst());
            }
        }
    }

    @Override
    public @NonNull String toString() {
        return "HeadersConfig{" +
                "random=" + random +
                ", headerSets=" + headerSets +
                '}';
    }
}