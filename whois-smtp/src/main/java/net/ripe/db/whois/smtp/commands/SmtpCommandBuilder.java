package net.ripe.db.whois.smtp.commands;


public class SmtpCommandBuilder {
    public static SmtpCommand build(String command) {
        if (HelloCommand.matches(command)) {
            return new HelloCommand(command);
        } else if (ExtendedHelloCommand.matches(command)) {
            return new ExtendedHelloCommand(command);
        } else if (MailCommand.matches(command)) {
            return new MailCommand(command);
        } else if (RecipientCommand.matches(command)) {
            return new RecipientCommand(command);
        } else if (DataCommand.matches(command)) {
            return new DataCommand(command);
        } else if (NoopCommand.matches(command)) {
            return new NoopCommand(command);
        } else if (HelpCommand.matches(command)) {
            return new HelpCommand(command);
        } else if (QuitCommand.matches(command)) {
            return new QuitCommand(command);
        } else {
            throw new IllegalArgumentException("Invalid smtp command: " + command);
        }
    }
}
