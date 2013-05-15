package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategies;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.AbstractDaoTest;
import net.ripe.db.whois.common.support.database.diff.Database;
import net.ripe.db.whois.common.support.database.diff.DatabaseDiff;
import net.ripe.db.whois.common.support.database.diff.Row;
import net.ripe.db.whois.common.support.database.diff.Rows;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.*;

import static net.ripe.db.whois.common.support.database.diff.Rows.with;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class JdbcRpslObjectUpdateDaoTest extends AbstractDaoTest {
    @Autowired RpslObjectUpdateDao subject;

    @Before
    public void setup() {
        sourceContext.setCurrentSourceToWhoisMaster();
    }

    @After
    public void cleanup() {
        sourceContext.removeCurrentSource();
    }

    @Test
    public void delete_basicObject() {
        final RpslObjectUpdateInfo created = subject.createObject(makeObject(ObjectType.MNTNER, "TEST"));
        Database before = new Database(whoisTemplate);

        subject.deleteObject(created.getObjectId(), created.getKey());
        final DatabaseDiff diff = Database.diff(before, new Database(whoisTemplate));

        // identical
        assertThat(diff.getIdentical().getAll(), hasSize(1));

        // removed
        assertThat(diff.getRemoved().getAll(), hasSize(2));
        final Row removedRow = diff.getRemoved().get("last",
                with("object_id", created.getObjectId()),
                with("sequence_id", created.getSequenceId()));
        diff.getRemoved().get("mntner",
                with("object_id", created.getObjectId()),
                with("mntner", "TEST"));

        // added
        final Database added = diff.getAdded();
        assertThat(added.getAll(), hasSize(3));

        // ensure removed row is moved to history
        added.find("history").get(removedRow);
        added.get("last",
                with("object_id", created.getObjectId()),
                with("sequence_id", 0),
                with("object_type", ObjectTypeIds.getId(ObjectType.MNTNER)),
                with("object", new byte[]{}),
                with("pkey", "TEST"));
        added.get("serials",
                with("serial_id", greaterThan(0)),
                with("object_id", created.getObjectId()),
                with("sequence_id", 2),
                with("atlast", 0),
                with("operation", Operation.DELETE.getCode())
        );

        // modified
        assertThat(diff.getModified().getAll(), hasSize(1));
        diff.getModified().get("serials",
                with("serial_id", greaterThan(0)),
                with("atlast", 0));
    }

    @Test
    public void undelete_basicObject() {
        final RpslObject mntnerObject = makeObject(ObjectType.MNTNER, "TEST");
        final RpslObjectUpdateInfo created = subject.createObject(mntnerObject);
        final RpslObjectUpdateInfo deleted = subject.deleteObject(created.getObjectId(), created.getKey());

        final Database before = new Database(whoisTemplate);

        subject.undeleteObject(deleted.getObjectId());

        final Database after = new Database(whoisTemplate);
        final DatabaseDiff diff = Database.diff(before, after);

        // identical
        assertThat(diff.getIdentical().getAll(), hasSize(4));
        assertThat(diff.getIdentical().find("history"), hasSize(1));
        assertThat(diff.getIdentical().find("mntner"), hasSize(1));

        final Rows serialsRows = diff.getIdentical().find("serials");
        assertThat(serialsRows, hasSize(2));
        assertThat(serialsRows.get(with("sequence_id", 1)).getInt("operation"), is(1));
        assertThat(serialsRows.get(with("sequence_id", 2)).getInt("operation"), is(2));

        // removed
        assertThat(diff.getRemoved().getAll(), hasSize(1));
        final Rows removedRows = diff.getRemoved().find("last", with("object_id", created.getObjectId()), with("sequence_id", 0));
        assertThat(removedRows, hasSize(1));

        // modified
        assertThat(diff.getModified().getAll(), hasSize(0));

        // added
        final Database added = diff.getAdded();
        assertThat(added.getAll(), hasSize(3));

        added.get("last",
                with("object_id", created.getObjectId()),
                with("sequence_id", 3),
                with("object_type", ObjectTypeIds.getId(ObjectType.MNTNER)),
                with("object", mntnerObject.toByteArray()),
                with("pkey", "TEST"));

        added.get("serials",
                with("serial_id", greaterThan(0)),
                with("object_id", created.getObjectId()),
                with("sequence_id", 3),
                with("atlast", 1),
                with("operation", Operation.UPDATE.getCode()));

        added.get("mntner",
                with("object_id", created.getObjectId()),
                with("mntner", "TEST"));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void undelete_basicObject_not_deleted() {
        final RpslObject mntnerObject = makeObject(ObjectType.MNTNER, "TEST");
        final RpslObjectUpdateInfo created = subject.createObject(mntnerObject);

        subject.undeleteObject(created.getObjectId());
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void undelete_basicObject_twice() {
        final RpslObject mntnerObject = makeObject(ObjectType.MNTNER, "TEST");
        final RpslObjectUpdateInfo created = subject.createObject(mntnerObject);
        final RpslObjectUpdateInfo deleted = subject.deleteObject(created.getObjectId(), created.getKey());

        subject.undeleteObject(deleted.getObjectId());
        subject.undeleteObject(deleted.getObjectId());
    }

    @Test
    public void delete_updated_object() {
        final RpslObjectUpdateInfo created = subject.createObject(makeObject(ObjectType.MNTNER, "TEST"));
        final RpslObject rpslObject = makeObject(ObjectType.MNTNER, created.getKey(), new RpslAttribute(AttributeType.REMARKS, "updated"));

        final RpslObjectUpdateInfo updated = subject.updateObject(created.getObjectId(), rpslObject);
        Database before = new Database(whoisTemplate);

        subject.deleteObject(created.getObjectId(), created.getKey());
        final DatabaseDiff diff = Database.diff(before, new Database(whoisTemplate));

        // identical
        assertThat(diff.getIdentical().getAll(), hasSize(3));
        diff.getIdentical().get("history",
                with("object_id", created.getObjectId()),
                with("sequence_id", created.getSequenceId()));
        diff.getIdentical().get("serials",
                with("object_id", created.getObjectId()),
                with("sequence_id", created.getSequenceId()));

        // removed
        assertThat(diff.getRemoved().getAll(), hasSize(2));
        diff.getRemoved().get("last",
                with("object_id", updated.getObjectId()),
                with("sequence_id", updated.getSequenceId()));
        diff.getRemoved().get("mntner",
                with("object_id", updated.getObjectId()),
                with("mntner", updated.getKey()));

        // added
        final Database added = diff.getAdded();
        assertThat(added.getAll(), hasSize(3));

        // modified
        assertThat(diff.getModified().getAll(), hasSize(1));
        diff.getModified().get("serials",
                with("serial_id", greaterThan(0)),
                with("atlast", 0));
    }

    @Test
    public void delete_one_basic_object_of_three() {
        final RpslObjectUpdateInfo first = subject.createObject(makeObject(ObjectType.MNTNER, "FIRST"));
        final RpslObjectUpdateInfo second = subject.createObject(makeObject(ObjectType.MNTNER, "SECOND"));
        final RpslObjectUpdateInfo third = subject.createObject(makeObject(ObjectType.MNTNER, "THIRD"));

        Database before = new Database(whoisTemplate);

        subject.deleteObject(second.getObjectId(), second.getKey());
        final DatabaseDiff diff = Database.diff(before, new Database(whoisTemplate));

        // identical
        assertThat(diff.getIdentical().getAll(), hasSize(7));
        diff.getIdentical().get("last",
                with("object_id", first.getObjectId()));
        diff.getIdentical().get("last",
                with("object_id", third.getObjectId()));
        diff.getIdentical().get("mntner",
                with("mntner", first.getKey()));
        diff.getIdentical().get("mntner",
                with("mntner", third.getKey()));
        diff.getIdentical().get("serials",
                with("object_id", first.getObjectId()));
        diff.getIdentical().get("serials",
                with("object_id", third.getObjectId()));

        // removed
        assertThat(diff.getRemoved().getAll(), hasSize(2));
        diff.getRemoved().get("last",
                with("object_id", second.getObjectId()));
        diff.getRemoved().get("mntner",
                with("object_id", second.getObjectId()),
                with("mntner", second.getKey()));

        // added
        final Database added = diff.getAdded();
        assertThat(added.getAll(), hasSize(3));

        // modified
        assertThat(diff.getModified().getAll(), hasSize(1));
        diff.getModified().get("serials",
                with("serial_id", greaterThan(0)),
                with("atlast", 0));
    }

    @Test(expected = IllegalStateException.class)
    public void create_object_twice() {
        final RpslObjectUpdateInfo person = subject.createObject(new RpslObject(1, ImmutableList.of(new RpslAttribute("person", "first person name"), new RpslAttribute("nic-hdl", "P1"))));
        assertThat(person.getKey(), is("P1"));

        subject.createObject(new RpslObject(2, ImmutableList.of(new RpslAttribute("person", "first person name"), new RpslAttribute("nic-hdl", "P1"))));
    }

    @Test
    public void create_object_after_delete() {
        final RpslObjectUpdateInfo create = subject.createObject(new RpslObject(1, ImmutableList.of(new RpslAttribute("person", "first person name"), new RpslAttribute("nic-hdl", "P1"))));
        assertThat(create.getKey(), is("P1"));

        final RpslObjectUpdateInfo delete = subject.deleteObject(create.getObjectId(), create.getKey());
        assertThat(delete.getKey(), is("P1"));

        final RpslObjectUpdateInfo create2 = subject.createObject(new RpslObject(2, ImmutableList.of(new RpslAttribute("person", "first person name"), new RpslAttribute("nic-hdl", "P1"))));
        assertThat(create2.getKey(), is("P1"));
    }

    @Test
    public void create_objects_different_types_same_pkey() {
        final RpslObjectUpdateInfo person = subject.createObject(new RpslObject(1, ImmutableList.of(new RpslAttribute("person", "first person name"), new RpslAttribute("nic-hdl", "P1"))));
        assertThat(person.getKey(), is("P1"));

        final RpslObjectUpdateInfo role = subject.createObject(new RpslObject(2, ImmutableList.of(new RpslAttribute("role", "first role name"), new RpslAttribute("nic-hdl", "P1"))));
        assertThat(role.getKey(), is("P1"));
    }

    @Test
    public void create_and_delete_person_role_names() {
        final Database empty = new Database(whoisTemplate);

        final RpslObjectUpdateInfo first_person = subject.createObject(new RpslObject(1, ImmutableList.of(new RpslAttribute("person", "first person name"), new RpslAttribute("nic-hdl", "P1"))));
        final RpslObjectUpdateInfo second_person = subject.createObject(new RpslObject(2, ImmutableList.of(new RpslAttribute("person", "second person name"), new RpslAttribute("nic-hdl", "P2"))));
        final RpslObjectUpdateInfo first_role = subject.createObject(new RpslObject(3, ImmutableList.of(new RpslAttribute("role", "first role name"), new RpslAttribute("nic-hdl", "R1"))));
        final RpslObjectUpdateInfo second_role = subject.createObject(new RpslObject(4, ImmutableList.of(new RpslAttribute("role", "second role name"), new RpslAttribute("nic-hdl", "R2"))));

        final Database created = new Database(whoisTemplate);
        final DatabaseDiff diff = Database.diff(empty, created);
        final Database added = diff.getAdded();

        assertThat(added.getTable("names"), hasSize(12));
        assertThat(added.find("names", with("name", "first"), with("object_type", ObjectTypeIds.getId(ObjectType.PERSON))), hasSize(1));
        assertThat(added.find("names", with("name", "person"), with("object_type", ObjectTypeIds.getId(ObjectType.PERSON))), hasSize(2));
        assertThat(added.find("names", with("name", "person"), with("object_type", ObjectTypeIds.getId(ObjectType.ROLE))), hasSize(0));
        assertThat(added.find("names", with("name", "role"), with("object_type", ObjectTypeIds.getId(ObjectType.ROLE))), hasSize(2));
        assertThat(added.find("names", with("name", "name")), hasSize(4));
        assertThat(added.find("names", with("name", "none")), hasSize(0));
        assertThat(added.find("names", with("name", "second")), hasSize(2));

        // test lookup
        final List<RpslObjectInfo> personIndex = IndexStrategies.get(AttributeType.PERSON).findInIndex(whoisTemplate, "name");
        assertThat(personIndex.size(), is(2));
        assertConsistsOfObjectIds(personIndex, new int[]{1, 2});

        final List<RpslObjectInfo> person2Index = IndexStrategies.get(AttributeType.PERSON).findInIndex(whoisTemplate, "second name");
        assertThat(person2Index.size(), is(1));
        assertConsistsOfObjectIds(person2Index, new int[]{2});

        final List<RpslObjectInfo> roleIndex = IndexStrategies.get(AttributeType.ROLE).findInIndex(whoisTemplate, "second");
        assertThat(roleIndex.size(), is(1));
        assertConsistsOfObjectIds(roleIndex, new int[]{4});

        final List<RpslObjectInfo> noneIndex = IndexStrategies.get(AttributeType.ROLE).findInIndex(whoisTemplate, "person");
        assertThat(noneIndex.size(), is(0));

        // now delete the objects
        assertThat(subject.deleteObject(first_person.getObjectId(), first_person.getKey()).getObjectId(), greaterThan(0));
        assertThat(subject.deleteObject(second_person.getObjectId(), second_person.getKey()).getObjectId(), greaterThan(0));
        assertThat(subject.deleteObject(first_role.getObjectId(), first_role.getKey()).getObjectId(), greaterThan(0));
        assertThat(subject.deleteObject(second_role.getObjectId(), second_role.getKey()).getObjectId(), greaterThan(0));

        final Database deleted = new Database(whoisTemplate);
        assertThat(deleted.getTable("names"), hasSize(0));
    }

    private void assertConsistsOfObjectIds(List<RpslObjectInfo> foundRpslObjects, int[] expectedObjectIds) {
        for (RpslObjectInfo rpslObjectInfo : foundRpslObjects) {
            if (Arrays.binarySearch(expectedObjectIds, rpslObjectInfo.getObjectId()) < 0) {
                fail("Found objectId " + rpslObjectInfo.getObjectId() + ", while expected one of: " + Arrays.toString(expectedObjectIds));
            }
        }
    }

    @Test
    public void create_organisation_names() {
        final Database empty = new Database(whoisTemplate);

        final RpslObjectUpdateInfo first_org = subject.createObject(new RpslObject(1, ImmutableList.of(new RpslAttribute("organisation", "O1"), new RpslAttribute("org-name", "first organisation name"))));
        final RpslObjectUpdateInfo second_org = subject.createObject(new RpslObject(2, ImmutableList.of(new RpslAttribute("organisation", "O2"), new RpslAttribute("org-name", "second organisation name"))));

        final Database created = new Database(whoisTemplate);

        final DatabaseDiff diff = Database.diff(empty, created);

        final Database added = diff.getAdded();
        assertThat(added.getTable("org_name"), hasSize(6));
        assertThat(added.find("org_name", with("name", "first")), hasSize(1));
        assertThat(added.find("org_name", with("name", "organisation")), hasSize(2));
        assertThat(added.find("org_name", with("name", "name")), hasSize(2));
        assertThat(added.find("org_name", with("name", "none")), hasSize(0));
        assertThat(added.find("org_name", with("name", "second")), hasSize(1));

        // test lookup
        final List<RpslObjectInfo> orgIndex = IndexStrategies.get(AttributeType.ORG_NAME).findInIndex(whoisTemplate, "first");
        assertThat(orgIndex.size(), is(1));
        assertConsistsOfObjectIds(orgIndex, new int[]{1});

        final List<RpslObjectInfo> org2Index = IndexStrategies.get(AttributeType.ORG_NAME).findInIndex(whoisTemplate, "name");
        assertThat(org2Index.size(), is(2));
        assertConsistsOfObjectIds(org2Index, new int[]{1, 2});

        final List<RpslObjectInfo> noneIndex = IndexStrategies.get(AttributeType.ORG_NAME).findInIndex(whoisTemplate, "bunny");
        assertThat(noneIndex.size(), is(0));

        // now delete the objects
        assertThat(subject.deleteObject(first_org.getObjectId(), first_org.getKey()).getObjectId(), greaterThan(0));
        assertThat(subject.deleteObject(second_org.getObjectId(), second_org.getKey()).getObjectId(), greaterThan(0));

        final Database deleted = new Database(whoisTemplate);
        assertThat(deleted.getTable("org_name"), hasSize(0));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void delete_nonexistant_object() {
        subject.deleteObject(999, "");
    }

    @Test
    public void create_basicObject() {
        // Only these objects are supported right now
        final HashSet<ObjectType> types = Sets.newHashSet(ObjectType.MNTNER, ObjectType.ORGANISATION, ObjectType.ROLE, ObjectType.PERSON,   // 1st batch
                ObjectType.INET6NUM, ObjectType.INETNUM);       // 2nd batch

        for (final ObjectType objectType : types) {
            assertCreate_simple(objectType);
        }
    }

    private void assertCreate_simple(final ObjectType objectType) {
        final String info = objectType.toString();
        truncateTables(new Database(whoisTemplate).getTableNames());

        final Database before = new Database(whoisTemplate);

        String pkey = "PKEY";
        switch (objectType) {
            case AS_BLOCK:
                pkey = "AS1 - AS10";
                break;
            case INET6NUM:
            case ROUTE6:
                pkey = "2001::/32";
                break;
            case INETNUM:
            case ROUTE:
                pkey = "10.0.0.0 - 10.0.0.255";
                break;
        }

        final RpslObject rpslObject = makeObject(objectType, pkey);
        final RpslObjectUpdateInfo created = subject.createObject(rpslObject);

        final DatabaseDiff diff = new DatabaseDiff(before, new Database(whoisTemplate));

        assertThat(created.getObjectId(), greaterThan(0));

        // Nothing except 3 added records
        assertThat(info, diff.getIdentical().getAll(), hasSize(0));
        assertThat(info, diff.getModified().getAll(), hasSize(0));
        assertThat(info, diff.getRemoved().getAll(), hasSize(0));
        final Database added = diff.getAdded();

        // test if last+history+leaf+main tables are updated correctly
        // we exclude special tables at this stage; they need separate unit tests
        assertThat(info, added.getAllButTables("names", "org_names"), hasSize(3));

        // Last
        final int objectTypeId = ObjectTypeIds.getId(objectType);
        assertThat(added.getTable("last"), hasSize(1));

        // We do not have to check: thread_id, serial, prev_serial
        added.get("last",
                with("object_id", created.getObjectId()),
                with("sequence_id", 1),
                with("pkey", created.getKey()),
                with("timestamp", lessThan(JdbcRpslObjectOperations.now(dateTimeProvider) + 5)),
                with("object_type", objectTypeId),
                with("object", rpslObject.toByteArray())
        );

        // Serials
        assertThat(added.getTable("serials"), hasSize(1));

        // We do not have to check: thread_id
        added.get("serials",
                with("serial_id", greaterThan(0)),
                with("object_id", created.getObjectId()),
                with("sequence_id", 1),
                with("atlast", 1),
                with("operation", Operation.UPDATE.getCode())
        );

        // 'lookupTable'
        final String tableName = getTableForObjectType(objectType);

        assertThat(added.getTable(tableName), hasSize(1));

        // We do not have to check: thread_id
        final Row lookupRow = added.get(tableName,
                with("object_id", created.getObjectId())
        );

        if (lookupRow.containsKey("object_type")) {
            assertThat(lookupRow.getInt("object_type"), is(objectTypeId));
        }
    }

    private void truncateTables(Set<String> tables) {
        for (String table : tables) {
            whoisTemplate.execute(String.format("TRUNCATE %s", table));
        }
    }

    @Test
    public void update_addAttribute() {
        final RpslObjectUpdateInfo referenced = subject.createObject(makeObject(ObjectType.MNTNER, "MNT-BY"));

        final RpslObjectUpdateInfo created = subject.createObject(makeObject(ObjectType.MNTNER, "TEST"));
        final Database oldDb = new Database(whoisTemplate);

        final AttributeType addedAttributeType = AttributeType.MNT_BY;
        final RpslObject rpslObject = makeObject(ObjectType.MNTNER, created.getKey(), new RpslAttribute(addedAttributeType, referenced.getKey()));
        final RpslObjectUpdateInfo updated = subject.updateObject(created.getObjectId(), rpslObject);

        final DatabaseDiff diff = new DatabaseDiff(oldDb, new Database(whoisTemplate));

        assertThat(created.getSequenceId() + 1, is(updated.getSequenceId()));

        // Objects not modified
        final Database identical = diff.getIdentical();
        assertThat("Identical", identical.getAll(), hasSize(5));
        assertThat(identical.getTable("last"), hasSize(1));
        assertThat(identical.getTable("mntner"), hasSize(3));
        assertThat(identical.getTable("serials"), hasSize(1));

        // Objects modified
        final Database modified = diff.getModified();
        assertThat("Modified", modified.getAll(), hasSize(1));
        final Row newSerial = modified.get("serials");

        diff.getFromDatabase().get("serials",
                with("serial_id", newSerial.get("serial_id")),
                // The new row should say it has moved to history
                with("atlast", not(newSerial.get("atlast"))),
                with("atlast", 1)
        );

        // Objects removed
        final Database removed = diff.getRemoved();
        assertThat("Removed", removed.getAll(), hasSize(1));
        // The last record moved to the history
        diff.getAdded().getTable("history").get(removed.get("last"));

        // Added records -- we already accounted for the history
        final Database added = diff.getAdded();
        assertThat("Added", added.getAll(), hasSize(4));
        assertThat(added.getTable("history"), hasSize(1));

        // Accounted for during another test
        assertThat(added.getTable(getTableForAttributeType(addedAttributeType)), hasSize(1));
        assertThat(added.getTable("serials"), hasSize(1));

        // We just need to check the last record, ensure the sequenceId is correct
        assertThat(added.getTable("last"), hasSize(1));
        added.get("last",
                with("object_id", updated.getObjectId()),
                with("sequence_id", updated.getSequenceId()),
                with("object", rpslObject.toByteArray()),
                with("pkey", updated.getKey())
        );
    }

    @Test
    public void update_removeAttribute_Main() {
        final AttributeType removedAttributeType = AttributeType.UPD_TO;
        final String removedKey = "UPD-TO";
        final RpslObject rpslObject = makeObject(ObjectType.MNTNER, "TEST", new RpslAttribute(removedAttributeType, removedKey));

        final RpslObjectUpdateInfo created = subject.createObject(rpslObject);

        final Database oldDb = new Database(whoisTemplate);

        final RpslObjectUpdateInfo updated = subject.updateObject(created.getObjectId(), makeObject(ObjectType.MNTNER, created.getKey()));

        final DatabaseDiff diff = new DatabaseDiff(oldDb, new Database(whoisTemplate));

        // Objects not modified
        final Database identical = diff.getIdentical();
        assertThat("Identical", identical.getAll(), hasSize(2));
        assertThat(identical.getTable("mntner"), hasSize(2));

        // Modified
        final Database modified = diff.getModified();
        assertThat("Modified", modified.getAll(), hasSize(1));
        assertThat(modified.find("serials"), hasSize(1)); // Validated in an earlier test

        // Added
        final Database added = diff.getAdded();
        assertThat("Added", added.getAll(), hasSize(3));
        assertThat(added.find("history"), hasSize(1)); // Validated in an earlier test
        assertThat(added.find("last"), hasSize(1)); // Validated in an earlier test
        assertThat(added.find("serials"), hasSize(1)); // Validated in an earlier test

        // Removed
        final Database removed = diff.getRemoved();
        assertThat("Removed", removed.getAll(), hasSize(2));
        assertThat(removed.find("last"), hasSize(1)); // Validated in an earlier test

        removed.get(getTableForAttributeType(removedAttributeType),
                with("object_id", updated.getObjectId()),
                with(getColumnForAttributeType(removedAttributeType), removedKey)
        );
    }

    @Test
    public void update_removeAttribute_Leaf() {
        final RpslObjectUpdateInfo referenced = subject.createObject(makeObject(ObjectType.MNTNER, "MNT-BY"));

        final AttributeType removedAttributeType = AttributeType.MNT_BY;
        final RpslObject rpslObject = makeObject(ObjectType.MNTNER, "TEST", new RpslAttribute(removedAttributeType, referenced.getKey()));

        final RpslObjectUpdateInfo created = subject.createObject(rpslObject);

        final Database oldDb = new Database(whoisTemplate);

        final RpslObjectUpdateInfo updated = subject.updateObject(created.getObjectId(), makeObject(ObjectType.MNTNER, created.getKey()));

        final DatabaseDiff diff = new DatabaseDiff(oldDb, new Database(whoisTemplate));

        // Objects not modified
        final Database identical = diff.getIdentical();
        assertThat("Identical", identical.getAll(), hasSize(5));
        assertThat(identical.getTable("last"), hasSize(1));
        assertThat(identical.getTable("mntner"), hasSize(3));
        assertThat(identical.getTable("serials"), hasSize(1));

        // Modified
        final Database modified = diff.getModified();
        assertThat("Modified", modified.getAll(), hasSize(1));
        assertThat(modified.find("serials"), hasSize(1)); // Validated in an earlier test

        // Added
        final Database added = diff.getAdded();
        assertThat("Added", added.getAll(), hasSize(3));
        assertThat(added.find("history"), hasSize(1)); // Validated in an earlier test
        assertThat(added.find("last"), hasSize(1)); // Validated in an earlier test
        assertThat(added.find("serials"), hasSize(1)); // Validated in an earlier test

        // Removed
        final Database removed = diff.getRemoved();
        assertThat("Removed", removed.getAll(), hasSize(2));
        assertThat(removed.find("last"), hasSize(1)); // Validated in an earlier test

        removed.get(getTableForAttributeType(removedAttributeType),
                with("object_id", updated.getObjectId()),
                with(getColumnForAttributeType(removedAttributeType), referenced.getObjectId()),
                with("object_type", ObjectTypeIds.getId(referenced.getObjectType()))
        );
    }

    @Test
    public void attributes_complexExample() {
        final RpslObjectUpdateInfo objectA = subject.createObject(makeObject(ObjectType.PERSON, "A"));
        final RpslObjectUpdateInfo objectB = subject.createObject(makeObject(ObjectType.ROLE, "B"));
        final RpslObjectUpdateInfo objectC = subject.createObject(makeObject(ObjectType.PERSON, "C"));
        final RpslObjectUpdateInfo objectD = subject.createObject(makeObject(ObjectType.ROLE, "D"));

        final String pkey = "TEST";
        final AttributeType changedAttributeType = AttributeType.ADMIN_C;

        final RpslObjectUpdateInfo created = subject.createObject(makeObject(ObjectType.MNTNER, pkey));

        final Database before = new Database(whoisTemplate);

        // ----- -> A + B
        subject.updateObject(created.getObjectId(), makeObject(ObjectType.MNTNER, pkey,
                new RpslAttribute(changedAttributeType, "A"),
                new RpslAttribute(changedAttributeType, "B")
        ));

        DatabaseDiff diff = Database.diff(before, new Database(whoisTemplate));

        // Identical
        Database identical = diff.getIdentical();
        assertThat("Identical", identical.getAll(), hasSize(18));
        assertThat(identical.find("last"), hasSize(4));
        assertThat(identical.find("mntner"), hasSize(2));
        assertThat(identical.find("person_role"), hasSize(4));
        assertThat(identical.find("serials"), hasSize(4));
        assertThat(identical.find("names"), hasSize(4));

        // Removed
        Database removed = diff.getRemoved();
        assertThat("Removed", removed.getAll(), hasSize(1));
        assertThat(removed.find("last"), hasSize(1));

        // Modified
        Database modified = diff.getModified();
        assertThat("Modified", modified.getAll(), hasSize(1));
        assertThat(modified.find("serials"), hasSize(1));

        // Added
        Database added = diff.getAdded();
        assertThat("Added", added.getAll(), hasSize(5));
        assertThat(added.find("serials"), hasSize(1));
        assertThat(added.find("last"), hasSize(1));
        assertThat(added.find("history"), hasSize(1));

        final String admin_c = getTableForAttributeType(AttributeType.ADMIN_C);

        assertThat(added.find(admin_c), hasSize(2));

        // This is what we care for
        Row rowA = added.get(admin_c,
                with("pe_ro_id", objectA.getObjectId()),
                with("object_id", created.getObjectId()),
                with("object_type", ObjectTypeIds.getId(ObjectType.MNTNER))
        );
        Row rowB = added.get(admin_c,
                with("pe_ro_id", objectB.getObjectId()),
                with("object_id", created.getObjectId()),
                with("object_type", ObjectTypeIds.getId(ObjectType.MNTNER))
        );

        // A + B -> B + C
        subject.updateObject(created.getObjectId(), makeObject(ObjectType.MNTNER, pkey,
                new RpslAttribute(changedAttributeType, "B"),
                new RpslAttribute(changedAttributeType, "C")
        ));

        diff = Database.diff(diff.getToDatabase(), new Database(whoisTemplate));

        // Identical
        identical = diff.getIdentical();
        assertThat("Identical", identical.getAll(), hasSize(21));
        assertThat(identical.find("last"), hasSize(4));
        assertThat(identical.find("mntner"), hasSize(2));
        assertThat(identical.find("person_role"), hasSize(4));
        assertThat(identical.find("serials"), hasSize(5));
        assertThat(identical.find("history"), hasSize(1));
        assertThat(identical.find("names"), hasSize(4));
        assertThat(identical.find(admin_c), hasSize(1));

        // Confirm 'B' is still there
        identical.find(admin_c).get(rowB);

        // Removed
        removed = diff.getRemoved();
        assertThat("Removed", removed.getAll(), hasSize(2));
        assertThat(removed.find("last"), hasSize(1));
        assertThat(removed.find(admin_c), hasSize(1));

        // Confirm 'A' is removed
        removed.find(admin_c).get(rowA);

        // Modified
        modified = diff.getModified();
        assertThat("Modified", modified.getAll(), hasSize(1));
        assertThat(modified.find("serials"), hasSize(1));

        // Added
        added = diff.getAdded();
        assertThat("Added", added.getAll(), hasSize(4));
        assertThat(added.find("serials"), hasSize(1));
        assertThat(added.find("last"), hasSize(1));
        assertThat(added.find("history"), hasSize(1));
        assertThat(added.find(admin_c), hasSize(1));

        // This is what we care for
        Row rowC = added.get(admin_c,
                with("pe_ro_id", objectC.getObjectId()),
                with("object_id", created.getObjectId()),
                with("object_type", ObjectTypeIds.getId(ObjectType.MNTNER))
        );

        // B + C -> A + D
        subject.updateObject(created.getObjectId(), makeObject(ObjectType.MNTNER, pkey,
                new RpslAttribute(changedAttributeType, "A"),
                new RpslAttribute(changedAttributeType, "D")
        ));
        diff = Database.diff(diff.getToDatabase(), new Database(whoisTemplate));

        // Identical
        identical = diff.getIdentical();
        assertThat("Identical", identical.getAll(), hasSize(22));
        assertThat(identical.find("last"), hasSize(4));
        assertThat(identical.find("mntner"), hasSize(2));
        assertThat(identical.find("person_role"), hasSize(4));
        assertThat(identical.find("serials"), hasSize(6));
        assertThat(identical.find("names"), hasSize(4));
        assertThat(identical.find("history"), hasSize(2));

        // Removed
        removed = diff.getRemoved();
        assertThat("Removed", removed.getAll(), hasSize(3));
        assertThat(removed.find("last"), hasSize(1));
        assertThat(removed.find(admin_c), hasSize(2));

        // Confirm 'B' + 'C' are removed
        removed.find(admin_c).get(rowB);
        removed.find(admin_c).get(rowC);

        // Modified
        modified = diff.getModified();
        assertThat("Modified", modified.getAll(), hasSize(1));
        assertThat(modified.find("serials"), hasSize(1));

        // Added
        added = diff.getAdded();
        assertThat("Added", added.getAll(), hasSize(5));
        assertThat(added.find("serials"), hasSize(1));
        assertThat(added.find("last"), hasSize(1));
        assertThat(added.find("history"), hasSize(1));
        assertThat(added.find(admin_c), hasSize(2));

        // Confirmed 'A' and 'D' are added (the same A! :D)
        added.find(admin_c).get(rowA);
        Row rowD = added.get(admin_c,
                with("pe_ro_id", objectD.getObjectId()),
                with("object_id", created.getObjectId()),
                with("object_type", ObjectTypeIds.getId(ObjectType.MNTNER))
        );

        // A + D -> -----
        subject.updateObject(created.getObjectId(), makeObject(ObjectType.MNTNER, pkey));

        diff = Database.diff(diff.getToDatabase(), new Database(whoisTemplate));

        // Identical
        identical = diff.getIdentical();
        assertThat("Identical", identical.getAll(), hasSize(24));
        assertThat(identical.find("last"), hasSize(4));
        assertThat(identical.find("mntner"), hasSize(2));
        assertThat(identical.find("person_role"), hasSize(4));
        assertThat(identical.find("serials"), hasSize(7));
        assertThat(identical.find("names"), hasSize(4));
        assertThat(identical.find("history"), hasSize(3));

        // Removed
        removed = diff.getRemoved();
        assertThat("Removed", removed.getAll(), hasSize(3));
        assertThat(removed.find("last"), hasSize(1));
        assertThat(removed.find(admin_c), hasSize(2));

        // Confirm 'A' + 'D' are removed
        removed.find(admin_c).get(rowA);
        removed.find(admin_c).get(rowD);

        // Modified
        modified = diff.getModified();
        assertThat("Modified", modified.getAll(), hasSize(1));
        assertThat(modified.find("serials"), hasSize(1));

        // Added
        added = diff.getAdded();
        assertThat("Added", added.getAll(), hasSize(3));
        assertThat(added.find("serials"), hasSize(1));
        assertThat(added.find("last"), hasSize(1));
        assertThat(added.find("history"), hasSize(1));

        // By now we need sequenceId of 5 :)
        added.get("last",
                with("object_id", created.getObjectId()),
                with("sequence_id", 5)
        );
    }

    @Test
    public void create_caseInsensitiveDuplicate_Main() {
        final RpslObjectUpdateInfo created = subject.createObject(makeObject(ObjectType.MNTNER, "PKEY"));
        final Database before = new Database(whoisTemplate);

        final AttributeType attributeType = AttributeType.UPD_TO;
        final RpslObjectUpdateInfo updated = subject.updateObject(created.getObjectId(), makeObject(ObjectType.MNTNER, "PKEY",
                new RpslAttribute(attributeType, "upd@te.to"),
                new RpslAttribute(attributeType, "UPD@TE.TO"),
                new RpslAttribute(attributeType, "uPd@TE.to"),
                new RpslAttribute(attributeType, "UpD@Te.To")
        ));

        final DatabaseDiff diff = Database.diff(before, new Database(whoisTemplate));

        // Make sure it only appears once
        assertThat(diff.getAdded().find(getTableForAttributeType(attributeType)), hasSize(1));
        final Rows rows = diff.getAdded().find(getTableForAttributeType(attributeType),
                with("object_id", updated.getObjectId())
        );
        assertThat(rows, hasSize(1));
    }

    @Test
    public void create_caseInsensitiveDuplicate_Leaf() {
        subject.createObject(makeObject(ObjectType.ORGANISATION, "ORG-REF"));
        final RpslObjectUpdateInfo created = subject.createObject(makeObject(ObjectType.MNTNER, "PKEY"));
        final Database before = new Database(whoisTemplate);

        final AttributeType attributeType = AttributeType.ORG;
        final RpslObjectUpdateInfo updated = subject.updateObject(created.getObjectId(), makeObject(ObjectType.MNTNER, "PKEY",
                new RpslAttribute(attributeType, "org-ref"),
                new RpslAttribute(attributeType, "Org-rEF"),
                new RpslAttribute(attributeType, "ORG-REF"),
                new RpslAttribute(attributeType, "ORG-rEf")
        ));

        final DatabaseDiff diff = Database.diff(before, new Database(whoisTemplate));

        // Make sure it only appears once
        assertThat(diff.getAdded().find(getTableForAttributeType(attributeType)), hasSize(1));
        final Rows rows = diff.getAdded().find(getTableForAttributeType(attributeType),
                with("object_id", updated.getObjectId())
        );
        assertThat(rows, hasSize(1));
    }

    /* The 4 methods below ensure we have no dependency on the data in the Object/AttributeDao we're testing. */
    private final Map<AttributeType, String> TABLE_BY_ATTRIBUTE = new HashMap<AttributeType, String>() {{
        put(AttributeType.ABUSE_MAILBOX, "abuse_mailbox");
        put(AttributeType.ADMIN_C, "admin_c");
        put(AttributeType.AUTH, "auth");
        put(AttributeType.MNTNER, "mntner");
        put(AttributeType.MNT_BY, "mnt_by");
        put(AttributeType.MNT_NFY, "mnt_nfy");
        put(AttributeType.NOTIFY, "notify");
        put(AttributeType.ORG, "org");
        put(AttributeType.REFERRAL_BY, "referral_by");
        put(AttributeType.TECH_C, "tech_c");
        put(AttributeType.UPD_TO, "upd_to");
    }};

    private String getTableForAttributeType(final AttributeType attributeType) {
        if (!TABLE_BY_ATTRIBUTE.containsKey(attributeType)) {
            throw new IllegalArgumentException("No table for AttributeType: " + attributeType);
        }

        return TABLE_BY_ATTRIBUTE.get(attributeType);
    }

    private final Map<AttributeType, String> COLUMN_BY_ATTRIBUTE = new HashMap<AttributeType, String>() {{
        put(AttributeType.ABUSE_MAILBOX, "abuse_mailbox");
        put(AttributeType.ADMIN_C, "pe_ro_id");
        put(AttributeType.AUTH, "auth");
        put(AttributeType.MNTNER, "mntner");
        put(AttributeType.MNT_BY, "mnt_id");
        put(AttributeType.MNT_NFY, "mnt_nfy");
        put(AttributeType.NOTIFY, "notify");
        put(AttributeType.ORG, "org_id");
        put(AttributeType.REFERRAL_BY, "mnt_id");
        put(AttributeType.TECH_C, "pe_ro_id");
        put(AttributeType.UPD_TO, "upd_to");
    }};

    private String getColumnForAttributeType(final AttributeType attributeType) {
        if (!COLUMN_BY_ATTRIBUTE.containsKey(attributeType)) {
            throw new IllegalArgumentException("No column for AttributeType: " + attributeType);
        }

        return COLUMN_BY_ATTRIBUTE.get(attributeType);
    }

    private final Map<ObjectType, String> TABLE_BY_OBJECT = new HashMap<ObjectType, String>() {{
        put(ObjectType.MNTNER, "mntner");
        put(ObjectType.ORGANISATION, "organisation");
        put(ObjectType.PERSON, "person_role");
        put(ObjectType.ROLE, "person_role");
        put(ObjectType.INETNUM, "inetnum");
        put(ObjectType.INET6NUM, "inet6num");
    }};

    private String getTableForObjectType(final ObjectType objectType) {
        if (!TABLE_BY_OBJECT.containsKey(objectType)) {
            throw new IllegalArgumentException("No table for ObjectType: " + objectType);
        }

        return TABLE_BY_OBJECT.get(objectType);
    }

    private static RpslObject makeObject(final ObjectType type, final String pkey, final RpslAttribute... rpslAttributes) {
        final List<RpslAttribute> attributeList = Lists.newArrayList();

        attributeList.add(new RpslAttribute(AttributeType.getByName(type.getName()), pkey));

        // append secondary keys
        switch (type) {
            case INET6NUM:
            case INETNUM:
                attributeList.add(new RpslAttribute(AttributeType.NETNAME, "netname"));
                break;
            case ROUTE:
            case ROUTE6:
                attributeList.add(new RpslAttribute(AttributeType.ORIGIN, "AS3333"));
                break;
            case PERSON:
            case ROLE:
                attributeList.add(new RpslAttribute(AttributeType.NIC_HDL, pkey));
                break;
        }

        attributeList.addAll(Arrays.asList(rpslAttributes));

        return new RpslObject(0, attributeList);
    }
}
