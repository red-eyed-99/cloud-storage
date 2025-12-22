package ru.redeyed.cloudstorage.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.redeyed.cloudstorage.common.validation.annotation.BcryptEncoded;
import ru.redeyed.cloudstorage.user.validation.annotation.ValidUsername;
import java.util.UUID;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ValidUsername
    private String username;

    @BcryptEncoded(parameterName = "password")
    private String password;
}
