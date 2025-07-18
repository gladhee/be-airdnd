package rice_monkey.image.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cloud.aws")
public class S3Properties {
    private String region;
    private S3Info s3Info;

    @Getter
    @Setter
    public static class S3Info {
        private String bucket;
    }
}

