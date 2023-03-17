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
import java.util.Map;

@Component
@Path("/")
public class NrtmClientService {

    private final SnapshotSourceAwareFileRepository snapshotSourceAwareFileRepository;
    private final DeltaSourceAwareFileRepository deltaReadOnlyFileRepository;
    private final NotificationFileSourceAwareDao notificationFileSourceAwareDao;
    private final SourceRepository sourceRepository;

    @Autowired
    public NrtmClientService(final SourceRepository sourceRepository, final NotificationFileSourceAwareDao notificationFileSourceAwareDao, final SnapshotSourceAwareFileRepository snapshotSourceAwareFileRepository, final DeltaSourceAwareFileRepository deltaReadOnlyFileRepository) {
        this.snapshotSourceAwareFileRepository = snapshotSourceAwareFileRepository;
        this.deltaReadOnlyFileRepository = deltaReadOnlyFileRepository;
        this.notificationFileSourceAwareDao = notificationFileSourceAwareDao;
        this.sourceRepository = sourceRepository;
    }

    @GET
    @Path("{source}/{filename}")
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public Response nrtmFiles(
            @Context final HttpServletRequest httpServletRequest,
            @PathParam("source") final String source,
            @PathParam("filename") final String fileName) {

        if(fileName.startsWith(NrtmDocumentType.SNAPSHOT.getFileNamePrefix())) {
            validateSource(source, fileName);
            final Map<String, Object> payloadWithHash = snapshotSourceAwareFileRepository.getByFileName(fileName);

            if(payloadWithHash.isEmpty()) {
                throw new NotFoundException("Requested Snapshot file does not exists");
            }

            return getResponse( (byte[]) payloadWithHash.get("payload"), (String) payloadWithHash.get("hash"), fileName);
        }

        final String filenameExt  = filenameWithExtension(fileName);
        if(fileName.startsWith(NrtmDocumentType.DELTA.getFileNamePrefix())) {
            validateSource(source, filenameExt);
            return deltaReadOnlyFileRepository.getByFileName(filenameExt)
                    .map( delta -> getResponse(delta.payload(), delta.hash(), filenameExt))
                    .orElseThrow(() -> new NotFoundException("Requested Delta file does not exists"));
        }

        if(fileName.startsWith(NrtmDocumentType.NOTIFICATION.getFileNamePrefix())) {
            return notificationFileSourceAwareDao.findLastNotification(getSource(source))
                    .map( payload -> getResponse(payload,  NrtmFileUtil.NOTIFICATION_FILENAME))
                    .orElseThrow(() -> new NotFoundException("update-notification-file.json does not exists for source " + source));
        }

        throw new BadRequestException("Invalid nrtm filename");
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
        return addCommonHeader(Response.ok(new ByteArrayInputStream(payload)), payload.length, filename, MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.ETAG, hash)
                .build();
    }

    private Response getResponse(final String payload, final String hash, final String filename) {
        return addCommonHeader(Response.ok(payload), payload.length(), filename, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ETAG, hash)
                .build();
    }

    private Response getResponse(final String payload, final String filename) {
        return addCommonHeader(Response.ok(payload), payload.length(), filename, MediaType.APPLICATION_JSON)
                .build();
    }

    private Response.ResponseBuilder addCommonHeader(final Response.ResponseBuilder builder, final int length, final String filename, final String type) {
        return builder.header(HttpHeaders.CONTENT_LENGTH, length)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .header(HttpHeaders.CONTENT_TYPE, type);
    }
}
