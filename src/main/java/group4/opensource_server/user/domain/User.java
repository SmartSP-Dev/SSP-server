package group4.opensource_server.user.domain;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Integer id;

    @Column(name = "email", length=100, nullable = false, unique = true)
    private String email;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "university", length = 100)
    private String university;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "profile_image", length = 1000, nullable = true)
    private String profileImage;

    @Column(name = "everytime_url", length = 1000, nullable = true)
    private String everytimeUrl;

    @Column(name = "provider")
    private String provider;

    @Builder
    public User(String email, String profileImage, String provider) {
        this.email = email;
        this.profileImage = profileImage;
        this.provider = provider;
    }
}
