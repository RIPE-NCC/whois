package net.ripe.db.whois.common.generated;

import org.junit.Test;

import java.io.StringReader;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AttributeLexerTest {

    @Test
    public void asnumber() throws Exception {
        FilterLexer subject = new FilterLexer(new StringReader("<[AS1]>"), new FilterParser());
        assertThat(subject.yylex(), is((int)'<'));
        assertThat(subject.yylex(), is((int)'['));
        assertThat(subject.yylex(), is((int)FilterParser.TKN_ASNO));
        assertThat(subject.yylex(), is((int)']'));
        assertThat(subject.yylex(), is((int)'>'));
        assertThat(subject.yylex(), is(0));
    }

    @Test
    public void asrange() throws Exception {
        FilterLexer subject = new FilterLexer(new StringReader("<[AS1-AS2]>"), new FilterParser());
        assertThat(subject.yylex(), is((int)'<'));
        assertThat(subject.yylex(), is((int)'['));
        assertThat(subject.yylex(), is((int)FilterParser.TKN_ASRANGE));
        assertThat(subject.yylex(), is((int)']'));
        assertThat(subject.yylex(), is((int)'>'));
        assertThat(subject.yylex(), is(0));
    }

    @Test
    public void asrange_import() throws Exception {
        ImportLexer subject = new ImportLexer(new StringReader("<[AS1-AS2]>"), new ImportParser());
        assertThat(subject.yylex(), is((int)'<'));
        assertThat(subject.yylex(), is((int)'['));
        assertThat(subject.yylex(), is((int)ImportParser.TKN_ASRANGE));
        assertThat(subject.yylex(), is((int)']'));
        assertThat(subject.yylex(), is((int)'>'));
        assertThat(subject.yylex(), is(0));
    }

    @Test
    public void asrange_one_space() throws Exception {
        FilterLexer subject = new FilterLexer(new StringReader("<[AS1 -AS2]>"), new FilterParser());
        assertThat(subject.yylex(), is((int)'<'));
        assertThat(subject.yylex(), is((int)'['));
        assertThat(subject.yylex(), is((int)FilterParser.TKN_ASRANGE));
        assertThat(subject.yylex(), is((int)']'));
        assertThat(subject.yylex(), is((int)'>'));
        assertThat(subject.yylex(), is(0));
    }

    @Test
    public void asrange_with_spaces() throws Exception {
        FilterLexer subject = new FilterLexer(new StringReader("<[AS1 -   AS2]>"), new FilterParser());
        assertThat(subject.yylex(), is((int)'<'));
        assertThat(subject.yylex(), is((int)'['));
        assertThat(subject.yylex(), is((int)FilterParser.TKN_ASRANGE));
        assertThat(subject.yylex(), is((int)']'));
        assertThat(subject.yylex(), is((int)'>'));
        assertThat(subject.yylex(), is(0));
    }


    @Test
    public void asrange_only_anglebrackets() throws Exception {
        FilterLexer subject = new FilterLexer(new StringReader("<^AS1 AS2 AS3$>"), new FilterParser());
        assertThat(subject.yylex(), is((int)'<'));
        assertThat(subject.yylex(), is((int)'^'));
        assertThat(subject.yylex(), is((int)FilterParser.TKN_ASNO));
        assertThat(subject.yylex(), is((int)FilterParser.TKN_ASNO));
        assertThat(subject.yylex(), is((int)FilterParser.TKN_ASNO));
        assertThat(subject.yylex(), is((int)'$'));
        assertThat(subject.yylex(), is((int)'>'));
        assertThat(subject.yylex(), is(0));
    }

    @Test
    public void ipcheck() throws Exception {
        MpPeeringLexer subject = new MpPeeringLexer(new StringReader("AS2320834 at 193.999"), new MpPeeringParser());
        assertThat(subject.yylex(), is((int)MpPeeringParser.TKN_ASNO));
        assertThat(subject.yylex(), is((int)MpPeeringParser.KEYW_AT));
        assertThat(subject.yylex(), is((int)'1'));
        assertThat(subject.yylex(), is((int)'9'));
        assertThat(subject.yylex(), is((int)'3'));
        assertThat(subject.yylex(), is((int)'.'));
        assertThat(subject.yylex(), is((int)'9'));
        assertThat(subject.yylex(), is((int)'9'));
        assertThat(subject.yylex(), is((int)'9'));
        assertThat(subject.yylex(), is(0));
    }

    @Test
    public void mpimport() throws Exception {
        MpImportLexer subject =new MpImportLexer(new StringReader("afi ipv6.unicastfrom AS39790 action pref=150; accept AS39790"), new MpImportParser());
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_AFI));
        assertThat(subject.yylex(), is((int)MpImportParser.KEYW_AFI_VALUE_V6));     // TODO: [ES] no space between tokens
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

}
