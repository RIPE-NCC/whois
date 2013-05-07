package net.ripe.db;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class LogUtil {
    private LogUtil() {
    }

    public static void initLogger() {
        ConsoleAppender console = new ConsoleAppender();
        console.setLayout(new PatternLayout("%d{ISO8601} %-5p [%c{1}] %m%n"));
        console.setThreshold(Level.INFO);
        console.activateOptions();
        Logger.getRootLogger().addAppender(console);
    }
}
