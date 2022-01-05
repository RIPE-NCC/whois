package net.ripe.db.whois.update.handler.validator.keycert;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PgpKeycertValidatorTest {

    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock DateTimeProvider dateTimeProvider;
    @InjectMocks PgpKeycertValidator subject;
    List<Message> messages;

    @BeforeEach
    public void setup() {
        messages = Lists.newArrayList();
        lenient().doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                messages.add((Message) args[2]);
                return null;
            }
        }).when(updateContext).addMessage(any(UpdateContainer.class), any(RpslAttribute.class), any(Message.class));
        lenient().when(dateTimeProvider.getCurrentDateTime()).thenReturn(LocalDateTime.now());
    }

    @Test
    public void get_actions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE));
    }

    @Test
    public void get_types() {
        assertThat(subject.getTypes(), containsInAnyOrder(ObjectType.KEY_CERT));
    }

    @Test
    public void pgp_key_smaller_than_minimum_key_length() {
        final RpslObject object = RpslObject.parse(
               "key-cert:     PGPKEY-D8F37DA3\n" +
               "method:       pGP\n" +
               "certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
               "certif:       Version: GnuPG v1.0.7 (GNU/Linux)\n" +
               "certif:\n" +
               "certif:       mQGiBD6BsrgRBACJW9zbjuY1fuIAOWGGWRpEzaCfdY/ixvwJ8WpQ0LhhGtdjFmCb\n" +
               "certif:       S2FAQuwTWcbHPlt+1dYi1aZcPzU8P54CI3yOl3aO7MToe7YkjY9ANzy9WvxRQcSr\n" +
               "certif:       n/jM50ugKcHTK6ounkpj7mAcUvoeL4WQjLmdAojgQNCWCYlcVXg5i9pRhwCgmYTI\n" +
               "certif:       vZzJclgpMpORIpuOxiKh2h0D/0o9FYAoYBB11FKnRsOAC399ZdmN4gYel5CFJNoV\n" +
               "certif:       1pasVdCznqkjxODe/3VyAFSX1RPrVd0kuTxb4qmnMYr0nekznNMg5TYpRg4Uulbz\n" +
               "certif:       oMoso3mrnrJ7U6doWE143YzGSEzyaD5lzTZ3uacWU9DjzaDAsmhCeLqklfocPfOb\n" +
               "certif:       F4eZA/wI6F+kcGhRjwmfzh4iDiB54dlLIxHS4/vPgqmVDfXmqYx7o2Eks9S145+z\n" +
               "certif:       yzks35AtDk7k6JYEB4xGeMZKvf9/V2E6lzo3a8OKqXcI9rHAeHtSdb+cBxCCd6zf\n" +
               "certif:       cO0eWQvkeHP1yucFDOiDOEpBJjuWcpawVJrG+Gq6m19sEswWxLQkVGVzdCBQZXJz\n" +
               "certif:       b24gKFRlc3QpIDxkYnRlc3RAcmlwZS5uZXQ+iFkEExECABkFAj6BsrgECwcDAgMV\n" +
               "certif:       AgMDFgIBAh4BAheAAAoJEJrYZFrY832jW+4AnRXEk4fOtix8ynErwsh68yrss16H\n" +
               "certif:       AJsEYkdtXwMM69Nd+JWsiEk+48ns3w==\n" +
               "certif:       =3+2Y\n" +
               "certif:       -----END PGP PUBLIC KEY BLOCK-----\n" +
               "mnt-by:       TST-MNT\n" +
               "source:       TEST");

        when(update.getUpdatedObject()).thenReturn(object);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.publicKeyLengthIsWeak("DSA", 2048, 1024));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void pgp_key_has_minimum_key_length() {
        final RpslObject object = RpslObject.parse("" +
                "key-cert:     PGPKEY-28F6CD6C\n" +
                "method:       PgP\n" +
                "certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:       Version: GnuPG v1.4.11 (Darwin)\n" +
                "certif:\n" +
                "certif:       mQENBE841dMBCAC80IDqJpJC7ch16NEaWvLDM8CslkhiqYk9fgXgUdMNuBsJJ/KV\n" +
                "certif:       4oUwzrX+3lNvMPSoW7yRfiokFQ48IhYVZuGlH7DzwsyfS3MniXmw6/vT6JaYPuIF\n" +
                "certif:       7TmMHIIxQbzJe+SUrauzJ2J0xQbnKhcfuLkmNO7jiOoKGJWIrO5wUZfd0/4nOoaz\n" +
                "certif:       RMokk0Paj6r52ZMni44vV4R0QnuUJRNIIesPDYDkOZGXX1lD9fprTc2DJe8tjAu0\n" +
                "certif:       VJz5PpCHwvS9ge22CRTUBSgmf2NBHJwDF+dnvijLuoDFyTuOrSkq0nAt0B9kTUxt\n" +
                "certif:       Bsb7mNxlARduo5419hBp08P07LJb4upuVsMPABEBAAG0HkVkIFNocnlhbmUgPGVz\n" +
                "certif:       aHJ5YW5lQHJpcGUubmV0PokBOAQTAQIAIgUCTzjV0wIbAwYLCQgHAwIGFQgCCQoL\n" +
                "certif:       BBYCAwECHgECF4AACgkQ7pke4ij2zWyUKAf+MmDQnBUUSjDeFvCnNN4JTraMXFUi\n" +
                "certif:       Ke2HzVnLvT/Z/XN5W6TIje7u1luTJk/siJJyKYa1ZWQoVOCXruTSge+vP6LxENOX\n" +
                "certif:       /sOJ1YxWHJUr3OVOfW2NoKBaUkBBCxi/CSaPti7YPHF0D6rn3GJtoJTnLL4KPnWV\n" +
                "certif:       gtja4FtpsgwhiPF/jVmx6/d5Zc/dndDLZZt2sMjh0KDVf7F03hsF/EAauBbxMLvK\n" +
                "certif:       yEHMdw7ab5CxeorgWEDaLrR1YwHWHy9cbYC00Mgp1zQR1ok2wN/XZVL7BZYPS/UC\n" +
                "certif:       H03bFi3AcN1Vm55QpbU0QJ4qPN8uwYc5VBFSSYRITUCwbB5qBO5kIIBLP7kBDQRP\n" +
                "certif:       ONXTAQgA16kMTcjxOtkU8v3sLAIpr2xWwG91BdB2fLV0aUgaZWfexKMnWDu8xpm1\n" +
                "certif:       qY+viF+/emdXBc/C7QbFUmhmXCslX5kfD10hkYFTIqc1Axk5Ya8FZtwHFpo0TVTl\n" +
                "certif:       sGodZ2gy8334rT9yMH+bZNSlZ+07Fxa7maC1ycxPPL/68+LSBy6wWlAFCwwr7XwN\n" +
                "certif:       LGnrBbELgvoi04yMu1EpqAvxZLH1TBgzrFcWzXJjj1JKIB1RGapoDc3m7dvHa3+e\n" +
                "certif:       27aQosQnNVNWrHiS67zqWoC963aNuHZBY174yfKPRaN6s5GppC2hMPYGnJV07yah\n" +
                "certif:       P0mwRcp4e3AaJIg2SP9CUQJKGPY+mQARAQABiQEfBBgBAgAJBQJPONXTAhsMAAoJ\n" +
                "certif:       EO6ZHuIo9s1souEH/ieP9J69j59zfVcN6FimT86JF9CVyB86PGv+naHEyzOrBjml\n" +
                "certif:       xBn2TPCNSE5KH8+gENyvYaQ6Wxv4Aki2HnJj5H43LfXPZZ6HNME4FPowoIkumc9q\n" +
                "certif:       mndn6WXsgjwT9lc2HQmUgolQObg3JMBRe0rYzVf5N9+eXkc5lR/PpTOHdesP17uM\n" +
                "certif:       QqtJs2hKdZKXgKNufSypfQBLXxkhez0KvoZ4PvrLItZTZUjrnRXdObNUgvz5/SVh\n" +
                "certif:       4Oqesj+Z36YNFrsYobghzIqOiP4hINsm9mQoshz8YLZe0z7InwcFYHp7HvQWEOyj\n" +
                "certif:       kSYadR4aN+CVhYHOsn5nxbiKSFNAWh40q7tDP7I=\n" +
                "certif:       =XRho\n" +
                "certif:       -----END PGP PUBLIC KEY BLOCK-----\n" +
                "mnt-by:       UPD-MNT\n" +
                "notify:       noreply@ripe.net\n" +
                "source:       TEST\n");

        when(update.getUpdatedObject()).thenReturn(object);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void pgp_key_greater_than_minimum_key_length() {
        final RpslObject object = RpslObject.parse(
                "key-cert:       PGPKEY-E7220D0A\n" +
                "method:         PGP\n" +
                "owner:          Testing 4096 bit key <long-key@ripe.net>\n" +
                "fingerpr:       2C4A A5B5 19BC D919 9585  0A90 69FE 3396 E722 0D0A\n" +
                "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:         Version: GnuPG v1.4.12 (Darwin)\n" +
                "certif:         Comment: GPGTools - http://gpgtools.org\n" +
                "certif:\n" +
                "certif:         mQINBFDqnZUBEADY+QqoS4tE7SmAGvwgOCC5d4sTctRVF27lrNaxGivPde+e2IDK\n" +
                "certif:         LL1k3c0RTTbULurYr9KSLisSRo/Rucj1PjMPdwaUbMGE2WoBNXSWb2B2aoWxw6W2\n" +
                "certif:         t0knjtDRgFXe8n3lHLlXk+NgJ7Cz3FmgoavNtkFeYHnHt5ulMeGug4iOGdyZs+Ly\n" +
                "certif:         5YKjnnXd1HO2KBkG1QzFM2uVYpIeA9DuzLSkWN7R2avW8rmj5lLg21XTmtOwiq2u\n" +
                "certif:         vSSaKTzr4JwuJ/mKgIhJFGaho6YCzZ7MKKBevHpqze2/JPvyR7IX27qZVqwLJQ3Z\n" +
                "certif:         3CEByLqn07WjsvaeE+yFmzwsX9dfhz4nP53475Bddk60eHq32rn0ctFqk3h5aZV4\n" +
                "certif:         aTUyK2uWM7SNeJJBxl4IyvKU5fKUwZXYendt0UPzXRqQOf7L1eL8vy2FqXiWkJZd\n" +
                "certif:         Sxzo7kl4E0Zn7tJWii2qkCp0sx4zeozcIIkDb9SUJo5WMzfqRGeZccxLuAJMIdA5\n" +
                "certif:         9AmCQ71ZPwPsE/A/KrxM7sI+Xq2A49+OMOQAXdHxVmg6tNZuHTB1Cj8TJhQ1eMzd\n" +
                "certif:         +NLPLcu34tOTJjw7L9xFHCR8UjE/cfmH3bGCCYzth6ywFg2/OCvToWv0tQjIkp6p\n" +
                "certif:         HCKmdy57PXxTA3WVHSMpQ8yxa6E9Yf2wgcx4xptej/ljcVuFyQ6B4ZkbLwARAQAB\n" +
                "certif:         tChUZXN0aW5nIDQwOTYgYml0IGtleSA8bG9uZy1rZXlAcmlwZS5uZXQ+iQI4BBMB\n" +
                "certif:         AgAiBQJQ6p2VAhsDBgsJCAcDAgYVCAIJCgsEFgIDAQIeAQIXgAAKCRBp/jOW5yIN\n" +
                "certif:         ChibEADBvlU/8fWNQ0ozu+EAf6vj7QmeG2VF/rXxfhu4ha5VPMLT4v94L3autmay\n" +
                "certif:         BfH09ZIpgvHFWjwMS/8oFnhTm2U1kvRGHrtLKupGWzeoX2bHB6ZXfx56xW56hzjc\n" +
                "certif:         r1LOODJe+mBCmiEYQNuJMf0lpw8BF74Na6iWtsHJGPQcvcjN8mHc3PmxD94tfTY2\n" +
                "certif:         d/0uJaceif5sVKt4sCBqLdXgeqgNC7RxQyVgkGM+3Gfxwar9v/c2Pq9/n1MOZEI0\n" +
                "certif:         lpoVMZUftUssi2/7Et9DJk8X1+oTCLzYtofK9CKPD0LEoBvvvMOSetiDwId5qVfV\n" +
                "certif:         KjCngnX0WKCIkPouYAIGE2nrELGTX7WrkMYFhomkoBcBc+GMWDo1c6SzV0VuvYvn\n" +
                "certif:         gZUwZN5D+SUA2jIT42JaV3mV2zZc8slsrj8dErakTjMQII9i6o29q7VKtx0zr+gV\n" +
                "certif:         TEmZ1ZVSxK+vs1ZKdSCP0OFiiyxEYuErmJH008Gt/Kw432utnx7v7CbvZC4omUNt\n" +
                "certif:         +K6yhXcyLZwdf+N+SM/2mgI2S0Eq3UhtYMBnxu/i1v+NaExvzVEJHDspFvaV0/PR\n" +
                "certif:         ma9QXAmreeLeRTQ1irubsz75pPqSamzh5gMHgr6e5JDEJIRfVtwOj9bigDqNDM5o\n" +
                "certif:         9xgg7J69qoSiGtvQoWYEfISfHovcqY4QNn4iWebVaCDIP2HkgbkCDQRQ6p2VARAA\n" +
                "certif:         6kaXaUHPmpMpgsRsCTRZgwzXSFT93/Rj7VVP7R3hUtt19c9tR6qq4vcXqIa/QAr7\n" +
                "certif:         eeWrxQm1F0vdB5boLua7VNf/fdB5G6dCA8PIN8NPnLPFGA4PPCTAHj78h61BeFLB\n" +
                "certif:         WmXlTvtvc3M33k0upW0t7X3MBHNyLGejA9mv9gPQuAMk4+GZbLYggkxaPMXjOS2u\n" +
                "certif:         L/jqah7nUQlFIc5ScNMV6E1mLfeCpBAXmijnDkVwb8R00/fcdHVl5OSbYXZACLXI\n" +
                "certif:         X50dzjIZQ7k1UUStw0knoZz33LJjhJ5zdxHLSjeT2ozqRjT9tJaB8NzpP2sa2qTX\n" +
                "certif:         5EXPHUy5OjX2BsToQcTsBvGXSquB0Z2u1h6RD23CQNkFgah3WE6kCAz1n1im+/Uy\n" +
                "certif:         BnOgVKmsbaKsyd2ZtuLwBQRRmU4Qlb+2lIId9rM6fG9VmS2A3KsP1DwphmLvTwT0\n" +
                "certif:         p2ubCi1Lm5wPouZftsrt9mrr1Q9xDBHZleWjXZFrVKxoKzdtwarnw9sHr3oXnLul\n" +
                "certif:         00iP2gXyXlknPA1Msq8eXcC6iX/ofTTgQF2xUqBrhq5exOwiSD0jQA2iklZTYjw8\n" +
                "certif:         EXkHImnkIS4k66QEjVnoEqVxCR7JJqD+BatMLvmv+2tGJXYFsxFQ8Zk0IwuJKbv6\n" +
                "certif:         0OQHFOhSlbW2F5GDKeb0ZfVoGcXQndxiW4vhrsRP88cAEQEAAYkCHwQYAQIACQUC\n" +
                "certif:         UOqdlQIbDAAKCRBp/jOW5yINCvlRD/0fViWEjt1QLxdj1/ml4B7SiS5fabE9fnRj\n" +
                "certif:         UtFjPHGI+tJb2aAelOPChuxTh0T5YxFHaIMjRA+a9DjutBjnmYoSGLlp3ZIFrAzF\n" +
                "certif:         qOXP4DYP/Teg+QgbatjsZMdU8K6B6G7JlJWrLGMURSsZ3sKVyv6NlKAGkfKJiKyL\n" +
                "certif:         ss8wsTDm8VVQJH+WXntKMdFUdL1K6MGq4ijm3b3lFMvjZrP12T6l0rr5t/lihPhc\n" +
                "certif:         MfXfw/idD5y3d3gavW0klfLf3iZE9SXKaumNPw6CDAMqegeSiXQVxFoal1vO7rSm\n" +
                "certif:         CifYePPc7VMLDy+1JW9IM8rzaYUpf7FXkY07GIInFNe4ibV0P40oGxspdFR6irwj\n" +
                "certif:         LwHPxeHap17N3ca/xdesm5kIfbxE/c7Q2FeZ7rYN88ElVMJiC+ETJC4dYqR5S4sG\n" +
                "certif:         OMvMoIkjA8I17rBiNE65voCRgUxoqj5sbAXnPZQf+rG7IeWdiCvT50gMTKG4exrG\n" +
                "certif:         LcT0ByruHmAfISqDvhUqrV9RoH0oBWyV/LKa0DDwsUusJFDuvIxCq1fqKHv6IF+q\n" +
                "certif:         Edjou/3zMx/gqnq6Hudtfs78Rbw1REJPidEmPh3k9GV7tPwSFGYaSvWfB1gbVond\n" +
                "certif:         ps58MLBDWKEJyHm0GZq2ookGyZg5Fy0SBBJ3r932OxT4UFoSZn7Evrkzkw3WzpZ0\n" +
                "certif:         xulqMmMYIQ==\n" +
                "certif:         =Iya+\n" +
                "certif:         -----END PGP PUBLIC KEY BLOCK-----\n" +
                "notify:         noreply@ripe.net\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST");

//        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getUpdatedObject()).thenReturn(object);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void pgp_key_is_revoked() {
        final RpslObject object = RpslObject.parse(
                "key-cert:       PGPKEY-8947C26B\n" +
                "method:         PGP\n" +
                "owner:          Test User <revoked@ripe.net>\n" +
                "fingerpr:       610A 2457 2BA3 A575 5F85  4DD8 5E62 6C72 C88C A438\n" +
                "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:         Comment: GPGTools - http://gpgtools.org\n" +
                "certif:         \n" +
                "certif:         mI0EXFm2FwEEALc4QJzSrefgg33AOHhS45L2kbSNTcXNVmVfk5ra2h3kr9ia8C5I\n" +
                "certif:         yLBz78108XD+0QwdMM3/acaJPqUxOkVzmwf5ydd1nJn1ZeLznfrSWnvb4DSxNGeU\n" +
                "certif:         yVm8j6I53Ay5WDEJWUu3XQzUHnqYeb3Fcwa5MjPzf8iBbFmdi6riLLBHABEBAAGI\n" +
                "certif:         tgQgAQgAIBYhBHSJUuDx58YlX1PWwpKhmKCJR8JrBQJcWbZ5Ah0AAAoJEJKhmKCJ\n" +
                "certif:         R8Jr6kkD/10EHYfhXVxwF5zeH6hMKEBQLYtJMo2fcK7055njT6PTS3tVWnjQ2UDB\n" +
                "certif:         8ExA34/LJuDXn19qZSpAM4NP2SwxpC8kPecvY+0Akdu3mwV8X525/A4eQ1l0+pF6\n" +
                "certif:         TL0gF9+kLLRyIg9Qbme1tf2734gu8JfKZek83O/9prv1xnsYz3ArtBxUZXN0IFVz\n" +
                "certif:         ZXIgPHJldm9rZWRAcmlwZS5uZXQ+iM4EEwEIADgWIQR0iVLg8efGJV9T1sKSoZig\n" +
                "certif:         iUfCawUCXFm2FwIbAwULCQgHAgYVCgkICwIEFgIDAQIeAQIXgAAKCRCSoZigiUfC\n" +
                "certif:         a2BDBACEP9SlUsPCRcsAFlM/lg/7prXSLzZhi3gpVYKkEDKRafpBBa5XcfmoSSiY\n" +
                "certif:         r6hcwq5r5O0ezfLVi75VeZq+R8CsaSWDqp0FFS7n/2o87PIyZog0fyqrJIt/97Tn\n" +
                "certif:         mOpX1wWbFEBC25k52jUP10VA7jPDq12b/8BrqCSD+aD5y7rVR7iNBFxZthcBBADO\n" +
                "certif:         z3HfUAcmk/DeFOJWjYhUj/b0m+pAG/2PLEUrj9DelJeRwEa8dTUN1AaTdn5pvf2p\n" +
                "certif:         qPXPr9EHRSdhum5kFq4SkrEW9wYrvdfYVAs4UTCC/xjP6JDAYRWc153yzJaFeRk4\n" +
                "certif:         TZn76PA957bekHTKk1jwOgJmqQ+Mjpzv1IK4vZnvswARAQABiLYEGAEIACAWIQR0\n" +
                "certif:         iVLg8efGJV9T1sKSoZigiUfCawUCXFm2FwIbDAAKCRCSoZigiUfCa5mzA/sF3aZW\n" +
                "certif:         m2+4zh9w1qWHkARTu4aB8YzaT7cLihMYS94h/wzcJPbMDmhUJZNtVkKC2OEvaeSw\n" +
                "certif:         RpZrD3N2Cq5uuELopJhaDFpnKntc2NmmUn8P06gW0Ep1uyObPJfID/xDWznZH8SQ\n" +
                "certif:         357CfW0mENdBquWnAtGxABuv//JeCp0Ar3WkEg==\n" +
                "certif:         =Ofyo\n" +
                "certif:         -----END PGP PUBLIC KEY BLOCK-----\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        when(update.getUpdatedObject()).thenReturn(object);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.publicKeyIsRevoked("8947C26B"));
        verify(updateContext).addMessage(update, UpdateMessages.publicKeyLengthIsWeak("RSA", 2048, 1024));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void pgp_key_is_expired() {
        final RpslObject object = RpslObject.parse(
                "key-cert:       PGPKEY-C88CA438\n" +
                "method:         PGP\n" +
                "owner:          Expired <expired@ripe.net>\n" +
                "fingerpr:       610A 2457 2BA3 A575 5F85  4DD8 5E62 6C72 C88C A438\n" +
                "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:         Version: GnuPG v1.4.12 (Darwin)\n" +
                "certif:         Comment: GPGTools - http://gpgtools.org\n" +
                "certif:\n" +
                "certif:         mI0EUOoKSgEEAMvJBJzUBKDA8BGK+KpJMuGSOXnQgvymxgyOUOBVkLpeOcPQMy1A\n" +
                "certif:         4fffXJ4V0xdlqtikDATCnSIBS17ihi7xD8fUvKF4dJrq+rmaVULoy06B68IcfYKQ\n" +
                "certif:         yoRJqGii/1Z47FuudeJp1axQs1JER3OJ64IHuLblFIT7oS+YWBLopc1JABEBAAG0\n" +
                "certif:         GkV4cGlyZWQgPGV4cGlyZWRAcmlwZS5uZXQ+iL4EEwECACgFAlDqCkoCGwMFCQAB\n" +
                "certif:         UYAGCwkIBwMCBhUIAgkKCwQWAgMBAh4BAheAAAoJEF5ibHLIjKQ4tEMD/j8VYxdY\n" +
                "certif:         V6JM8rDokg+zNE4Ifc7nGaUrsrF2YRmcIg6OXVhPGLIqfQB2IsKub595sA1vgwNs\n" +
                "certif:         +Cg0tzaQfzWh2Nz5NxFGnDHm5tPfOfiADwpMuLtZby390Wpbwk7VGZMqfcDXt3uy\n" +
                "certif:         Ch4rvayDTtzQqDVqo1kLgK5dIc/UIlX3jaxWuI0EUOoKSgEEANYcEMxrEGD4LSgk\n" +
                "certif:         vHVECSOB0q32CN/wSrvVzL6hP8RuO0gwwVQH1V8KCYiY6kDEk33Qb4f1bTo+Wbi6\n" +
                "certif:         9yFvn1OvLh3/idb3U1qSq2+Y6Snl/kvgoVJQuS9x1NePtCYL2kheTAGiswg6CxTF\n" +
                "certif:         RZ3c7CaNHsCbUdIpQmNUxfcWBH3PABEBAAGIpQQYAQIADwUCUOoKSgIbDAUJAAFR\n" +
                "certif:         gAAKCRBeYmxyyIykON13BACeqmXZNe9H/SK2AMiFLIx2Zfyw/P0cKabn3Iaan7iF\n" +
                "certif:         kSwrZQhF4571MBxb9U41Giiyza/t7vLQH1S+FYFUqfWCa8p1VQDRavi4wDgy2PDp\n" +
                "certif:         ouhDqH+Mfkqb7yv40kYOUJ02eKkdgSgwTEcpfwq9GU4kJLVO5O3Y3nOEAx736gPQ\n" +
                "certif:         xw==\n" +
                "certif:         =XcVO\n" +
                "certif:         -----END PGP PUBLIC KEY BLOCK-----\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        when(update.getUpdatedObject()).thenReturn(object);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.publicKeyHasExpired("C88CA438"));
        verify(updateContext).addMessage(update, UpdateMessages.publicKeyLengthIsWeak("RSA", 2048, 1024));
        verifyNoMoreInteractions(updateContext);
    }
}
