module pvva.core {
    requires com.google.gson;
    requires org.jetbrains.annotations;
    requires org.jspecify;
    requires org.slf4j;

    exports org.plovdev.pvva.models.chunks;
    exports org.plovdev.pvva.models.configs.httpconfig;
    exports org.plovdev.pvva.models.configs.resourceconfig;
    exports org.plovdev.pvva.models.parsers;
    exports org.plovdev.pvva.models.table;
    exports org.plovdev.pvva.models;

    exports org.plovdev.pvva.read;
    exports org.plovdev.pvva.transforms.parser;
    exports org.plovdev.pvva.transforms;

    exports org.plovdev.pvva.utils.vars;
    exports org.plovdev.pvva.utils;

    exports org.plovdev.pvva.write;
    exports org.plovdev.pvva.plugins;

    provides java.net.spi.URLStreamHandlerProvider with org.plovdev.pvva.url.PVVAProtocolProvider;
}