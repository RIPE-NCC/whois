package net.ripe.db.whois.query.query;

import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.QueryMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
public class TagValidatorTest {
    private Messages messages;
    private TagValidator subject;

    @BeforeEach
    public void setup() {
        subject = new TagValidator();
        messages = new Messages();
    }

    @Test
    public void both_filter_tag_include_and_exclude() {
        try {
            subject.validate(Query.parse("--filter-tag-include unref --filter-tag-exclude unref TEST-MNT"), messages);
        } catch (QueryException e) {
            assertThat(e.getMessage(), containsString(QueryMessages.invalidCombinationOfFlags("--filter-tag-include (unref)", "--filter-tag-exclude (unref)").toString()));
        }
    }

    @Test
    public void both_filter_tag_include_and_exclude_different_arguments() {
        subject.validate(Query.parse("--filter-tag-include foo --filter-tag-exclude unref TEST-MNT"), messages);
    }

    @Test
    public void filter_tag_include_correct() {
        subject.validate(Query.parse("--filter-tag-include unref TEST-MNT"), messages);
    }

    @Test
    public void filter_tag_exclude_correct() {
        subject.validate(Query.parse("--filter-tag-exclude unref TEST-MNT"), messages);
    }
}
