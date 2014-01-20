package net.ripe.db;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;

public class LogUtil {
    private LogUtil() {
    }

    public static void initLogger() {
        LogManager.getRootLogger().setLevel(Level.INFO);
        final ConsoleAppender console = new ConsoleAppender();
        console.setLayout(new PatternLayout("%d [%C{1}] %m%n"));
        console.setTarget("System.err");
        console.setThreshold(Level.INFO);
        console.activateOptions();
        LogManager.getRootLogger().addAppender(console);
    }
}
