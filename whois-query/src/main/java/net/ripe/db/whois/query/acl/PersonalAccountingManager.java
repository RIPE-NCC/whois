package net.ripe.db.whois.query.acl;

public interface PersonalAccountingManager {

     int getPersonalObjects();

     void accountPersonalObjects(final int amount);

     void blockTemporary(final int limit);
     int getPersonalDataLimit();
}
