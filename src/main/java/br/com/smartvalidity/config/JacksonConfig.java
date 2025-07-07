package br.com.smartvalidity.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Registrar módulo padrão do Java Time
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // Configurar serializador simples para LocalDateTime
        DateTimeFormatter serializationFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(serializationFormatter));
        
        mapper.registerModule(javaTimeModule);
        
        // Criar módulo customizado para deserialização flexível de LocalDateTime
        SimpleModule customModule = new SimpleModule();
        customModule.addDeserializer(LocalDateTime.class, new FlexibleLocalDateTimeDeserializer());
        mapper.registerModule(customModule);
        
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        return mapper;
    }
    
    /**
     * Deserializador customizado que aceita múltiplos formatos de data e converte para LocalDateTime
     */
    public static class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        
        private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_ZONED_DATE_TIME
        };
        
        @Override
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            String dateString = parser.getValueAsString();
            
            if (dateString == null || dateString.trim().isEmpty()) {
                return null;
            }
            
            // Primeiro, tenta parsear como LocalDateTime diretamente
            for (DateTimeFormatter formatter : FORMATTERS) {
                try {
                    return LocalDateTime.parse(dateString, formatter);
                } catch (DateTimeParseException ignored) {
                    // Continua tentando outros formatters
                }
            }
            
            // Se não conseguir como LocalDateTime, tenta como OffsetDateTime e converte
            try {
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateString);
                return offsetDateTime.toLocalDateTime();
            } catch (DateTimeParseException ignored) {
                // Continua tentando
            }
            
            // Se não conseguir como OffsetDateTime, tenta como ZonedDateTime e converte
            try {
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString);
                return zonedDateTime.toLocalDateTime();
            } catch (DateTimeParseException ignored) {
                // Continua tentando
            }
            
            throw new IOException("Não foi possível parsear a data: " + dateString);
        }
    }
} 