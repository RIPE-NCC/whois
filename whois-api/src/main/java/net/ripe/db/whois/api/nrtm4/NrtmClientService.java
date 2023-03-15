package net.ripe.db.whois.api.nrtm4;

import com.google.common.net.HttpHeaders;
import net.ripe.db.nrtm4.dao.DeltaReadOnlyFileRepository;
import net.ripe.db.nrtm4.dao.SnapshotReadOnlyFileRepository;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Component
@Path("/")
public class NrtmClientService {

    private final SnapshotReadOnlyFileRepository snapshotReadOnlyFileRepository;
    private final DeltaReadOnlyFileRepository deltaReadOnlyFileRepository;

    @Autowired
    public NrtmClientService(final SnapshotReadOnlyFileRepository snapshotReadOnlyFileRepository, final DeltaReadOnlyFileRepository deltaReadOnlyFileRepository) {
        this.snapshotReadOnlyFileRepository = snapshotReadOnlyFileRepository;
        this.deltaReadOnlyFileRepository = deltaReadOnlyFileRepository;
    }

    @GET
    @Path("/snapshot/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response snapshotFile(
            @Context final HttpServletRequest httpServletRequest,
            @PathParam("filename") final String fileName) {

        if(!fileName.startsWith(NrtmDocumentType.SNAPSHOT.getFileNamePrefix())) {
           throw new BadRequestException("Invalid snapshot file name");
        }

        return snapshotReadOnlyFileRepository.getByFileName(fileName)
                .map( payload -> getResponse(payload, fileName))
                .orElseThrow(() -> new NotFoundException("Requested Snapshot file does not exists"));
    }

    @GET
    @Path("/delta/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response deltaFile(
            @Context final HttpServletRequest httpServletRequest,
            @PathParam("filename") final String fileName) {

        if(!fileName.startsWith(NrtmDocumentType.DELTA.getFileNamePrefix())) {
            throw new BadRequestException("Invalid delta file name");
        }

        return deltaReadOnlyFileRepository.getByFileName(fileName)
                .map( payload -> getResponse(payload, fileName))
                .orElseThrow(() -> new NotFoundException("Requested Delta file does not exists"));
    }

    private static Response getResponse(final byte[] payload, final String filename) {
        return Response.ok(new ByteArrayInputStream(payload))
                .header(HttpHeaders.CONTENT_LENGTH, payload.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .build();
    }
}
