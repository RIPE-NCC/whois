package net.ripe.db.whois.logsearch.bootstrap;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LogSearchJettyConfig {
    private List<Integer> portList = Lists.newArrayListWithCapacity(1);

    public void setPort(final int port) {
        portList.add(port);
    }

    public int getPort() {
        return portList.get(0);
    }
}
