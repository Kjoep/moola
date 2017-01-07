package be.echostyle.moola.rest.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.FactoryBean;

public class ObjectMapperFactory implements FactoryBean<ObjectMapper> {
    @Override
    public ObjectMapper getObject() throws Exception {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new LocalDateTimeSerialization());
        return om;
    }

    @Override
    public Class<?> getObjectType() {
        return ObjectMapper.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
