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
@Path("/transfer/aut-num/{autNum}")
public class AsnTransfersRestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsnTransfersRestService.class);
    private AsnTransferService asnTransferService;

    @Autowired
    public AsnTransfersRestService(final AsnTransferService asnTransferService) {
        this.asnTransferService = asnTransferService;
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response transferIn(@Context final HttpServletRequest request,
                               @PathParam("autNum") final String autnum,
                               @QueryParam("override") final String override) {

        LOGGER.info("transfer-in: aut-num: {}", autnum);

        try {
            final String responseMsg = asnTransferService.transferIn(request, autnum, override);
            return createResponse(request, responseMsg, Response.Status.OK);
        } catch (ClientErrorException exc) {
            LOGGER.info("ClientErrorException:{}", exc.getMessage());
            return createResponse(request, exc.getMessage(), Response.Status.fromStatusCode(exc.getResponse().getStatus()));
        } catch (TransferFailedException exc) {
            LOGGER.info("TransferFailedException:{}", exc.getMessage());
            return createResponse(request, exc.getMessage(), exc.getStatus());
        } catch (Exception exc) {
            LOGGER.info("Exception:{}", exc.getMessage());
            return createResponse(request, "", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response transferOut(@Context final HttpServletRequest request,
                                @PathParam("autNum") final String autnum,
                                @QueryParam("override") final String override) {

        LOGGER.info("transfer-out: aut-num: {}", autnum);

        try {
            final String responseMsg = asnTransferService.transferOut(request, autnum, override);
            return createResponse(request, responseMsg, Response.Status.OK);
        } catch (ClientErrorException exc) {
            LOGGER.info("ClientErrorException: {}", exc.getMessage());
            return createResponse(request, exc.getMessage(), Response.Status.fromStatusCode(exc.getResponse().getStatus()));
        } catch (TransferFailedException exc) {
            LOGGER.info("TransferFailedException {}", exc.getMessage());
            return createResponse(request, exc.getMessage(), exc.getStatus());
        } catch (Exception exc) {
            LOGGER.info("Exception: {}", exc.getMessage());
            return createResponse(request, "", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}


