package org.plovdev.pvva.models;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public final class PluginJson {
    private final String title;
    private final String version;
    private final String description;
    private final String autoUpdateUrl;
    private final String author;
    private final String developerId;
    private final String authorPage;
    private final String licenseUrl;
    private final String homepage;

    public PluginJson(
            @NonNull String title,
            @NonNull String version,

            @Nullable String description,
            @Nullable String autoUpdateUrl,
            @Nullable String author,
            @Nullable String developerId,
            @Nullable String authorPage,
            @Nullable String licenseUrl,
            @Nullable String homepage) {

        this.title = Objects.requireNonNull(title);
        this.version = Objects.requireNonNull(version);
        this.description = description;
        this.autoUpdateUrl = autoUpdateUrl;
        this.author = author;
        this.developerId = developerId;
        this.authorPage = authorPage;
        this.licenseUrl = licenseUrl;
        this.homepage = homepage;
    }

    //===NON NULL===\\
    public @NonNull String title() {
        return title;
    }

    public @NonNull String version() {
        return version;
    }


    //===NULLABLE===\\

    public @NonNull Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public @NonNull Optional<String> autoUpdateUrl() {
        return Optional.ofNullable(autoUpdateUrl);
    }

    public @NonNull Optional<String> author() {
        return Optional.ofNullable(author);
    }

    public @NonNull Optional<String> developerId() {
        return Optional.ofNullable(developerId);
    }

    public @NonNull Optional<String> authorPage() {
        return Optional.ofNullable(authorPage);
    }

    public @NonNull Optional<String> licenseUrl() {
        return Optional.ofNullable(licenseUrl);
    }

    public @NonNull Optional<String> homepage() {
        return Optional.ofNullable(homepage);
    }

    @Override
    public String toString() {
        return "PluginJson{" +
                "title='" + title + '\'' +
                ", version='" + version + '\'' +
                ", description='" + description + '\'' +
                ", autoUpdateUrl='" + autoUpdateUrl + '\'' +
                ", author='" + author + '\'' +
                ", developerId='" + developerId + '\'' +
                ", authorPage='" + authorPage + '\'' +
                ", licenseUrl='" + licenseUrl + '\'' +
                ", homepage='" + homepage + '\'' +
                '}';
    }
}