package net.ripe.db.whois.changedphase3.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class MailUpdateRunner extends AbstractScenarioRunner {

    public MailUpdateRunner(final Context context) {
        super(context);
    }

    @Override
    public String getProtocolName() {
        return "Mail";
    }

    @Override
    public void create(final Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            RpslObject objectForScenario = objectForScenario(scenario);

            final String response = context.getMailUpdatesTestSupport().insert("NEW", objectForScenario.toString() + "\npassword:123");
            final MimeMessage message = context.getMailSenderStub().getMessage(response);

            if (scenario.getResult() == Scenario.Result.SUCCESS) {
                assertThat(message.getContent().toString(), containsString("Create SUCCEEDED: [mntner] TESTING-MNT"));
                verifyPostCondition(scenario, Scenario.Result.SUCCESS, message.getContent().toString());
            } else {
                assertThat(message.getContent().toString(), containsString("***Error:"));
                verifyPostCondition(scenario, Scenario.Result.FAILURE);
            }

        } catch (MessagingException | IOException exc) {
            verifyPostCondition(scenario, Scenario.Result.FAILURE);
        }
    }

    @Override
    public void modify(final Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            RpslObject objectForScenario = addRemarks(objectForScenario(scenario));

            final String response = context.getMailUpdatesTestSupport().insert("MODIFY", objectForScenario.toString() + "password:123");
            final MimeMessage message = context.getMailSenderStub().getMessage(response);

            if (scenario.getResult() == Scenario.Result.SUCCESS) {
                assertThat(message.getContent().toString(), containsString("Modify SUCCEEDED: [mntner] TESTING-MNT"));
                verifyPostCondition(scenario, Scenario.Result.SUCCESS, message.getContent().toString());
            } else {
                assertThat(message.getContent().toString(), containsString("***Error:"));
                verifyPostCondition(scenario, Scenario.Result.FAILURE);
            }

        } catch (MessagingException | IOException exc) {
            verifyPostCondition(scenario, Scenario.Result.FAILURE);
        }
    }

    @Override
    public void delete(final Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            RpslObject objectForScenario = objectForScenario(scenario);

            final String response = context.getMailUpdatesTestSupport().insert("DELETE", objectForScenario.toString() + "delete: testing\npassword:123");
            final MimeMessage message = context.getMailSenderStub().getMessage(response);

            if (scenario.getResult() == Scenario.Result.SUCCESS) {
                assertThat(message.getContent().toString(), containsString("Delete SUCCEEDED: [mntner] TESTING-MNT"));
                verifyPostCondition(scenario, Scenario.Result.SUCCESS, message.getContent().toString());
            } else {
                assertThat(message.getContent().toString(), containsString("***Error:"));
                verifyPostCondition(scenario, Scenario.Result.FAILURE);
            }

        } catch (MessagingException | IOException exc) {
            verifyPostCondition(scenario, Scenario.Result.FAILURE);
        }
    }
}
