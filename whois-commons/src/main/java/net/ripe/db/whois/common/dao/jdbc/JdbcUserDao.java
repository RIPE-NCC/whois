package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.dao.UserDao;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RetryFor(RecoverableDataAccessException.class)
public class JdbcUserDao implements UserDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcUserDao(final @Qualifier("aclDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public User getOverrideUser(final String username) {
        return jdbcTemplate.queryForObject("" +
                "SELECT username, password, objecttypes " +
                "FROM override_users " +
                "WHERE username = ? ",
                new UserMapper(),
                username);
    }

    private static final class UserMapper implements RowMapper<User> {
        private static final Logger LOGGER = LoggerFactory.getLogger(UserMapper.class);
        private static final Splitter OBJECTTYPE_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

        @Override
        public User mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final String username = rs.getString(1);
            final String password = rs.getString(2);

            final List<ObjectType> objectTypes = Lists.newArrayList();
            for (final String objectTypeString : OBJECTTYPE_SPLITTER.split(rs.getString(3))) {
                try {
                    objectTypes.add(ObjectType.valueOf(objectTypeString));
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Unknown objecttype for user {}: {}", username, objectTypeString);
                }
            }

            return User.createWithHashedPassword(username, password, objectTypes);
        }
    }
}
