package ru.redeyed.cloudstorage.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.redeyed.cloudstorage.common.validation.annotation.BcryptEncoded;
import ru.redeyed.cloudstorage.common.validation.annotation.ValidUsername;
import ru.redeyed.cloudstorage.resource.Resource;
import java.util.ArrayList;
import java.util.List;
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

    @OneToMany
    @JoinColumn(name = "user_id")
    @Builder.Default
    private List<Resource> resources = new ArrayList<>();
}
