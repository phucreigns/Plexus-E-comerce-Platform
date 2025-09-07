package com.phuc.auth.dto.auth0;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Auth0UserInfo {
    @JsonProperty("sub")
    String sub;

    @JsonProperty("email")
    String email;

    @JsonProperty("email_verified")
    Boolean emailVerified;

    @JsonProperty("name")
    String name;

    @JsonProperty("given_name")
    String givenName;

    @JsonProperty("family_name")
    String familyName;

    @JsonProperty("nickname")
    String nickname;

    @JsonProperty("picture")
    String picture;

    @JsonProperty("phone_number")
    String phoneNumber;

    @JsonProperty("phone_number_verified")
    Boolean phoneNumberVerified;

    @JsonProperty("birthdate")
    String birthdate;

    @JsonProperty("preferred_username")
    String preferredUsername;

    @JsonProperty("updated_at")
    String updatedAt;

    @JsonProperty("https://xuanphuc.com/roles")
    List<String> roles;

    @JsonProperty("https://xuanphuc.com/permissions")
    List<String> permissions;
}
