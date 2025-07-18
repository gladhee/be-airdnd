package rice_monkey.image.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rice_monkey.image.dto.ImageResponse;
import rice_monkey.image.dto.ImageUploadResponse;
import rice_monkey.image.entity.Image;
import rice_monkey.image.repository.ImageRepository;
import rice_monkey.image.util.S3Uploader;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3Uploader s3Uploader;
    private final ImageRepository imageRepository;

    @Transactional
    public ImageUploadResponse uploadImage(MultipartFile file) throws IOException {
        String fileUrl = s3Uploader.upload(file);

        Image image = Image.builder()
                .url(fileUrl)
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .createdAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        imageRepository.save(image);

        return new ImageUploadResponse(image.getId(), image.getUrl());
    }

    @Transactional(readOnly = true)
    public ImageResponse getImage(Long id) {
        Image findImage = imageRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Image not found: " + id));
        return new ImageResponse(findImage.getId(),findImage.getUrl(),findImage.getFileName());
    }

    @Transactional
    public void deleteImage(Long id) {
        imageRepository.deleteById(id);
    }
}

