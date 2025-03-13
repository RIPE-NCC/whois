package net.ripe.db.whois.smtp;

import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.dao.SerialDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component
public class SmtpServerHandlerFactory {

    private final SerialDao serialDao;
    private final TaskScheduler clientSynchronisationScheduler;
    final ApplicationVersion applicationVersion;

    @Autowired
    public SmtpServerHandlerFactory(
            @Qualifier("jdbcSlaveSerialDao") final SerialDao serialDao,
            @Qualifier("clientSynchronisationScheduler") final TaskScheduler clientSynchronisationScheduler,
            final ApplicationVersion applicationVersion) {
        this.serialDao = serialDao;
        this.clientSynchronisationScheduler = clientSynchronisationScheduler;
        this.applicationVersion = applicationVersion;
    }

    public SmtpServerHandler getInstance() {
        return new SmtpServerHandler(
            serialDao,
            clientSynchronisationScheduler,
            applicationVersion);
    }



}
