package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.domain.User;

public interface UserDao {
    User getOverrideUser(String username);
}
