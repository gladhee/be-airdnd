package rice_monkey.image.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rice_monkey.image.entity.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {}

