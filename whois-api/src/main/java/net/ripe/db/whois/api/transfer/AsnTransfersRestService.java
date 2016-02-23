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

@Service
@Path("/transfer/aut-num/{autNum}")
public class AsnTransfersRestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsnTransfersRestService.class);
    private AsnTransferService asnTransfersRestServ;

    @Autowired
    public AsnTransfersRestService(final AsnTransferService asnTransfersRestServ) {
        this.asnTransfersRestServ = asnTransfersRestServ;
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response transferIn(@Context final HttpServletRequest request,
                               @PathParam("autNum") final String autnum,
                               @QueryParam("override") final String override) {

        LOGGER.info("transfer-in: aut-num: {}", autnum);
        LOGGER.info("transfer-in: override: {}", override);

        return asnTransfersRestServ.transferIn(request, autnum, override);
    }

    @DELETE
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response transferOut(@Context final HttpServletRequest request,
                                @PathParam("autNum") final String autnum,
                                @QueryParam("override") final String override) {

        LOGGER.info("transfer-out: aut-num: {}", autnum);
        LOGGER.info("transfer-out: override: {}", override);

        return asnTransfersRestServ.transferOut(request, autnum, override);
    }

}


