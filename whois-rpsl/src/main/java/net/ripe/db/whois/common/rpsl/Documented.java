package net.ripe.db.whois.common.rpsl;

import com.google.common.collect.Maps;

import java.util.Map;

interface Documented {
    String getDescription(ObjectType objectType);

    class Single implements Documented {
        private final String description;

        public Single(final String description) {
            this.description = description;
        }

        @Override
        public String getDescription(final ObjectType objectType) {
            return description;
        }
    }

    class Multiple implements Documented {
        private final Map<ObjectType, String> descriptionMap = Maps.newEnumMap(ObjectType.class);

        public Multiple(final Map<ObjectType, String> descriptionMap) {
            this.descriptionMap.putAll(descriptionMap);
        }

        @Override
        public String getDescription(final ObjectType objectType) {
            final String description = descriptionMap.get(objectType);
            return description == null ? "" : description;
        }
    }
}
