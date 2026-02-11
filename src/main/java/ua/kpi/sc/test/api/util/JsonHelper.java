package ua.kpi.sc.test.api.util;

import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import ua.kpi.sc.test.api.exception.SerializationException;

public final class JsonHelper {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private JsonHelper() {}

    public static ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }

    public static RestAssuredConfig configWithJackson3() {
        return RestAssuredConfig.config()
                .objectMapperConfig(new ObjectMapperConfig()
                        .jackson3ObjectMapperFactory((type, s) -> OBJECT_MAPPER));
    }

    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new SerializationException(SerializationException.Direction.SERIALIZE,
                    obj.getClass().getName(), e);
        }
    }

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (Exception e) {
            throw new SerializationException(SerializationException.Direction.DESERIALIZE,
                    type.getSimpleName(), json, e);
        }
    }
}
