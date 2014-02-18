package net.ripe.db.whois.api.rest.mapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Attributes;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.ErrorMessages;
import net.ripe.db.whois.api.rest.domain.Flag;
import net.ripe.db.whois.api.rest.domain.Flags;
import net.ripe.db.whois.api.rest.domain.InverseAttribute;
import net.ripe.db.whois.api.rest.domain.InverseAttributes;
import net.ripe.db.whois.api.rest.domain.PrimaryKey;
import net.ripe.db.whois.api.rest.domain.QueryString;
import net.ripe.db.whois.api.rest.domain.QueryStrings;
import net.ripe.db.whois.api.rest.domain.Source;
import net.ripe.db.whois.api.rest.domain.Sources;
import net.ripe.db.whois.api.rest.domain.Template;
import net.ripe.db.whois.api.rest.domain.TemplateAttribute;
import net.ripe.db.whois.api.rest.domain.TemplateAttributes;
import net.ripe.db.whois.api.rest.domain.Templates;
import net.ripe.db.whois.api.rest.domain.TypeFilter;
import net.ripe.db.whois.api.rest.domain.TypeFilters;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisObjects;
import net.ripe.db.whois.api.rest.domain.WhoisTag;
import net.ripe.db.whois.api.rest.domain.WhoisTags;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class Json {

    public static class AttributesDeserializer extends JsonDeserializer<Attributes> {
        @Override
        public Attributes deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            jp.nextToken();
            if (jp.getCurrentToken() == JsonToken.END_ARRAY) {
                return new Attributes();
            }

            final Iterator<Attribute> iterator = jp.readValuesAs(Attribute.class);

            final List<Attribute> attributesList = Lists.newArrayList();
            while (iterator.hasNext()) {
                attributesList.add(iterator.next());
            }

            return new Attributes(attributesList);
        }
    }

    public static class AttributesSerializer extends JsonSerializer<Attributes> {
        @Override
        public void serialize(final Attributes value, final JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartArray();

            for (Attribute attribute : value.getAttributes()) {
                jgen.writeObject(attribute);
            }

            jgen.writeEndArray();
        }
    }

    public static class ErrorMessagesDeserializer extends JsonDeserializer<ErrorMessages> {
        @Override
        public ErrorMessages deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            jp.nextToken();
            if (jp.getCurrentToken() == JsonToken.END_ARRAY) {
                return new ErrorMessages();
            }

            final Iterator<ErrorMessage> iterator = jp.readValuesAs(ErrorMessage.class);

            final List<ErrorMessage> errors = Lists.newArrayList();
            while (iterator.hasNext()) {
                errors.add(iterator.next());
            }

            return new ErrorMessages(errors);
        }
    }

    public static class ErrorMessagesSerializer extends JsonSerializer<ErrorMessages> {
        @Override
        public void serialize(final ErrorMessages value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
            jgen.writeStartArray();

            for (ErrorMessage error : value.getErrorMessages()) {
                jgen.writeObject(error);
            }

            jgen.writeEndArray();
        }
    }

    public static class FlagsDeserializer extends JsonDeserializer<Flags> {
        @Override
        public Flags deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            jp.nextToken();
            if (jp.getCurrentToken() == JsonToken.END_ARRAY) {
                return new Flags();
            }

            final Iterator<Flag> iterator = jp.readValuesAs(Flag.class);

            final List<Flag> flagList = Lists.newArrayList();
            while (iterator.hasNext()) {
                flagList.add(iterator.next());
            }

            return new Flags(flagList);
        }
    }

    public static class FlagsSerializer extends JsonSerializer<Flags> {
        @Override
        public void serialize(final Flags value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
            jgen.writeStartArray();

            for (Flag flag : value.getFlags()) {
                jgen.writeObject(flag);
            }

            jgen.writeEndArray();
        }
    }

    public static class InverseAttributesDeserializer extends JsonDeserializer<InverseAttributes> {
        @Override
        public InverseAttributes deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            jp.nextToken();
            if (jp.getCurrentToken() == JsonToken.END_ARRAY) {
                return new InverseAttributes();
            }

            final Iterator<InverseAttribute> iterator = jp.readValuesAs(InverseAttribute.class);

            final List<InverseAttribute> iaList = Lists.newArrayList();
            while (iterator.hasNext()) {
                iaList.add(iterator.next());
            }

            return new InverseAttributes(iaList);
        }
    }

    public static class InverseAttributesSerializer extends JsonSerializer<InverseAttributes> {
        @Override
        public void serialize(final InverseAttributes value, final JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartArray();

            for (InverseAttribute inverseAttribute : value.getInverseAttributes()) {
                jgen.writeObject(inverseAttribute);
            }

            jgen.writeEndArray();
        }
    }

    public static class WhoisObjectsDeserializer extends JsonDeserializer<WhoisObjects> {
        @Override
        public WhoisObjects deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            jp.nextToken();
            if (jp.getCurrentToken() == JsonToken.END_ARRAY) {
                return new WhoisObjects();
            }

            final Iterator<WhoisObject> whoisObjectIterator = jp.readValuesAs(WhoisObject.class);

            final List<WhoisObject> whoisObjectList = Lists.newArrayList();
            while (whoisObjectIterator.hasNext()) {
                whoisObjectList.add(whoisObjectIterator.next());
            }

            return new WhoisObjects(whoisObjectList);
        }
    }

    public static class WhoisObjectsSerializer extends JsonSerializer<WhoisObjects> {
        @Override
        public void serialize(final WhoisObjects value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
            jgen.writeStartArray();

            for (WhoisObject object : value.getWhoisObjects()) {
                jgen.writeObject(object);
            }

            jgen.writeEndArray();
        }
    }

    public static class QueryStringsDeserializer extends JsonDeserializer<QueryStrings> {
        @Override
        public QueryStrings deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            jp.nextToken();
            if (jp.getCurrentToken() == JsonToken.END_ARRAY) {
                return new QueryStrings();
            }

            final Iterator<QueryString> qsIterator = jp.readValuesAs(QueryString.class);

            final List<QueryString> sourceList = Lists.newArrayList();
            while (qsIterator.hasNext()) {
                sourceList.add(qsIterator.next());
            }

            return new QueryStrings(sourceList);
        }
    }

    public static class QueryStringsSerializer extends JsonSerializer<QueryStrings> {
        @Override
        public void serialize(final QueryStrings value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
            jgen.writeStartArray();

            for (QueryString queryString : value.getQueryStrings()) {
                jgen.writeObject(queryString);
            }

            jgen.writeEndArray();
        }
    }

    public static class PrimaryKeyDeserializer extends JsonDeserializer<PrimaryKey> {
        @Override
        public PrimaryKey deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            jp.nextToken();
            if (jp.getCurrentToken() == JsonToken.END_ARRAY) {
                return new PrimaryKey();
            }

            final Iterator<Attribute> iterator = jp.readValuesAs(Attribute.class);

            final List<Attribute> attributesList = Lists.newArrayList();
            while (iterator.hasNext()) {
                attributesList.add(iterator.next());
            }

            return new PrimaryKey(attributesList);
        }
    }

    public static class PrimaryKeySerializer extends JsonSerializer<PrimaryKey> {
        @Override
        public void serialize(final PrimaryKey value, final JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartArray();

            for (Attribute attribute : value.getAttributes()) {
                jgen.writeObject(attribute);
            }

            jgen.writeEndArray();
        }
    }

    public static class SourcesDeserializer extends JsonDeserializer<Sources> {
        @Override
        public Sources deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            jp.nextToken();
            if (jp.getCurrentToken() == JsonToken.END_ARRAY) {
                return new Sources();
            }

            final Iterator<Source> sourceIterator = jp.readValuesAs(Source.class);

            final List<Source> sourceList = Lists.newArrayList();
            while (sourceIterator.hasNext()) {
                sourceList.add(sourceIterator.next());
            }

            return new Sources(sourceList);
        }
    }

    public static class SourcesSerializer extends JsonSerializer<Sources> {

        @Override
        public void serialize(final Sources value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
            jgen.writeStartArray();

            for (Source source : value.getSources()) {
                jgen.writeObject(source);
            }

            jgen.writeEndArray();
        }
    }

    public static class TagsDeserializer extends JsonDeserializer<WhoisTags> {
        @Override
        public WhoisTags deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            jp.nextToken();
            if (jp.getCurrentToken() == JsonToken.END_ARRAY) {
                return new WhoisTags();
            }

            final Iterator<WhoisTag> iterator = jp.readValuesAs(WhoisTag.class);

            final List<WhoisTag> tagList = Lists.newArrayList();
            while (iterator.hasNext()) {
                tagList.add(iterator.next());
            }

            return new WhoisTags(tagList);
        }
    }

    public static class TagsSerializer extends JsonSerializer<WhoisTags> {
        @Override
        public void serialize(final WhoisTags value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
            jgen.writeStartArray();

            for (WhoisTag tag : value.getTags()) {
                jgen.writeObject(tag);
            }

            jgen.writeEndArray();
        }
    }

    public static class TemplatesDeserializer extends JsonDeserializer<Templates> {
        @Override
        public Templates deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            jp.nextToken();
            if (jp.getCurrentToken() == JsonToken.END_ARRAY) {
                return new Templates();
            }

            final Iterator<Template> iterator = jp.readValuesAs(Template.class);

            final List<Template> templatesList = Lists.newArrayList();
            while (iterator.hasNext()) {
                templatesList.add(iterator.next());
            }

            return new Templates(templatesList);
        }
    }

    public static class TemplatesSerializer extends JsonSerializer<Templates> {
        @Override
        public void serialize(final Templates value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
            jgen.writeStartArray();

            for (Template template : value.getTemplates()) {
                jgen.writeObject(template);
            }

            jgen.writeEndArray();
        }
    }

    public static class TemplateAttributesDeserializer extends JsonDeserializer<TemplateAttributes> {
        @Override
        public TemplateAttributes deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            jp.nextToken();
            if (jp.getCurrentToken() == JsonToken.END_ARRAY) {
                return new TemplateAttributes();
            }

            final Iterator<TemplateAttribute> iterator = jp.readValuesAs(TemplateAttribute.class);

            final List<TemplateAttribute> attributesList = Lists.newArrayList();
            while (iterator.hasNext()) {
                attributesList.add(iterator.next());
            }

            return new TemplateAttributes(attributesList);
        }
    }

    public static class TemplateAttributesSerializer extends JsonSerializer<TemplateAttributes> {
        @Override
        public void serialize(final TemplateAttributes value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
            jgen.writeStartArray();

            for (TemplateAttribute attribute : value.getAttributes()) {
                jgen.writeObject(attribute);
            }

            jgen.writeEndArray();
        }
    }

    public static class TypeFiltersDeserializer extends JsonDeserializer<TypeFilters> {
        @Override
        public TypeFilters deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            jp.nextToken();
            if (jp.getCurrentToken() == JsonToken.END_ARRAY) {
                return new TypeFilters();
            }

            final Iterator<TypeFilter> sourceIterator = jp.readValuesAs(TypeFilter.class);

            final List<TypeFilter> tfList = Lists.newArrayList();
            while (sourceIterator.hasNext()) {
                tfList.add(sourceIterator.next());
            }

            return new TypeFilters(tfList);
        }
    }

    public static class TypeFiltersSerializer extends JsonSerializer<TypeFilters> {
        @Override
        public void serialize(final TypeFilters value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
            jgen.writeStartArray();

            for (TypeFilter typeFilter : value.getTypeFilters()) {
                jgen.writeObject(typeFilter);
            }

            jgen.writeEndArray();
        }
    }
}
