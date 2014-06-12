package net.ripe.db.roleattribute;

import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

import static net.ripe.db.whois.common.rpsl.AttributeSyntax.ORGANISATION_SYNTAX;
import static net.ripe.db.whois.common.rpsl.AttributeSyntax.ORG_NAME_SYNTAX;
import static net.ripe.db.whois.common.rpsl.AttributeType.ROLE;

/**
 * One shot script to get all current role attributes in role objects and check if they are compatible
 */
public class RoleAttributeSyntaxChecker {
    JdbcTemplate template;

    public static void main(String[] args) throws SQLException {
        RoleAttributeSyntaxChecker checker = new RoleAttributeSyntaxChecker();
        checker.run();

    }

    public RoleAttributeSyntaxChecker() throws SQLException {
        DataSource dataSource = new SimpleDriverDataSource(new com.mysql.jdbc.Driver(), "jdbc:mysql://dbc-whois8/WHOIS_UPDATE_RIPE", "dbint", "dbint");
        template = new JdbcTemplate(dataSource);

    }


    private void run() {
        int errorCount = 0;

        for (RpslObject role : template.query("SELECT l.object_id, l.object FROM last l WHERE l.object_type = 11", new RpslObjectRowMapperHandleExceptions())) {
            if (role != null && !ORG_NAME_SYNTAX.matches(null, role.getValueForAttribute(ROLE).toString())) {
                System.out.println("Error: " + role.getValueForAttribute(ROLE) + " does not match organisation syntax");
                errorCount++;
            }
        }
        System.out.println("total roles in error: " + errorCount);
    }

    public class RpslObjectRowMapperHandleExceptions implements RowMapper<RpslObject> {
        @Override
        @Nullable
        public RpslObject mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            try {
                return RpslObject.parse(rs.getInt(1), rs.getBytes(2));

            } catch (Exception e) {
                return null;
            }

        }
    }

}
