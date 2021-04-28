package Models;

import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.VertexEntity;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.annotations.GenericGenerator;
import com.arangodb.entity.DocumentField;
import com.arangodb.entity.DocumentField.Type;

import java.time.LocalDateTime;
import java.util.*;
import javax.persistence.*;

@Entity
@Table(name = "users")
public class UserData {
    @Id
    private String id;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String _key ;

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    private String bio;
    private String birthDate;
    private UserLocation location;
    private UserLinks links;
    private List <UserPicture> profilePictures;
    private List<String> videos;
    private List <UserInterest> interests;
    private UserPreference preferences;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String get_key() {
        return _key;
    }

    public void set_key(String _key) {
        this._key = _key;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }





    public UserLocation getLocation() {
        return location;
    }

    public void setLocation(UserLocation location) {
        this.location = location;
    }

    public UserLinks getLinks() {
        return links;
    }

    public void setLinks(UserLinks links) {
        this.links = links;
    }

    public List<UserPicture> getProfilePictures() {
        return profilePictures;
    }

    public void setProfilePictures(List<UserPicture> profilePictures) {
        this.profilePictures = profilePictures;
    }

    public List<String> getVideos() {
        return videos;
    }

    public void setVideos(List<String> videos) {
        this.videos = videos;
    }

    public List<UserInterest> getInterests() {
        return interests;
    }

    public void setInterests(List<UserInterest> interests) {
        this.interests = interests;
    }

    public UserPreference getPreferences() {
        return preferences;
    }

    public void setPreferences(UserPreference preferences) {
        this.preferences = preferences;
    }
}