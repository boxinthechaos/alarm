package org.example.qweralarm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Avatar {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String currentHatPath;
    private String currentClothesPath;
    private String currentBackground;

    @ManyToOne(fetch = FetchType.LAZY)
    private Item equippedHat;
}
