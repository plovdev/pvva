package org.plovdev.pvva.models.table;

import java.util.Map;

public record EntriesOffsetTable(short tableSize, byte entriesCount, Map<String, TableEntry> entries) {
    public static final int TABLE_HEADER_SIZE = 3;
}