package rice_monkey.common.testcontainer.annotation;

import org.springframework.context.annotation.Import;
import rice_monkey.common.testcontainer.config.TestcontainersMySQLConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(TestcontainersMySQLConfig.class)
public @interface EnableTestContainerMySQL {
}
