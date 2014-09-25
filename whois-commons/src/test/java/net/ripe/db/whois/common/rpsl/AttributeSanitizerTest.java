package net.ripe.db.whois.common.rpsl;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.CIString;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AttributeSanitizerTest {
    @Mock DateTimeProvider dateTimeProvider;
    @Mock ObjectMessages objectMessages;
    @InjectMocks AttributeSanitizer attributeSanitizer;

    @Test
    public void transform_domain_no_dot() {
        final RpslObject rpslObject = RpslObject.parse("domain:          17.45.212.in-addr.arpa");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getKey().toString(), is("17.45.212.in-addr.arpa"));
        assertThat(result.getValueForAttribute(AttributeType.DOMAIN).toString(), is("17.45.212.in-addr.arpa"));

        verifyZeroInteractions(objectMessages);
    }

    @Test
    public void transform_key_domain_no_dot() {
        final RpslObject rpslObject = RpslObject.parse("domain:          17.45.212.in-addr.arpa");

        final CIString result = attributeSanitizer.sanitizeKey(rpslObject);
        assertThat(result.toString(), is("17.45.212.in-addr.arpa"));
    }

    @Test
    public void transform_domain_with_trailing_dot() {
        final RpslObject rpslObject = RpslObject.parse("domain:          17.45.212.in-addr.arpa.");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getKey().toString(), is("17.45.212.in-addr.arpa"));
        assertThat(result.getValueForAttribute(AttributeType.DOMAIN).toString(), is("17.45.212.in-addr.arpa"));

        verify(objectMessages).addMessage(result.getTypeAttribute(), ValidationMessages.attributeValueConverted("17.45.212.in-addr.arpa.", "17.45.212.in-addr.arpa"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_keys_domain_with_trailing_dot() {
        final RpslObject rpslObject = RpslObject.parse("domain:          17.45.212.in-addr.arpa.");

        final CIString result = attributeSanitizer.sanitizeKey(rpslObject);
        assertThat(result.toString(), is("17.45.212.in-addr.arpa"));
    }

    @Test
    public void transform_ds_rdata_no_change() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "domain:            17.45.212.in-addr.arpa\n" +
                "ds-rdata:          52314 5 1 93B5837D4E5C063A3728FAA72BA64068F89B39DF");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);

        assertThat(result.getValueForAttribute(AttributeType.DS_RDATA).toString(), is("52314 5 1 93B5837D4E5C063A3728FAA72BA64068F89B39DF"));

        verifyZeroInteractions(objectMessages);
    }

    @Test
    public void transform_ds_rdata_remove_spaces() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "domain:            17.45.212.in-addr.arpa\n" +
                "ds-rdata:          52314 5 1 93B5837D4E5C063 A3728FAA72BA64 068F89B39DF");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);

        assertThat(result.getValueForAttribute(AttributeType.DS_RDATA).toString(), is("52314 5 1 93B5837D4E5C063A3728FAA72BA64068F89B39DF"));
        verify(objectMessages).addMessage(result.findAttribute(AttributeType.DS_RDATA), ValidationMessages.attributeValueConverted("52314 5 1 93B5837D4E5C063 A3728FAA72BA64 068F89B39DF", "52314 5 1 93B5837D4E5C063A3728FAA72BA64068F89B39DF"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_ds_rdata_remove_parentheses() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "domain:            17.45.212.in-addr.arpa\n" +
                "ds-rdata:          52314 5 1 ( 93B5837D4E5C063A3728FAA72BA64068F89B39DF )");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);

        assertThat(result.getValueForAttribute(AttributeType.DS_RDATA).toString(), is("52314 5 1 93B5837D4E5C063A3728FAA72BA64068F89B39DF"));
        verify(objectMessages).addMessage(result.findAttribute(AttributeType.DS_RDATA), ValidationMessages.attributeValueConverted("52314 5 1 ( 93B5837D4E5C063A3728FAA72BA64068F89B39DF )", "52314 5 1 93B5837D4E5C063A3728FAA72BA64068F89B39DF"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_ds_rdata_remove_spaces_and_parentheses() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "domain:            17.45.212.in-addr.arpa\n" +
                "ds-rdata:          52314 5 1 ( 93B5837D4E5C063 A3728FAA72BA64 068F89B39DF )");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);

        assertThat(result.getValueForAttribute(AttributeType.DS_RDATA).toString(), is("52314 5 1 93B5837D4E5C063A3728FAA72BA64068F89B39DF"));
        verify(objectMessages).addMessage(result.findAttribute(AttributeType.DS_RDATA), ValidationMessages.attributeValueConverted("52314 5 1 ( 93B5837D4E5C063 A3728FAA72BA64 068F89B39DF )", "52314 5 1 93B5837D4E5C063A3728FAA72BA64068F89B39DF"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_person() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "person: Test Person\n" +
                "nic-hdl: TP1-RIPE");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result, is(rpslObject));

        verifyZeroInteractions(objectMessages);
    }

    @Test
    public void transform_inetnum_no_change() {
        final RpslObject rpslObject = RpslObject.parse("inetnum: 193.0.0.0 - 193.255.255.255");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result, is(rpslObject));

        verifyZeroInteractions(objectMessages);
    }


    @Test
    public void transform_inetnum_leading_zeroes() {
        final RpslObject rpslObject = RpslObject.parse("inetnum: 010.0.0.01 - 193.255.255.098");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.INETNUM).toString(), is("10.0.0.1 - 193.255.255.98"));
        verify(objectMessages).addMessage(result.getTypeAttribute(), ValidationMessages.attributeValueConverted("010.0.0.01 - 193.255.255.098", "10.0.0.1 - 193.255.255.98"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_inetnum_change() {
        final RpslObject rpslObject = RpslObject.parse("inetnum: 192.0.0.0/4");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.INETNUM).toString(), is("192.0.0.0 - 207.255.255.255"));
        verify(objectMessages).addMessage(result.getTypeAttribute(), ValidationMessages.attributeValueConverted("192.0.0.0/4", "192.0.0.0 - 207.255.255.255"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_inetnum_prefix() {
        final RpslObject rpslObject = RpslObject.parse("inetnum: 193/8");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.INETNUM).toString(), is("193.0.0.0 - 193.255.255.255"));
        verify(objectMessages).addMessage(result.getTypeAttribute(), ValidationMessages.attributeValueConverted("193/8", "193.0.0.0 - 193.255.255.255"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_inetnum_prefix_with_comment() {
        final RpslObject rpslObject = RpslObject.parse("inetnum:    193/8 # Comment");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.toString(), is("inetnum:        193.0.0.0 - 193.255.255.255 # Comment\n"));
        verify(objectMessages).addMessage(result.getTypeAttribute(), ValidationMessages.attributeValueConverted("193/8", "193.0.0.0 - 193.255.255.255"));
        verify(objectMessages).addMessage(result.getTypeAttribute(), ValidationMessages.remarksReformatted());

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_inet6num_no_change() {
        final RpslObject rpslObject = RpslObject.parse("inet6num: 2001:67c:20c4::/48");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.INET6NUM).toString(), is("2001:67c:20c4::/48"));

        verifyZeroInteractions(objectMessages);
    }

    @Test
    public void transform_inet6num_leading_zeroes() {
        final RpslObject rpslObject = RpslObject.parse("inet6num: 2001:067c:20c4::/48");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.INET6NUM).toString(), is("2001:67c:20c4::/48"));
        verify(objectMessages).addMessage(result.getTypeAttribute(), ValidationMessages.attributeValueConverted("2001:067c:20c4::/48", "2001:67c:20c4::/48"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_inet6num_uppercase() {
        final RpslObject rpslObject = RpslObject.parse("inet6num: 2001:67C:20C4::/48");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.INET6NUM).toString(), is("2001:67c:20c4::/48"));
        verify(objectMessages).addMessage(result.getTypeAttribute(), ValidationMessages.attributeValueConverted("2001:67C:20C4::/48", "2001:67c:20c4::/48"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_inet6num_trailing_zero() {
        final RpslObject rpslObject = RpslObject.parse("inet6num: 2001:67c:20c4::0/48");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.INET6NUM).toString(), is("2001:67c:20c4::/48"));
        verify(objectMessages).addMessage(result.getTypeAttribute(), ValidationMessages.attributeValueConverted("2001:67c:20c4::0/48", "2001:67c:20c4::/48"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_inet6num_prefix_with_comment() {
        final RpslObject rpslObject = RpslObject.parse("inet6num: 2001:67c:20c4::0/48 # Comment");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.toString(), is("inet6num:       2001:67c:20c4::/48 # Comment\n"));
        verify(objectMessages).addMessage(result.getTypeAttribute(), ValidationMessages.attributeValueConverted("2001:67c:20c4::0/48", "2001:67c:20c4::/48"));
        verify(objectMessages).addMessage(result.getTypeAttribute(), ValidationMessages.remarksReformatted());

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_inet6num_change() {
        final RpslObject rpslObject = RpslObject.parse("inet6num: 2001::/8");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.INET6NUM).toString(), is("2000::/8"));
        verify(objectMessages).addMessage(result.getTypeAttribute(), ValidationMessages.attributeValueConverted("2001::/8", "2000::/8"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_inet6num_change_key() {
        final RpslObject rpslObject = RpslObject.parse("inet6num: 2001:0600:0000:0000:0000:0000:0000:0000 - 2001:0600:0000:0000:ffff:ffff:ffff:ffff");

        final CIString resultKey = attributeSanitizer.sanitizeKey(rpslObject);
        assertThat(resultKey.toString(), is("2001:0600:0000:0000:0000:0000:0000:0000 - 2001:0600:0000:0000:ffff:ffff:ffff:ffff"));
    }

    @Test
    public void transform_nserver_no_not() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "domain:          17.45.212.in-addr.arpa\n" +
                "nserver:         hostname.nu\n");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.NSERVER).toString(), is("hostname.nu"));

        verifyZeroInteractions(objectMessages);
    }

    @Test
    public void transform_nserver_with_dot() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "domain:          17.45.212.in-addr.arpa\n" +
                "nserver:         hostname.nu.\n");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.NSERVER).toString(), is("hostname.nu"));
        verify(objectMessages).addMessage(result.findAttribute(AttributeType.NSERVER), ValidationMessages.attributeValueConverted("hostname.nu.", "hostname.nu"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_nserver_glue_ipv4() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "domain:          17.45.212.in-addr.arpa\n" +
                "nserver:         hostname.nu 10.0.0.0\n");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.NSERVER).toString(), is("hostname.nu 10.0.0.0"));

        verifyZeroInteractions(objectMessages);
    }

    @Test
    public void transform_nserver_glue_ipv4_prefix() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "domain:          17.45.212.in-addr.arpa\n" +
                "nserver:         hostname.nu 10.0.0.0/32\n");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.NSERVER).toString(), is("hostname.nu 10.0.0.0"));
        verify(objectMessages).addMessage(result.findAttribute(AttributeType.NSERVER), ValidationMessages.attributeValueConverted("hostname.nu 10.0.0.0/32", "hostname.nu 10.0.0.0"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_nserver_glue_ipv6() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "domain:          17.45.212.in-addr.arpa\n" +
                "nserver:         hostname.nu FFAA::0\n");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.NSERVER).toString(), is("hostname.nu ffaa::"));
        verify(objectMessages).addMessage(result.findAttribute(AttributeType.NSERVER), ValidationMessages.attributeValueConverted("hostname.nu FFAA::0", "hostname.nu ffaa::"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_route_no_change() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "route:           212.166.64.0/19\n" +
                "origin:          AS12321");


        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.ROUTE).toString(), is("212.166.64.0/19"));

        verifyZeroInteractions(objectMessages);
    }

    @Test
    public void transform_route_leading_zeroes() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "route:           212.166.064.000/19\n" +
                "origin:          AS12321");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.ROUTE).toString(), is("212.166.64.0/19"));
        verify(objectMessages).addMessage(result.getTypeAttribute(), ValidationMessages.attributeValueConverted("212.166.064.000/19", "212.166.64.0/19"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_route_short_prefix() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "route:           15/8\n" +
                "origin:          AS12321");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.ROUTE).toString(), is("15.0.0.0/8"));
        verify(objectMessages).addMessage(result.getTypeAttribute(), ValidationMessages.attributeValueConverted("15/8", "15.0.0.0/8"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_route6_no_change() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "route6:          2001:1578:200::/40\n" +
                "origin:          AS12321");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.ROUTE6).toString(), is("2001:1578:200::/40"));

        verifyZeroInteractions(objectMessages);
    }

    @Test
    public void transform_route6_leading_zeroes() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "route6:          2001:1578:0200::/40\n" +
                "origin:          AS12321");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.ROUTE6).toString(), is("2001:1578:200::/40"));
        verify(objectMessages).addMessage(result.findAttribute(AttributeType.ROUTE6), ValidationMessages.attributeValueConverted("2001:1578:0200::/40", "2001:1578:200::/40"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_route6_trailing_zero() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "route6:          2001:1578:200::0/40\n" +
                "origin:          AS12321");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.ROUTE6).toString(), is("2001:1578:200::/40"));
        verify(objectMessages).addMessage(result.findAttribute(AttributeType.ROUTE6), ValidationMessages.attributeValueConverted("2001:1578:200::0/40", "2001:1578:200::/40"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_route6_uppercase() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "route6:          2001:6F8::/32\n" +
                "origin:          AS12321");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.ROUTE6).toString(), is("2001:6f8::/32"));
        verify(objectMessages).addMessage(result.findAttribute(AttributeType.ROUTE6), ValidationMessages.attributeValueConverted("2001:6F8::/32", "2001:6f8::/32"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_alias_no_dot() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inet-rtr:          test.ripe.net\n" +
                "alias:          alias.ripe.net");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.ALIAS).toString(), is("alias.ripe.net"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_alias_with_trailing_dot() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inet-rtr:          test.ripe.net\n" +
                "alias:          alias.ripe.net.");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.ALIAS).toString(), is("alias.ripe.net"));
        verify(objectMessages).addMessage(result.findAttribute(AttributeType.ALIAS), ValidationMessages.attributeValueConverted("alias.ripe.net.", "alias.ripe.net"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_inetrtr_no_dot() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inet-rtr:          test.ripe.net\n" +
                "alias:          alias.ripe.net");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.INET_RTR).toString(), is("test.ripe.net"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_key_inetrtr_no_dot() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inet-rtr:          test.ripe.net\n" +
                "alias:          alias.ripe.net");

        final CIString result = attributeSanitizer.sanitizeKey(rpslObject);
        assertThat(result.toString(), is("test.ripe.net"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_inetrtr_with_trailing_dot() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inet-rtr:          test.ripe.net.\n" +
                "alias:          alias.ripe.net");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.INET_RTR).toString(), is("test.ripe.net"));
        verify(objectMessages).addMessage(result.findAttribute(AttributeType.INET_RTR), ValidationMessages.attributeValueConverted("test.ripe.net.", "test.ripe.net"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_key_inetrtr_with_trailing_dot() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inet-rtr:          test.ripe.net.\n" +
                "alias:          alias.ripe.net");

        final CIString result = attributeSanitizer.sanitizeKey(rpslObject);
        assertThat(result.toString(), is("test.ripe.net"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_invalid() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inetnum:          test.ripe.net");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.getValueForAttribute(AttributeType.INETNUM).toString(), is("test.ripe.net"));

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_changed() {
        when(dateTimeProvider.getCurrentDate()).thenReturn(new LocalDate(2013, 02, 25));
        final RpslObject rpslObject = RpslObject.parse("inet6num: 2001::/16\n" +
                "changed: user@host.org 20120601\n" +
                "changed: user@host.org\n" +
                "remarks: changed");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        final List<RpslAttribute> changed = result.findAttributes(AttributeType.CHANGED);
        assertThat(changed.get(0).getCleanValue().toString(), is("user@host.org 20120601"));
        assertThat(changed.get(1).getCleanValue().toString(), is("user@host.org 20130225"));
        verifyZeroInteractions(objectMessages);
    }

    @Test
    public void transform_type_and_key() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "person:      some # with\n" +
                "+            person\n" +
                "+            # remark\n" +
                "nic-hdl:     TEST-PN\n" +
                "+            # another\n" +
                "+            #\n" +
                "+            # remark\n");

        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);
        assertThat(result.findAttribute(AttributeType.PERSON).getValue(), is("some person # with remark"));
        assertThat(result.findAttribute(AttributeType.NIC_HDL).getValue(), is("TEST-PN # another remark"));

        verify(objectMessages).addMessage(result.getTypeAttribute(), ValidationMessages.continuationLinesRemoved());
        verify(objectMessages).addMessage(result.getTypeAttribute(), ValidationMessages.remarksReformatted());
        verify(objectMessages).addMessage(result.findAttribute(AttributeType.NIC_HDL), ValidationMessages.continuationLinesRemoved());
        verify(objectMessages).addMessage(result.findAttribute(AttributeType.NIC_HDL), ValidationMessages.remarksReformatted());

        verifyNoMoreInteractions(objectMessages);
    }

    @Test
    public void transform_source_to_upper() {
        final RpslObject rpslObject = RpslObject.parse("person: Person A\nnic-hdl: tst-test\nsource: Test");
        final RpslObject result = attributeSanitizer.sanitize(rpslObject, objectMessages);

        assertThat(result.findAttribute(AttributeType.SOURCE).getValue(), is("TEST"));
    }
}
