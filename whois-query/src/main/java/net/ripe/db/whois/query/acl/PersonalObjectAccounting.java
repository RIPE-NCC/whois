package net.ripe.db.whois.query.acl;

import java.net.InetAddress;

public interface PersonalObjectAccounting {

    /**
     * Get the number of allowed personal objects in the query response.
     *
     * @param remoteAddress The remote address
     * @return The number of personal objects
     */
    int getQueriedPersonalObjects(InetAddress remoteAddress);
    /**
     * Get the number of allowed personal objects in the query response.
     *
     * @param ssoId The email address of user
     * @return The number of personal objects
     */
    int getQueriedPersonalObjects(String ssoId);

    /**
     * Account a personal object.
     *
     * @param remoteAddress The remote address
     * @param amount        The amount to count
     * @return The personal object balance
     */
    int accountPersonalObject(InetAddress remoteAddress, int amount);

    /**
     * Account a personal object.
     *
     * @param ssoId The email address of user
     * @param amount        The amount to count
     * @return The personal object balance
     */
    int accountPersonalObject(String ssoId, int amount);

    /**
     * Resets all counters
     */
    void resetAccounting();
}
