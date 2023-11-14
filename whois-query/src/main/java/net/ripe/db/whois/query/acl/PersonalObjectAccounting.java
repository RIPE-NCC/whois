package net.ripe.db.whois.query.acl;

public interface PersonalObjectAccounting {

    /**
     * Get the number of allowed personal objects in the query response.
     *
     * @param identifier The identifier address
     * @return The number of personal objects
     */
    int getQueriedPersonalObjects(String identifier);

    /**
     * Account a personal object.
     *
     * @param identifier The remote address
     * @param amount        The amount to count
     * @return The personal object balance
     */
    int accountPersonalObject(String identifier, int amount);

    /**
     * Resets all counters
     */
    void resetAccounting();
}
