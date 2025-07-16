package rice_monkey.listing.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer price;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Long imgId;
    private Long hostId;

    private String imgUrl;

    private StayType type;
    private Integer maxGuests;

    private Address address;
    private ListingStatus status;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClosedStayDate> closedStayDates = new ArrayList<>();

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListingComment> comments = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "stay_tag",
            joinColumns = @JoinColumn(name = "stay_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();

}
