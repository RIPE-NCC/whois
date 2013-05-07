package net.ripe.db.whois.nrtm.integration;

import net.ripe.db.whois.common.support.DummyWhoisClient;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class DummyNrtmClient {

    private final FutureTask<String> task;

    public DummyNrtmClient(final int port, final String query, final int timeout) {
        task = new FutureTask<String>(new Callable<String>() {
            public String call() {
                String result = DummyWhoisClient.query(port, query, timeout * 1000);
                return result;
            }
         });
    }

    public void start() {
        Executor ex = Executors.newFixedThreadPool(1);
        ex.execute(task);
    }

    public String end() {
        try {
            return task.get();
        } catch (Exception e) {
            return null;
        }
    }
}