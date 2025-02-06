package net.ripe.db.whois.api.nrtm4;

import com.google.common.net.HttpHeaders;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.nrtm4.dao.DeltaFileSourceAwareDao;
import net.ripe.db.nrtm4.dao.NrtmKeyConfigDao;
import net.ripe.db.nrtm4.dao.UpdateNotificationFileSourceAwareDao;
import net.ripe.db.nrtm4.dao.SnapshotFileSourceAwareDao;
import net.ripe.db.nrtm4.dao.NrtmSourceDao;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import static net.ripe.db.nrtm4.util.JWSUtil.signWithJWS;

@Component
@Path("/")
public class NrtmClientService {

    public static final String SOURCE_LINK_PAGE = "<html><header><title>NRTM Version 4</title></header><body>%s<body></html>";
    private final SnapshotFileSourceAwareDao snapshotFileSourceAwareDao;
    private final DeltaFileSourceAwareDao deltaFileSourceAwareDao;
    private final UpdateNotificationFileSourceAwareDao updateNotificationFileSourceAwareDao;
    private final NrtmSourceDao nrtmSourceDao;
    private final NrtmKeyConfigDao nrtmKeyConfigDao;
    final String nrtmUrl;

    @Autowired
    public NrtmClientService(@Value("${nrtm.baseUrl:}") final String nrtmUrl,
                             final NrtmSourceDao nrtmSourceDao,
                             final UpdateNotificationFileSourceAwareDao updateNotificationFileSourceAwareDao,
                             final SnapshotFileSourceAwareDao snapshotFileSourceAwareDao,
                             final NrtmKeyConfigDao nrtmKeyConfigDao,
                             final DeltaFileSourceAwareDao deltaFileSourceAwareDao) {
        this.snapshotFileSourceAwareDao = snapshotFileSourceAwareDao;
        this.deltaFileSourceAwareDao = deltaFileSourceAwareDao;
        this.updateNotificationFileSourceAwareDao = updateNotificationFileSourceAwareDao;
        this.nrtmSourceDao = nrtmSourceDao;
        this.nrtmKeyConfigDao = nrtmKeyConfigDao;
        this.nrtmUrl = nrtmUrl;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String sourcesLinkAsHtml() {
        final StringBuilder sourceLink = new StringBuilder();

        nrtmSourceDao.getSources().forEach(sourceModel ->
                sourceLink.append(
                        String.format("<a href='%s'>%s</a><br/>",
                                        String.join("/", nrtmUrl, sourceModel.getName().toString(), "update-notification-file.json"),
                                        sourceModel.getName().toString()
                                    )
                )
        );

        return String.format(SOURCE_LINK_PAGE, sourceLink);
    }

    @GET
    @Path("{source}/{filename}")
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public Response nrtmFiles(@PathParam("source") final String source,
            @PathParam("filename") final String fileName) {

        if(isNotificationFile(fileName)) {

            final String payload = updateNotificationFileSourceAwareDao.findLastNotification(getSource(source))
                    .orElseThrow(() -> new NotFoundException("update-notification-file does not exists for source " + source));

            return getResponseForJWS(signWithJWS(payload, nrtmKeyConfigDao.getActivePrivateKey()));
        }

        validateSource(source, fileName);
        if(fileName.startsWith(NrtmDocumentType.SNAPSHOT.getFileNamePrefix())) {
            return snapshotFileSourceAwareDao.getByFileName(fileName)
                    .map( snapshot -> getResponse(snapshot.getPayload(), snapshot.getSnapshotFile().hash(), fileName))
                    .orElseThrow(() -> new NotFoundException("Requested Snapshot file does not exists"));
        }

        if(fileName.startsWith(NrtmDocumentType.DELTA.getFileNamePrefix())) {
            return deltaFileSourceAwareDao.getByFileName(filenameWithExt(fileName))
                    .map( delta -> getResponseForDelta(delta.payload()))
                    .orElseThrow(() -> new NotFoundException("Requested Delta file does not exists"));
        }

        throw new BadRequestException("Invalid Nrtm filename");
    }

    private void validateSource(final String source, final String fileName) {
        if(!NrtmFileUtil.getSource(fileName).equals(source)) {
            throw new BadRequestException("Invalid source and filename combination");
        }
    }

    private NrtmSource getSource(final String source) {
        return nrtmSourceDao.getSources().stream().filter(sourceModel -> sourceModel.getName().equals(source)).findFirst().orElseThrow(() -> new BadRequestException("Invalid source"));
    }

    private String filenameWithExt(final String filename) {
        //ExtensionOverridesAcceptHeaderFilter removes file extension
        return filename.endsWith(".json") ? filename : String.join("", filename, ".json");
    }
    private Response getResponse(final byte[] payload,  final String hash, final String filename) {
        return Response.ok(new ByteArrayInputStream(payload))
                .header(HttpHeaders.CONTENT_LENGTH, payload.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.ETAG, hash)
                .build();
    }

    private Response getResponseForJWS(final String payload) {
        return Response.ok(payload)
                .header(HttpHeaders.CONTENT_TYPE, "application/jose+json")
                .build();
    }
    
    private Response getResponseForDelta(final String payload) {
        return Response.ok(payload)
                .header(HttpHeaders.CONTENT_TYPE, "application/json-seq")
                .build();
    }

    private boolean isNotificationFile(final String fileName) {

        if(!fileName.startsWith(NrtmDocumentType.NOTIFICATION.getFileNamePrefix())) {
            return false;
        }

        final String fileExtension = StringUtils.substringAfter(fileName, NrtmDocumentType.NOTIFICATION.getFileNamePrefix());
        if(!fileExtension.equals(".jose") )  {
            throw new NotFoundException("Notification file does not exists");
        }

        return true;
    }
}
