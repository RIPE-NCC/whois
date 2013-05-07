package net.ripe.db.whois.common.support.database.diff;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;

public class DatabaseDiff {
    private final Database fromDatabase;
    private final Database toDatabase;
    private final Database added;
    private final Database identical;
    private final Database modified;
    private final Database removed;

    public DatabaseDiff(final Database fromDatabase, final Database toDatabase) {
        this.fromDatabase = fromDatabase;
        this.toDatabase = toDatabase;

        final List<Table> addedTables = Lists.newArrayList();
        final List<Table> identicalTables = Lists.newArrayList();
        final List<Table> modifiedTables = Lists.newArrayList();
        final List<Table> removedTables = Lists.newArrayList();

        for (final String tableName : Sets.newTreeSet(fromDatabase.getTableNames())) {
            final Table fromTable = fromDatabase.getTable(tableName);
            final Table toTable = toDatabase.getTable(tableName);

            if (fromTable.isEmpty() && toTable.isEmpty()) continue;

            final Rows identicalRows = new Rows();
            final Rows modifiedRows = new Rows();
            final Rows removedRows = new Rows();
            final Rows addedRows = toDatabase.find(tableName);

            final Rows fromRows = fromDatabase.find(tableName);

            for (final Row fromRow : fromRows) {
                // Search by tables pkey values
                final Rows toRows = toTable.find(fromRow);
                if (toRows.isEmpty()) {
                    // Deleted
                    removedRows.add(fromRow);
                    continue;
                }

                // [EB]: this only happens when a table fails to specify a primary key column
                if (toRows.size() > 1) {
                    throw new IllegalStateException("This cannot happen unless the schema is broken :(");
                }

                // Now look for the *exact* match in the 'to' Rows
                final Rows record = toRows.find(fromRow);
                if (!record.isEmpty()) {
                    // Identical
                    addedRows.remove(record.get(0));
                    identicalRows.add(record.get(0));
                } else {
                    // Changed
                    final Row updatedRow = toTable.get(toRows.get(0));
                    final Row row = toTable.rowDiff(fromRow, updatedRow);

                    addedRows.remove(updatedRow);
                    modifiedRows.add(row);
                }
            }

            if (!addedRows.isEmpty()) addedTables.add(new Table(fromTable, addedRows));
            if (!modifiedRows.isEmpty()) modifiedTables.add(new Table(fromTable, modifiedRows));
            if (!removedRows.isEmpty()) removedTables.add(new Table(fromTable, removedRows));
            if (!identicalRows.isEmpty()) identicalTables.add(new Table(fromTable, identicalRows));
        }

        added = new Database(addedTables);
        modified = new Database(modifiedTables);
        removed = new Database(removedTables);
        identical = new Database(identicalTables);
    }

    public Database getFromDatabase() {
        return fromDatabase;
    }

    public Database getToDatabase() {
        return toDatabase;
    }

    public Database getAdded() {
        return added;
    }

    public Database getIdentical() {
        return identical;
    }

    public Database getModified() {
        return modified;
    }

    public Database getRemoved() {
        return removed;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Added:\n").append(added).append('\n');
        builder.append("Modified:\n").append(modified).append('\n');
        builder.append("Removed:\n").append(removed).append('\n');
        builder.append("Identical:\n").append(identical).append('\n');
        return builder.toString();
    }
}
