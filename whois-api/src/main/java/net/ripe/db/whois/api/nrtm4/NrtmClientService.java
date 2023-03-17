package net.ripe.db.whois.api.nrtm4;

import com.google.common.net.HttpHeaders;
import net.ripe.db.nrtm4.dao.DeltaSourceAwareFileRepository;
import net.ripe.db.nrtm4.dao.NotificationFileSourceAwareDao;
import net.ripe.db.nrtm4.dao.SnapshotSourceAwareFileRepository;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmSourceModel;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;

@Component
@Path("/")
public class NrtmClientService {

    public static final String SOURCE_LINK_PAGE = "<html><header><title>NRTM Version 4</title></header><body>%s<body></html>";
    private final SnapshotSourceAwareFileRepository snapshotSourceAwareFileRepository;
    private final DeltaSourceAwareFileRepository deltaSourceAwareFileRepository;
    private final NotificationFileSourceAwareDao notificationFileSourceAwareDao;
    private final SourceRepository sourceRepository;
    final String nrtmUrl;

    @Autowired
    public NrtmClientService(@Value("${nrtm.baseUrl}") final String nrtmUrl,
                             final SourceRepository sourceRepository,
                             final NotificationFileSourceAwareDao notificationFileSourceAwareDao,
                             final SnapshotSourceAwareFileRepository snapshotSourceAwareFileRepository,
                             final DeltaSourceAwareFileRepository deltaSourceAwareFileRepository) {
        this.snapshotSourceAwareFileRepository = snapshotSourceAwareFileRepository;
        this.deltaSourceAwareFileRepository = deltaSourceAwareFileRepository;
        this.notificationFileSourceAwareDao = notificationFileSourceAwareDao;
        this.sourceRepository = sourceRepository;
        this.nrtmUrl = nrtmUrl;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String sourcesLinkAsHtml() {

        final StringBuilder sourceLink = new StringBuilder();

        sourceRepository.getAllSources().forEach(sourceModel ->
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
    public Response nrtmFiles(
            @Context final HttpServletRequest httpServletRequest,
            @PathParam("source") final String source,
            @PathParam("filename") final String fileName) {

        if(fileName.startsWith(NrtmDocumentType.NOTIFICATION.getFileNamePrefix())) {
            return notificationFileSourceAwareDao.findLastNotification(getSource(source))
                    .map( payload -> getResponse(payload))
                    .orElseThrow(() -> new NotFoundException("update-notification-file.json does not exists for source " + source));
        }

        validateSource(source, fileName);
        if(fileName.startsWith(NrtmDocumentType.SNAPSHOT.getFileNamePrefix())) {
            return snapshotSourceAwareFileRepository.getByFileName(fileName)
                    .map( snapshot -> getResponse(snapshot.getPayload(), snapshot.getSnapshotFile().hash(), fileName))
                    .orElseThrow(() -> new NotFoundException("Requested Snapshot file does not exists"));
        }

        final String filenameExt  = filenameWithExtension(fileName);
        if(fileName.startsWith(NrtmDocumentType.DELTA.getFileNamePrefix())) {
            return deltaSourceAwareFileRepository.getByFileName(filenameExt)
                    .map( delta -> getResponse(delta.payload()))
                    .orElseThrow(() -> new NotFoundException("Requested Delta file does not exists"));
        }

        throw new BadRequestException("Invalid Nrtm filename");
    }

    private void validateSource(final String source, final String fileName) {
        if(!NrtmFileUtil.getSource(fileName).equals(source)) {
            throw new BadRequestException("Invalid source and filename combination");
        }
    }

    private NrtmSourceModel getSource(final String source) {
        return sourceRepository.getAllSources().stream().filter( sourceModel -> sourceModel.getName().equals(source)).findFirst().orElseThrow(() -> new BadRequestException("Invalid source"));
    }

    private String filenameWithExtension(final String filename) {
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

    private Response getResponse(final String payload) {
        return Response.ok(payload)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .build();
    }
}
