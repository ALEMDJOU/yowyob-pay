package com.yowyob.template.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class R2dbcPoolPropertiesBindingTest {

    @Test
    void environmentOverridesPoolMaxSize() {
        StandardEnvironment environment = new StandardEnvironment();
        Map<String, Object> map = new HashMap<>();
        map.put("spring.r2dbc.url", "r2dbc:postgresql://localhost:5432/payment_db");
        map.put("spring.r2dbc.username", "u");
        map.put("spring.r2dbc.password", "p");
        map.put("spring.r2dbc.pool.max-size", "42");
        map.put("spring.r2dbc.pool.initial-size", "2");
        environment.getPropertySources().addFirst(new MapPropertySource("test", map));

        BindResult<R2dbcProperties> result = Binder.get(environment).bind("spring.r2dbc", R2dbcProperties.class);
        assertTrue(result.isBound());
        R2dbcProperties props = result.get();
        assertEquals(42, props.getPool().getMaxSize());
        assertEquals(2, props.getPool().getInitialSize());
    }
}
