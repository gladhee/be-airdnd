package testcontainer.annotation;

import org.springframework.context.annotation.Import;
import testcontainer.config.TestcontainersRedisConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(TestcontainersRedisConfig.class)
public @interface EnableTestContainerRedis {
}
