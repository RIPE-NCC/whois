package net.ripe.db.whois.common.generated;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MpImportlexerTest {

    @Test
    public void mpimport() throws Exception {
        MpImportLexer subject =new MpImportLexer(new StringReader("afi ipv6.unicast from AS39790 action pref=150; accept AS39790"), new MpImportParser());
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_AFI));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_IPV6_TXT));
        assertThat(subject.yylex(), is((int)'.'));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_UNICAST));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_FROM));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_ASNO));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_ACTION));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_PREF));
        assertThat(subject.yylex(), is((int)MpImportParser.OP_EQUAL));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_INT));
        assertThat(subject.yylex(), is((int)';'));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_ACCEPT));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_ASNO));
        assertThat(subject.yylex(), is(0));
    }

    @Test
    public void mpimport_with_error_1() throws Exception {
        MpImportLexer subject =new MpImportLexer(new StringReader("afiipv6.unicast from AS39790 action pref=150; accept AS39790"), new MpImportParser());
        // One would have hoped for
        // assertThat(subject.yylex(), is((int)MpImportParser.KEYW_AFI));
        // assertThat(subject.yylex(), is((int)MpImportParser.KEYW_IPV6_TXT));
        // but got 'the error
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_DNAME));
        assertThat(subject.yylex(), is((int)'.'));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_UNICAST));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_FROM));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_ASNO));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_ACTION));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_PREF));
        assertThat(subject.yylex(), is((int)MpImportParser.OP_EQUAL));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_INT));
        assertThat(subject.yylex(), is((int)';'));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_ACCEPT));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_ASNO));
        assertThat(subject.yylex(), is(0));
    }


    @Test
    public void mpimport_with_error_2() throws Exception {
        MpImportLexer subject =new MpImportLexer(new StringReader("afi ipv6.unicastfrom AS39790 action pref=150; accept AS39790"), new MpImportParser());
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_AFI));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_IPV6_TXT));
        assertThat(subject.yylex(), is((int)'.'));
        // One would have hoped for
        // assertThat(subject.yylex(), is((int)MpImportParser.KEYW_UNICAST));
        // assertThat(subject.yylex(), is((int)MpImportParser.KEYW_FROM));
        // but got 'the error
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_DNAME));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_ASNO));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_ACTION));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_PREF));
        assertThat(subject.yylex(), is((int)MpImportParser.OP_EQUAL));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_INT));
        assertThat(subject.yylex(), is((int)';'));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_ACCEPT));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_ASNO));
        assertThat(subject.yylex(), is(0));
    }

    @Test
    public void mpimport_with_error_3() throws Exception {
        MpImportLexer subject =new MpImportLexer(new StringReader("afi ipv6.unicast fromAS39790 action pref=150; accept AS39790"), new MpImportParser());
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_AFI));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_IPV6_TXT));
        assertThat(subject.yylex(), is((int)'.'));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_UNICAST));
        // One would have hoped for
        // assertThat(subject.yylex(), is((int)MpImportParser.KEYW_FROM));
        // assertThat(subject.yylex(), is((int)MpImportParser.TKN_ASNO));
        // but got 'the error
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_DNAME));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_ACTION));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_PREF));
        assertThat(subject.yylex(), is((int)MpImportParser.OP_EQUAL));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_INT));
        assertThat(subject.yylex(), is((int)';'));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_ACCEPT));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_ASNO));
        assertThat(subject.yylex(), is(0));
    }

    @Test
    public void mpimport_with_error_4() throws Exception {
        MpImportLexer subject =new MpImportLexer(new StringReader("afi ipv6.unicast from AS39790action pref=150; accept AS39790"), new MpImportParser());
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_AFI));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_IPV6_TXT));
        assertThat(subject.yylex(), is((int)'.'));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_UNICAST));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_FROM));
        // One would have hoped for
        // assertThat(subject.yylex(), is((int)MpImportParser.TKN_ASNO));
        // assertThat(subject.yylex(), is((int)MpImportParser.KEYW_ACTION));
        // but got 'the error
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_DNAME));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_PREF));
        assertThat(subject.yylex(), is((int)MpImportParser.OP_EQUAL));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_INT));
        assertThat(subject.yylex(), is((int)';'));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_ACCEPT));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_ASNO));
        assertThat(subject.yylex(), is(0));
    }

    @Test
    public void mpimport_with_error_5() throws Exception {
        MpImportLexer subject =new MpImportLexer(new StringReader("afi ipv6.unicast from AS39790 actionpref=150; accept AS39790"), new MpImportParser());
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_AFI));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_IPV6_TXT));
        assertThat(subject.yylex(), is((int)'.'));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_UNICAST));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_FROM));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_ASNO));
        // One would have hoped for
        // assertThat(subject.yylex(), is((int)MpImportParser.KEYW_ACTION));
        // assertThat(subject.yylex(), is((int)MpImportParser.TKN_PREF));
        // but got 'the error
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_DNAME));
        assertThat(subject.yylex(), is((int)MpImportParser.OP_EQUAL));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_INT));
        assertThat(subject.yylex(), is((int)';'));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_ACCEPT));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_ASNO));
        assertThat(subject.yylex(), is(0));
    }

    @Test
    public void mpimport_with_error_6() throws Exception {
        MpImportLexer subject =new MpImportLexer(new StringReader("afi ipv6.unicast from AS39790 action pref=150;accept AS39790"), new MpImportParser());
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_AFI));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_IPV6_TXT));
        assertThat(subject.yylex(), is((int)'.'));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_UNICAST));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_FROM));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_ASNO));
        // One would have hoped for
        // assertThat(subject.yylex(), is((int)MpImportParser.KEYW_UNICAST));
        // assertThat(subject.yylex(), is((int)MpImportParser.KEYW_FROM));
        // but got 'the error
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_ACTION));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_PREF));
        assertThat(subject.yylex(), is((int)MpImportParser.OP_EQUAL));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_INT));
        assertThat(subject.yylex(), is((int)';'));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_ACCEPT));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_ASNO));
        assertThat(subject.yylex(), is(0));
    }

    @Test
    public void mpimport_with_error_7() throws Exception {
        MpImportLexer subject =new MpImportLexer(new StringReader("afi ipv6.unicast from AS39790 action pref=150; acceptAS39790"), new MpImportParser());
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_AFI));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_IPV6_TXT));
        assertThat(subject.yylex(), is((int)'.'));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_UNICAST));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_FROM));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_ASNO));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_ACTION));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_PREF));
        assertThat(subject.yylex(), is((int)MpImportParser.OP_EQUAL));
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_INT));
        assertThat(subject.yylex(), is((int)';'));
        // One would have hoped for
        // assertThat(subject.yylex(), is((int)MpImportParser.KEYW_ACCEPT));
        // assertThat(subject.yylex(), is((int)MpImportParser.TKN_ASNO));
        // but got 'the error
        assertThat(subject.yylex(), is((int)MpImportParser.TKN_DNAME));
        assertThat(subject.yylex(), is(0));
    }
}
