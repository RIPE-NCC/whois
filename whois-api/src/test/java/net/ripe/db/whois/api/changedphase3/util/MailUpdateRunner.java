package net.ripe.db.whois.api.changedphase3.util;

import net.ripe.db.whois.common.rpsl.RpslObject;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import java.io.IOException;

import static net.ripe.db.whois.common.rpsl.RpslObjectFilter.buildGenericObject;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class MailUpdateRunner extends AbstactScenarioRunner {

    public MailUpdateRunner(Context context) {
        super(context);
    }

    public String getProtocolName() {
        return "Mail";
    }

    public void create(Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            RpslObject objectForScenario = objectForScenario(scenario);

            final String response = context.getMailUpdatesTestSupport().insert("NEW", buildGenericObject(objectForScenario, "mntner: TESTING-MNT").toString() + "\npassword:123");
            final MimeMessage message = context.getMailSenderStub().getMessage(response);

            if (scenario.getResult() == Scenario.Result.SUCCESS) {
                assertThat(message.getContent().toString(), containsString("Create SUCCEEDED: [mntner] TESTING-MNT"));
                verifyPostCondition(scenario, Scenario.Result.SUCCESS);
            } else {
                assertThat(message.getContent().toString(), containsString("***Error:"));
                verifyPostCondition(scenario, Scenario.Result.FAILED);
            }

        } catch( MessagingException  | IOException exc ) {
            verifyPostCondition(scenario, Scenario.Result.FAILED);
        }
    }

    public void modify(Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            RpslObject objectForScenario = addRemarks(objectForScenario(scenario));

            final String response = context.getMailUpdatesTestSupport().insert("MODIFY", buildGenericObject(objectForScenario, "mntner: TESTING-MNT").toString() + "password:123");
            final MimeMessage message = context.getMailSenderStub().getMessage(response);

            if (scenario.getResult() == Scenario.Result.SUCCESS) {
                assertThat(message.getContent().toString(), containsString("Modify SUCCEEDED: [mntner] TESTING-MNT"));
                verifyPostCondition(scenario, Scenario.Result.SUCCESS);
            } else {
                assertThat(message.getContent().toString(), containsString("***Error:"));
                verifyPostCondition(scenario, Scenario.Result.FAILED);
            }

        } catch( MessagingException  | IOException exc ) {
            verifyPostCondition(scenario, Scenario.Result.FAILED);
        }
    }

    public void delete(Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            RpslObject objectForScenario = objectForScenario(scenario);

            final String response = context.getMailUpdatesTestSupport().insert("DELETE", buildGenericObject(objectForScenario, "mntner: TESTING-MNT").toString() + "delete: testing\npassword:123");
            final MimeMessage message = context.getMailSenderStub().getMessage(response);

            if (scenario.getResult() == Scenario.Result.SUCCESS) {
                assertThat(message.getContent().toString(), containsString("Delete SUCCEEDED: [mntner] TESTING-MNT"));
                verifyPostCondition(scenario, Scenario.Result.SUCCESS);
            } else {
                assertThat(message.getContent().toString(), containsString("***Error:"));
                verifyPostCondition(scenario, Scenario.Result.FAILED);
            }

        } catch( MessagingException  | IOException exc ) {
            verifyPostCondition(scenario, Scenario.Result.FAILED);
        }
    }
}
