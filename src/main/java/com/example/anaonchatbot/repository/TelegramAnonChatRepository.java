package com.example.anaonchatbot.repository;

import com.example.anaonchatbot.domains.TelegramAnonChat;
import com.example.anaonchatbot.domains.TelegramProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelegramAnonChatRepository extends JpaRepository<TelegramAnonChat, String> {
    Optional<TelegramAnonChat> getTelegramAnonChatById(String id);

    Boolean existsByOwnerAndRecipient(TelegramProfile owner, TelegramProfile recipient);
}
