package net.ripe.db.whois.scheduler.task.export;

import net.ripe.db.whois.common.rpsl.Dummifier;
import net.ripe.db.whois.common.rpsl.DummifierProposed;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DecorationStrategyTest {
    RpslObject object;
    @Mock Dummifier dummifier;
    @Mock DummifierProposed dummifierProposed;

    @Before
    public void setUp() throws Exception {
        object = RpslObject.parse("mntner: DEV-MNT");
    }

    @Test
    public void decorate_none() {
        DecorationStrategy subject = new DecorationStrategy.None();
        final RpslObject decorated = subject.decorate(object);

        Assert.assertThat(decorated, Matchers.is(object));
    }

    @Test
    public void decorate_dummify_allowed() {
        DecorationStrategy subject = new DecorationStrategy.Dummify(dummifier);
        Mockito.when(dummifier.isAllowed(3, object)).thenReturn(true);

        final RpslObject dummified = RpslObject.parse("mntner: DEV-MNT");
        Mockito.when(dummifier.dummify(3, object)).thenReturn(dummified);

        final RpslObject decorated = subject.decorate(object);
        Assert.assertThat(decorated, Matchers.is(dummified));

        verify(dummifier).isAllowed(3, object);
        verify(dummifier).dummify(3, object);
    }

    @Test
    public void decorate_dummify_not_allowed() {
        DecorationStrategy subject = new DecorationStrategy.Dummify(dummifier);
        Mockito.when(dummifier.isAllowed(3, object)).thenReturn(false);

        final RpslObject decorated = subject.decorate(object);
        Assert.assertThat(decorated, Matchers.is(Dummifier.PLACEHOLDER_PERSON_OBJECT));

        final RpslObject decoratedSecond = subject.decorate(object);
        Assert.assertNull(decoratedSecond);

        verify(dummifier, Mockito.times(2)).isAllowed(3, object);
        verify(dummifier, never()).dummify(3, object);
    }

    @Test
    public void decorate_dummify_proposed_allowed() {
        DecorationStrategy subject = new DecorationStrategy.DummifyProposed(dummifierProposed);
        final RpslObject object = RpslObject.parse("role: Test Role\nnic-hdl: TR1-TEST");

        when(dummifierProposed.isAllowed(3)).thenReturn(true);
        when(dummifierProposed.dummify(3, object)).thenReturn(object);

        final RpslObject decorated = subject.decorate(object);

        assertThat(object, is(decorated));
        verify(dummifierProposed).isAllowed(3);
        verify(dummifierProposed).dummify(3, object);
    }
}
