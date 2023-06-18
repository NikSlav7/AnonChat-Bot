package com.example.anaonchatbot.repository;

import com.example.anaonchatbot.domains.TelegramProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface TelegramProfileRepository extends JpaRepository<TelegramProfile, Long> {
    Optional<TelegramProfile> getTelegramProfileByProfileId(Long profileId);
    Boolean existsByProfileId(Long profileId);

    Boolean existsByPhoneNumber(String phoneNumber);
}
