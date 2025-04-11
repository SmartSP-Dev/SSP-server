package group4.opensource_server.user.domain;


import group4.opensource_server.user.dto.UserUpdateRequestDto;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
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

    public User update(UserUpdateRequestDto userUpdateRequestDto){
        if(userUpdateRequestDto.getName() != null) this.name = userUpdateRequestDto.getName();
        if(userUpdateRequestDto.getUniversity() != null) this.university = userUpdateRequestDto.getUniversity();
        if(userUpdateRequestDto.getDepartment() != null) this.department = userUpdateRequestDto.getDepartment();
        if(userUpdateRequestDto.getProfileImage() != null) this.profileImage = userUpdateRequestDto.getProfileImage();
        if(userUpdateRequestDto.getEverytimeUrl() != null) this.everytimeUrl = userUpdateRequestDto.getEverytimeUrl();
        return this;
    }

    @Builder
    public User(String email, String profileImage, String provider) {
        this.email = email;
        this.profileImage = profileImage;
        this.provider = provider;
    }
}
