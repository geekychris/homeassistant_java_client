package io.github.homeassistant.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a Home Assistant service.
 * <p>
 * This class models a service available in Home Assistant, which can be called to perform
 * various actions within the system. Each service belongs to a domain (like 'light', 'switch',
 * 'climate') and has a specific name (like 'turn_on', 'turn_off', 'toggle').
 * </p>
 * <p>
 * Services can accept various parameters defined in the fields map, and may have specific
 * targeting capabilities described in the target field.
 * </p>
 *
 * <h3>Example:</h3>
 * <pre>
 * // Light turn_on service
 * ServiceField brightnessField = new ServiceField(
 *     "brightness", 
 *     "Brightness of the light (0..255)", 
 *     "integer", 
 *     false, 
 *     null, 
 *     1, 
 *     255
 * );
 *
 * Map<String, ServiceField> fields = new HashMap<>();
 * fields.put("brightness", brightnessField);
 *
 * ServiceTarget target = new ServiceTarget(
 *     true, true, true, true, true, true
 * );
 *
 * Service lightTurnOn = new Service(
 *     "light",
 *     "turn_on",
 *     "Turn on a light",
 *     fields,
 *     target
 * );
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Service {

    private final String domain;
    private final String service;
    private final String description;
    private final Map<String, ServiceField> fields;
    private final ServiceTarget target;

    /**
     * Creates a new Service instance.
     *
     * @param domain      The domain the service belongs to (e.g., "light", "switch")
     * @param service     The service name (e.g., "turn_on", "turn_off")
     * @param description Description of what the service does
     * @param fields      Map of field names to ServiceField objects defining parameters
     * @param target      The target capabilities of this service
     */
    @JsonCreator
    public Service(
            @JsonProperty("domain") String domain,
            @JsonProperty("service") String service,
            @JsonProperty("description") String description,
            @JsonProperty("fields") Map<String, ServiceField> fields,
            @JsonProperty("target") ServiceTarget target) {
        this.domain = domain;
        this.service = service;
        this.description = description;
        this.fields = fields != null ? fields : new HashMap<>();
        this.target = target;
    }

    /**
     * Gets the domain this service belongs to.
     *
     * @return The service domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Gets the service name.
     *
     * @return The service name
     */
    public String getService() {
        return service;
    }

    /**
     * Gets the description of what the service does.
     *
     * @return The service description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the map of fields (parameters) this service accepts.
     *
     * @return Map of field names to ServiceField objects
     */
    public Map<String, ServiceField> getFields() {
        return fields;
    }

    /**
     * Gets the target capabilities of this service.
     *
     * @return The ServiceTarget describing targeting capabilities
     */
    public ServiceTarget getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service1 = (Service) o;
        return Objects.equals(domain, service1.domain) &&
                Objects.equals(service, service1.service) &&
                Objects.equals(description, service1.description) &&
                Objects.equals(fields, service1.fields) &&
                Objects.equals(target, service1.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, service, description, fields, target);
    }

    @Override
    public String toString() {
        return "Service{" +
                "domain='" + domain + '\'' +
                ", service='" + service + '\'' +
                ", description='" + description + '\'' +
                ", fields=" + fields +
                ", target=" + target +
                '}';
    }

    /**
     * Represents a field (parameter) that can be passed to a service call.
     * <p>
     * Each field has a name, description, data type, and optional constraints like
     * minimum/maximum values or example values.
     * </p>
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ServiceField {
        private final String name;
        private final String description;
        private final String type;
        private final Boolean required;
        private final Object example;
        private final Object min;
        private final Object max;

        /**
         * Creates a new ServiceField instance.
         *
         * @param name        The field name
         * @param description Description of the field's purpose
         * @param type        Data type (e.g., "string", "integer", "float", "boolean")
         * @param required    Whether the field is required for service calls
         * @param example     Example value for the field
         * @param min         Minimum value (for numeric fields)
         * @param max         Maximum value (for numeric fields)
         */
        @JsonCreator
        public ServiceField(
                @JsonProperty("name") String name,
                @JsonProperty("description") String description,
                @JsonProperty("type") String type,
                @JsonProperty("required") Boolean required,
                @JsonProperty("example") Object example,
                @JsonProperty("min") Object min,
                @JsonProperty("max") Object max) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.required = required;
            this.example = example;
            this.min = min;
            this.max = max;
        }

        /**
         * Gets the field name.
         *
         * @return The field name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the field description.
         *
         * @return The field description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Gets the field data type.
         *
         * @return The data type (e.g., "string", "integer", "float", "boolean")
         */
        public String getType() {
            return type;
        }

        /**
         * Checks if the field is required for service calls.
         *
         * @return true if the field is required, false otherwise
         */
        public Boolean getRequired() {
            return required;
        }

        /**
         * Gets an example value for the field.
         *
         * @return Example value
         */
        public Object getExample() {
            return example;
        }

        /**
         * Gets the minimum allowed value for numeric fields.
         *
         * @return Minimum value
         */
        public Object getMin() {
            return min;
        }

        /**
         * Gets the maximum allowed value for numeric fields.
         *
         * @return Maximum value
         */
        public Object getMax() {
            return max;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ServiceField that = (ServiceField) o;
            return Objects.equals(name, that.name) &&
                    Objects.equals(description, that.description) &&
                    Objects.equals(type, that.type) &&
                    Objects.equals(required, that.required) &&
                    Objects.equals(example, that.example) &&
                    Objects.equals(min, that.min) &&
                    Objects.equals(max, that.max);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, description, type, required, example, min, max);
        }

        @Override
        public String toString() {
            return "ServiceField{" +
                    "name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", type='" + type + '\'' +
                    ", required=" + required +
                    ", example=" + example +
                    ", min=" + min +
                    ", max=" + max +
                    '}';
        }
    }

    /**
     * Describes the targeting capabilities of a service.
     * <p>
     * This class indicates whether a service can target entities by various methods
     * such as entity ID, device ID, area ID, etc.
     * </p>
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ServiceTarget {
        private final Boolean entity;
        private final Boolean device;
        private final Boolean area;
        private final Boolean entityRegistryEntryId;
        private final Boolean deviceRegistryEntryId;
        private final Boolean areaRegistryEntryId;

        /**
         * Creates a new ServiceTarget instance.
         *
         * @param entity                Whether the service can target by entity ID
         * @param device                Whether the service can target by device ID
         * @param area                  Whether the service can target by area ID
         * @param entityRegistryEntryId Whether the service can target by entity registry entry ID
         * @param deviceRegistryEntryId Whether the service can target by device registry entry ID
         * @param areaRegistryEntryId   Whether the service can target by area registry entry ID
         */
        @JsonCreator
        public ServiceTarget(
                @JsonProperty("entity") Boolean entity,
                @JsonProperty("device") Boolean device,
                @JsonProperty("area") Boolean area,
                @JsonProperty("entity_registry_entry_id") Boolean entityRegistryEntryId,
                @JsonProperty("device_registry_entry_id") Boolean deviceRegistryEntryId,
                @JsonProperty("area_registry_entry_id") Boolean areaRegistryEntryId) {
            this.entity = entity;
            this.device = device;
            this.area = area;
            this.entityRegistryEntryId = entityRegistryEntryId;
            this.deviceRegistryEntryId = deviceRegistryEntryId;
            this.areaRegistryEntryId = areaRegistryEntryId;
        }

        /**
         * Checks if the service can target by entity ID.
         *
         * @return true if targeting by entity ID is supported, false otherwise
         */
        public Boolean getEntity() {
            return entity;
        }

        /**
         * Checks if the service can target by device ID.
         *
         * @return true if targeting by device ID is supported, false otherwise
         */
        public Boolean getDevice() {
            return device;
        }

        /**
         * Checks if the service can target by area ID.
         *
         * @return true if targeting by area ID is supported, false otherwise
         */
        public Boolean getArea() {
            return area;
        }

        /**
         * Checks if the service can target by entity registry entry ID.
         *
         * @return true if targeting by entity registry entry ID is supported, false otherwise
         */
        @JsonProperty("entity_registry_entry_id")
        public Boolean getEntityRegistryEntryId() {
            return entityRegistryEntryId;
        }

        /**
         * Checks if the service can target by device registry entry ID.
         *
         * @return true if targeting by device registry entry ID is supported, false otherwise
         */
        @JsonProperty("device_registry_entry_id")
        public Boolean getDeviceRegistryEntryId() {
            return deviceRegistryEntryId;
        }

        /**
         * Checks if the service can target by area registry entry ID.
         *
         * @return true if targeting by area registry entry ID is supported, false otherwise
         */
        @JsonProperty("area_registry_entry_id")
        public Boolean getAreaRegistryEntryId() {
            return areaRegistryEntryId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ServiceTarget that = (ServiceTarget) o;
            return Objects.equals(entity, that.entity) &&
                    Objects.equals(device, that.device) &&
                    Objects.equals(area, that.area) &&
                    Objects.equals(entityRegistryEntryId, that.entityRegistryEntryId) &&
                    Objects.equals(deviceRegistryEntryId, that.deviceRegistryEntryId) &&
                    Objects.equals(areaRegistryEntryId, that.areaRegistryEntryId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entity, device, area, entityRegistryEntryId, deviceRegistryEntryId, areaRegistryEntryId);
        }

        @Override
        public String toString() {
            return "ServiceTarget{" +
                    "entity=" + entity +
                    ", device=" + device +
                    ", area=" + area +
                    ", entityRegistryEntryId=" + entityRegistryEntryId +
                    ", deviceRegistryEntryId=" + deviceRegistryEntryId +
                    ", areaRegistryEntryId=" + areaRegistryEntryId +
                    '}';
        }
    }
}

