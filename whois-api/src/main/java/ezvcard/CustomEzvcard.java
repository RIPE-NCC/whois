/************************************************************************************************
 * Copyright (c) 2013, Asia Pacific Network Information Center (APNIC). All rights reserved.
 *
 * This is unpublished proprietary source code of Asia Pacific Network Information Center (APNIC)
 * The copyright notice above does not evidence any actual or intended
 * publication of such source code.
 ************************************************************************************************/
package ezvcard;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/**
 * <Insert Class Description here>
 * <p/>
 *
 * @author dragan@apnic.net
 *         Date: 21/06/13
 *         Time: 2:48 PM
 */
public class CustomEzvcard {

    public static WriterChainJsonSingle writeJson(VCard vcard) {
        return new WriterChainJsonSingle(vcard);
    }

    /**
     * Convenience chainer class for writing JSON-encoded vCards (jCard).
     */
    public static class WriterChainJsonSingle extends Ezvcard.WriterChainJson<WriterChainJsonSingle> {
        private List<String> warnings;

        private WriterChainJsonSingle(VCard vcard) {
            super(Arrays.asList(vcard));
        }

        @Override
        public WriterChainJsonSingle prodId(boolean include) {
            return super.prodId(include);
        }

        @Override
        public WriterChainJsonSingle indent(boolean indent) {
            return super.indent(indent);
        }

        /**
         * Provides a list object that any marshal warnings will be put into.
         * Warnings usually occur when there is a property in the VCard that is
         * not supported by the version to which the vCard is being marshalled.
         * @param warnings the list object that will be populated with the
         * warnings of the marshalled vCard.
         * @return this
         */
        public WriterChainJsonSingle warnings(List<String> warnings) {
            this.warnings = warnings;
            return this;
        }

        @Override
        void addWarnings(List<String> warnings) {
            if (this.warnings != null) {
                this.warnings.addAll(warnings);
            }
        }

        /**
         * Writes the jCards to a writer.
         * @param writer the writer to write to
         * @throws java.io.IOException if there's a problem writing to the writer
         */
        public void go(Writer writer) throws IOException {
            CustomJCardWriter jcardWriter = new CustomJCardWriter(writer);
            jcardWriter.setAddProdId(prodId);
            jcardWriter.setIndent(indent);
            try {
                for (VCard vcard : vcards) {
                    jcardWriter.write(vcard);
                    addWarnings(jcardWriter.getWarnings());
                }
            } finally {
                jcardWriter.endJsonStream();
            }
        }
    }
}
