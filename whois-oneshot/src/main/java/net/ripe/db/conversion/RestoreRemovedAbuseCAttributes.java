package net.ripe.db.conversion;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.*;
import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.dao.jdbc.JdbcVersionDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static net.ripe.db.whois.common.domain.CIString.ciString;

public class RestoreRemovedAbuseCAttributes {

    private static DataSource dataSource;
    private static JdbcTemplate jdbcTemplate;
    private static VersionDao versionDao;

    private static int count = 0;

    public static void main(String[] args) throws SQLException, IOException {
        dataSource = new SimpleDriverDataSource(new com.mysql.jdbc.Driver(), "jdbc:mysql://whois/WHOIS_UPDATE_RIPE", "rdonly", "XXX");
        jdbcTemplate = new JdbcTemplate(dataSource);
        versionDao = new JdbcVersionDao(dataSource);

        // collect last 2 weeks of changed org objects with org-type: LIR
        final List<Map<String,Object>> orgEntries = jdbcTemplate.queryForList("SELECT object, pkey FROM last WHERE timestamp > 1363079257 AND object_type = 18");
//        final List<Map<String,Object>> orgEntries = jdbcTemplate.queryForList(""+
//                "SELECT last.object, last.pkey FROM serials "+
//                "JOIN last ON last.object_id = serials.object_id AND last.sequence_id = serials.sequence_id "+
//                "WHERE serials.serial_id > 25206872 AND last.object_type = 18");

        outer:
        for (Map<String, Object> orgEntry : orgEntries) {
            byte[] orgObject = (byte[])orgEntry.get("object");
            if (orgObject.length == 0) {
                System.err.println("Deleted: "+orgEntry.get("pkey"));
                continue;
            }
            final RpslObjectBase rpslObject = RpslObjectBase.parse(orgObject);

            // check for org-type
            if (!rpslObject.getValueForAttribute(AttributeType.ORG_TYPE).equals(ciString("lir"))) {
                System.err.println("Not LIR: ["+rpslObject.getValueForAttribute(AttributeType.ORG_TYPE)+"]");
                continue;
            }

            // check for abuse-c
            if (rpslObject.containsAttribute(AttributeType.ABUSE_C)) {
                System.err.println("Has abuse-c: " + orgEntry.get("pkey"));
                continue;
            }

            // check for abuse-c in previous versions of the object
            for (VersionInfo version : Iterables.skip(Lists.reverse(versionDao.findByKey(ObjectType.ORGANISATION, rpslObject.getKey().toString()).getVersionInfos()), 1)) {
                final RpslObject oldEntry = versionDao.getRpslObject(version);
                if (oldEntry.containsAttribute(AttributeType.ABUSE_C)) {
                    final List<RpslAttribute> attributes = Lists.newArrayList(rpslObject.getAttributes());
                    for (int i = 0; i < attributes.size(); i++) {
                        RpslAttribute rpslAttribute = attributes.get(i);
                        if (rpslAttribute.getType() == AttributeType.MNT_REF) {
                            attributes.add(i, oldEntry.findAttribute(AttributeType.ABUSE_C));
                            break;
                        }
                    }

                    final RpslObjectBase newEntry = new RpslObjectBase(attributes);
                    System.out.println(newEntry.toString().trim());
                    System.out.println("override:agoston,XXX,restoring override {notify=false}");
                    System.out.println();
                    continue outer;
                }
            }

            System.err.println("No old abuse-c found: "+rpslObject.getKey());
        }
    }

    private String doPostRequest(final String url, final String data, final int responseCode) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Length", Integer.toString(data.length()));
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        connection.setDoInput(true);
        connection.setDoOutput(true);

        Writer writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(data);
        writer.close();

        return readResponse(connection);
    }

    private String readResponse(final HttpURLConnection connection) throws IOException {
        InputStream inputStream = null;

        try {
            StringBuilder builder = new StringBuilder();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }

            BufferedReader responseReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = responseReader.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }

            return builder.toString();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    // Encode a String into a URL encoded format.
    // We don't consider a space to be a separator, and is encoded to '%20' not '+'.
    private String encode(final String data) throws UnsupportedEncodingException {
        return URLEncoder.encode(data, Charsets.ISO_8859_1.name()).replaceAll("[+]", "%20");
    }
}
