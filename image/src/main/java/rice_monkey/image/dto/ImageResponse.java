package rice_monkey.image.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageResponse {

    private Long id;

    private String url;

    private String fileName;
}
