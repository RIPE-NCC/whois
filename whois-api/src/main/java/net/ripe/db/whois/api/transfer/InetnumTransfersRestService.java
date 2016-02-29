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

        return inetnumTransfersService.transferOut(request, inetnum, override);
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response transferIn(@Context final HttpServletRequest request,
                               @PathParam("inetnum") final String inetnum,
                               @QueryParam("override") final String override) {

        LOGGER.info("transfer-in: inetnum: {}", inetnum);

        try {
            return inetnumTransfersService.transferIn(request, inetnum, override);
        } catch(org.springframework.transaction.UnexpectedRollbackException exc ) {
            exc.printStackTrace();
            LOGGER.info("case:" + exc.getMessage() );
            LOGGER.info("root-cause:" + exc.getRootCause().getMessage());
            LOGGER.info("most-specific-cause:"+ exc.getMostSpecificCause());
            return null;
        }
    }
}
