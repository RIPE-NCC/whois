package net.ripe.db.whois.api.transfer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static net.ripe.db.whois.api.transfer.ResponseHandling.createResponse;

@Service
@Path("/transfer/inetnum/{inetnum:.*}")
public class InetnumTransfersRestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InetnumTransfersRestService.class);
    private final InetnumTransfersService inetnumTransfersService;

    @Autowired
    public InetnumTransfersRestService(final InetnumTransfersService inetnumTransfersService) {
        this.inetnumTransfersService = inetnumTransfersService;
    }

    @DELETE
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response transferOut(@Context final HttpServletRequest request,
                                @PathParam("inetnum") final String inetnum,
                                @QueryParam("override") final String override) {

        LOGGER.info("transfer-out: inetnum: {}", inetnum);

        try {
            final String responseMsg = inetnumTransfersService.transferOut(request, inetnum, override);
            return createResponse(request, responseMsg, Response.Status.OK);
        } catch (IllegalArgumentException exc) {
            LOGGER.warn("IllegalArgumentException:{}", exc.getMessage());
            return createResponse(request, exc.getMessage(), Response.Status.BAD_REQUEST);
        } catch (ClientErrorException exc) {
            LOGGER.warn("ClientErrorException:{}", exc.getMessage());
            return createResponse(request, exc.getMessage(), Response.Status.fromStatusCode(exc.getResponse().getStatus()));
        } catch (TransferFailedException exc) {
            LOGGER.warn("TransferFailedException:{}", exc.getMessage());
            return createResponse(request, exc.getMessage(), exc.getStatus());
        } catch (Exception exc) {
            LOGGER.warn("Exception:{}", exc.getMessage());
            return createResponse(request, "", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response transferIn(@Context final HttpServletRequest request,
                               @PathParam("inetnum") final String inetnum,
                               @QueryParam("override") final String override) {

        LOGGER.info("transfer-in: inetnum: {}", inetnum);

        try {
            final String responseMsg = inetnumTransfersService.transferIn(request, inetnum, override);
            return createResponse(request, responseMsg, Response.Status.OK);
        } catch (IllegalArgumentException exc) {
            LOGGER.warn("IllegalArgumentException:{}", exc.getMessage());
            return createResponse(request, exc.getMessage(), Response.Status.BAD_REQUEST);
        } catch (ClientErrorException exc) {
            LOGGER.warn("ClientErrorException:{}", exc.getMessage());
            return createResponse(request, exc.getMessage(), Response.Status.fromStatusCode(exc.getResponse().getStatus()));
        } catch (TransferFailedException exc) {
            LOGGER.warn("TransferFailedException:{}", exc.getMessage());
            return createResponse(request, exc.getMessage(), exc.getStatus());
        } catch (Exception exc) {
            LOGGER.warn("Exception:{}", exc.getMessage());
            return createResponse(request, "", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
