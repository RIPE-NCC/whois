package net.ripe.db.whois.scheduler.task.export;

import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DecorationStrategyTest {
    RpslObject object;
    @Mock
    DummifierNrtm dummifier;

    @BeforeEach
    public void setUp() throws Exception {
        object = RpslObject.parse("mntner: DEV-MNT");
    }

    @Test
    public void decorate_none() {
        DecorationStrategy subject = new DecorationStrategy.None();
        final RpslObject decorated = subject.decorate(object);

        assertThat(decorated, is(object));
    }

    @Test
    public void decorate_dummify_allowed() {
        DecorationStrategy subject = new DecorationStrategy.DummifySplitFiles(dummifier);
        Mockito.when(dummifier.isAllowed(3, object)).thenReturn(true);

        final RpslObject dummified = RpslObject.parse("mntner: DEV-MNT");
        Mockito.when(dummifier.dummify(3, object)).thenReturn(dummified);

        final RpslObject decorated = subject.decorate(object);
        assertThat(decorated, is(dummified));

        verify(dummifier).isAllowed(3, object);
        verify(dummifier).dummify(3, object);
    }

    @Test
    public void decorate_dummify_not_allowed() {
        DecorationStrategy subject = new DecorationStrategy.DummifySplitFiles(dummifier);
        Mockito.when(dummifier.isAllowed(3, object)).thenReturn(false);

        final RpslObject decorated = subject.decorate(object);
        assertThat(decorated, is(DummifierNrtm.getPlaceholderPersonObject()));

        final RpslObject decoratedSecond = subject.decorate(object);
        assertThat(decoratedSecond, is(nullValue()));

        verify(dummifier, Mockito.times(2)).isAllowed(3, object);
        verify(dummifier, never()).dummify(3, object);
    }

}
