package net.ripe.db.whois.update.domain;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.Latin1Conversion;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UpdateFactory {

    private final List<RpslTransformer> transformers = Lists.newArrayList();

    public UpdateFactory() {
        this.transformers.add(new Latin1RpslTransformer());
        this.transformers.add(new UnsupportedCharacterTransformer());
    }

    public Update createUpdate(final Paragraph paragraph,
                               final Operation operation,
                               final List<String> deleteReasons,
                               final String rpslObject,
                               final UpdateContext updateContext) {

        Map<Optional<RpslAttribute>, Message> messages = Maps.newHashMap();
        String rpsl = rpslObject;
        for (RpslTransformer transformer : transformers) {
            RpslTransformerResult result = transformer.transform(rpsl);
            rpsl = result.getRpsl();
            messages.putAll(result.getMessages());
        }

        final Update update = new Update(paragraph, operation, deleteReasons, RpslObject.parse(rpsl));

        messages.forEach((key, value) -> {
            if (key.isPresent()) {
                updateContext.addMessage(update, key.get(), value);
            } else {
                updateContext.addMessage(update, value);
            }
        });

        return update;
    }

    interface RpslTransformer {

        RpslTransformerResult transform(final String rpsl);
    }

    class Latin1RpslTransformer implements RpslTransformer {

        @Override
        public RpslTransformerResult transform(String rpsl) {
            final String convertedRpslObject = Latin1Conversion.convert(rpsl);

            final Map<Optional<RpslAttribute>, Message> messages = Maps.newHashMap();

            if (!convertedRpslObject.equals(rpsl)) {
                messages.put(Optional.empty(), UpdateMessages.valueChangedDueToLatin1Conversion());
            }

            return new RpslTransformerResult(convertedRpslObject, messages);
        }
    }

    class UnsupportedCharacterTransformer implements RpslTransformer {

        @Override
        public RpslTransformerResult transform(String rpsl) {
            final RpslObject original = RpslObject.parse(rpsl);
            final String substitutedString = Latin1Conversion.substitute(rpsl);
            final RpslObject substituted = RpslObject.parse(substitutedString);

            final Map<Optional<RpslAttribute>, Message> messages = Maps.newHashMap();

            for (int offset = 0; offset < original.getAttributes().size(); offset++) {
                final RpslAttribute attribute = original.getAttributes().get(offset);
                final RpslAttribute updatedAttribute = substituted.getAttributes().get(offset);

                if (!attribute.equals(updatedAttribute)) {
                    messages.put(Optional.of(updatedAttribute), UpdateMessages.valueChangedDueToLatin1Conversion(updatedAttribute.getKey()));
                }
            }

            return new RpslTransformerResult(substitutedString, messages);
        }
    }

    public static class RpslTransformerResult {

        private final String rpsl;
        private final Map<Optional<RpslAttribute>, Message> messages;

        RpslTransformerResult(final String rpsl, final Map<Optional<RpslAttribute>, Message> messages) {
            this.rpsl = rpsl;
            this.messages = messages;
        }

        public String getRpsl() {
            return rpsl;
        }

        public Map<Optional<RpslAttribute>, Message> getMessages() {
            return messages;
        }
    }
}
