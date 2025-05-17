package me.sailex.secondbrain.llm.player2.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import io.github.sashirestela.openai.SimpleUncheckedException;
import io.github.sashirestela.openai.support.DefaultSchemaConverter;

public class Player2SchemaConverter extends DefaultSchemaConverter {

    private static final String JSON_EMPTY_CLASS = "{\"type\":\"object\",\"properties\":{}," +
            "\"additionalProperties\":false,\"required\":[]}";

    private final SchemaGenerator schemaGenerator;
    private final ObjectMapper objectMapper;

    public Player2SchemaConverter() {
        this(Boolean.FALSE);
    }

    public Player2SchemaConverter(Boolean isStructuredOutput) {
        objectMapper = new ObjectMapper();
        JacksonModule jacksonModule = null;
        if (Boolean.TRUE.equals(isStructuredOutput)) {
            jacksonModule = new JacksonModule(JacksonOption.RESPECT_JSONPROPERTY_ORDER);
        } else {
            jacksonModule = new JacksonModule(JacksonOption.RESPECT_JSONPROPERTY_ORDER,
                    JacksonOption.RESPECT_JSONPROPERTY_REQUIRED);
        }
        var configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
                .with(jacksonModule)
                .with(Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT)
                .without(Option.SCHEMA_VERSION_INDICATOR);
        if (Boolean.TRUE.equals(isStructuredOutput)) {
            configBuilder.forFields().withRequiredCheck(field -> Boolean.TRUE);
        }
        var config = configBuilder.build();
        schemaGenerator = new SchemaGenerator(config);
    }

    @Override
    public JsonNode convert(Class<?> clazz) {
        JsonNode jsonSchema;
        try {
            jsonSchema = schemaGenerator.generateSchema(clazz);
            if (jsonSchema.get("properties") == null) {
                jsonSchema = objectMapper.readTree(JSON_EMPTY_CLASS);
            }

        } catch (Exception e) {
            throw new SimpleUncheckedException("Cannot generate the JsonSchema for the class {0}.", clazz.getName(), e);
        }
        return jsonSchema;
    }

}
