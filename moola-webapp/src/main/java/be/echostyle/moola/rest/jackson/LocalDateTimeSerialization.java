package be.echostyle.moola.rest.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeSerialization extends SimpleModule {

    public LocalDateTimeSerialization() {
        addSerializer(LocalDateTime.class, new Serializer());
        addDeserializer(LocalDateTime.class, new Deserializer());
    }

    public static class Serializer extends com.fasterxml.jackson.databind.JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            jsonGenerator.writeString(localDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
        }
    }

    public static class Deserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(jsonParser.getValueAsString()));
        }
    }
}
