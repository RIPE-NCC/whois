package net.ripe.db.whois.api.generator.gen;

import nl.grol.whois.data.model.Inetnum;
import static nl.grol.whois.data.model.Inetnum.*;


public class Doit {

    public static void main(String[] args) {

        Doit doit = new Doit();

        doit.construct();

        //Entity response doit.sendReceive( Entity entity );
    }

    public void construct() {
        //
        // varags option style
        //
        Inetnum factoryStyle = NewInetnum(
                inetnum("10.20.30.0 - 10.20.30.255"),
                netname("my net"),
                descr("my descr"),
                descr("my descr 2"),
                country("nl"),
                adminCRef("GROL-RIPE"),
                techCRef("GROL-RIPE"),
                status("ASSIGNED"),
                mntByRef("GROL-MNT"),
                changed("27022015"),
                source("RIPE"),
                orgRef("MY-ORG"));
        System.err.println( factoryStyle.toRpsl() );
        System.out.println( factoryStyle.toString() );

        // next line will break: due to missing mandatory fields
        NewInetnum(inetnum("10.20.30.0 - 10.20.30.255"));

        //
        // Builder style
        //
        Inetnum builderStyle = new Inetnum.Builder()
                .mandatorySetInetnum("10.20.30.0 - 10.20.30.255")
                .mandatorySetNetname("my net")
                .mandatoryAddDescr("my descr")
                .mandatoryAddDescr("my descr 2")
                .mandatoryAddCountry("nl")
                .mandatoryAddAdminCRef("GROL-RIPE")
                .mandatoryAddTechCRef("GROL-RIPE")
                .mandatorySetStatus("ASSIGNED")
                .mandatoryAddMntByRef("GROL-MNT")
                .mandatoryAddChanged("27022015")
                .mandatorySetSource("RIPE")
                .optionalSetOrgRef("MY-ORG")
                .build();
        System.err.print( builderStyle.toRpsl() );
        System.out.print( builderStyle.toString() );

        // next line will break: due to missing mandatory fields
        new Inetnum.Builder().mandatorySetInetnum("10.20.30.0 - 10.20.30.255").build();

        }
}
