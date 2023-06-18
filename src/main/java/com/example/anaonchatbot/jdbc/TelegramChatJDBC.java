package com.example.anaonchatbot.jdbc;


import com.example.anaonchatbot.domains.TelegramAnonChat;
import com.example.anaonchatbot.domains.TelegramProfile;
import com.example.anaonchatbot.exceptions.ChatAlreadyExistsException;
import com.example.anaonchatbot.repository.TelegramAnonChatRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TelegramChatJDBC {

    private final TelegramProfileJDBC telegramProfileJDBC;

    private final TelegramAnonChatRepository telegramAnonChatRepository;

    @Autowired
    public TelegramChatJDBC(TelegramProfileJDBC telegramProfileJDBC, TelegramAnonChatRepository telegramAnonChatRepository) {
        this.telegramProfileJDBC = telegramProfileJDBC;
        this.telegramAnonChatRepository = telegramAnonChatRepository;
    }



    public TelegramAnonChat createTelegramAnonChatIfNotExists(TelegramProfile owner, TelegramProfile recipient, String ownerName) throws ChatAlreadyExistsException {
        if (existsByOwnerAndRecipient(owner, recipient)) throw new ChatAlreadyExistsException("Chat with such user already exists");
        TelegramAnonChat anonChat = new TelegramAnonChat(UUID.randomUUID().toString(), owner, recipient, ownerName);
        telegramAnonChatRepository.save(anonChat);
        return anonChat;
    }

    public boolean existsByOwnerAndRecipient(TelegramProfile owner, TelegramProfile recipient){
        return telegramAnonChatRepository.existsByOwnerAndRecipient(owner, recipient);
    }

    public boolean isCurrentlyInChat(TelegramProfile telegramProfile, Long chatId){
        if (telegramProfile.getCurrentChat() == null) return false;
        TelegramProfile other = telegramProfile.getCurrentChat().getOwner().getProfileId().equals(telegramProfile.getProfileId()) ?
                telegramProfile.getCurrentChat().getRecipient() :
                telegramProfile.getCurrentChat().getOwner();
        return Objects.equals(other.getProfileId(), chatId);
    }

    public boolean existsByOwnerAndRecipient(Long ownerId, Long recipientId){
        TelegramProfile owner = new TelegramProfile();
        owner.setProfileId(ownerId);

        TelegramProfile recipient = new TelegramProfile();
        recipient.setProfileId(recipientId);
        return telegramAnonChatRepository.existsByOwnerAndRecipient(owner, recipient);
    }
    public void activateChat(String chatId){
        TelegramAnonChat anonChat = telegramAnonChatRepository.getTelegramAnonChatById(chatId).orElseThrow();
        anonChat.setActive(true);
        telegramAnonChatRepository.save(anonChat);
    }

    public void setTelegramChatOwnerName(String chatId, String owner){
        TelegramAnonChat anonChat = telegramAnonChatRepository.getTelegramAnonChatById(chatId).orElseThrow();
        anonChat.setOwnerName(owner);
        telegramAnonChatRepository.save(anonChat);
    }

    public void deleteChat(TelegramAnonChat telegramAnonChat){
        telegramAnonChatRepository.delete(telegramAnonChat);
    }
    public void deleteChat(String id){
        telegramAnonChatRepository.deleteById(id);
    }


    public void deleteAndTurnOffChat(TelegramAnonChat telegramAnonChat){
        TelegramProfile owner = telegramAnonChat.getOwner(), recipient = telegramAnonChat.getRecipient();
        if (owner.getCurrentChat() != null && owner.getCurrentChat().getId().equals(telegramAnonChat.getId())) owner.setCurrentChat(null);
        if (recipient.getCurrentChat() != null && recipient.getCurrentChat().getId().equals(telegramAnonChat.getId())) recipient.setCurrentChat(null);
        telegramProfileJDBC.saveProfiles(owner, recipient);
        telegramAnonChatRepository.delete(telegramAnonChat);
    }

    public List<TelegramAnonChat> getProfileChats(Long profileId){
        TelegramProfile profile = telegramProfileJDBC.getProfileById(profileId);
        List<TelegramAnonChat> chats = new ArrayList<>();
        Hibernate.initialize(profile.getOwnedChats());
        Hibernate.initialize(profile.getReceivedChats());
        chats.addAll(profile.getOwnedChats());
        chats.addAll(profile.getReceivedChats());
        chats = chats.stream().filter(TelegramAnonChat::isActive).collect(Collectors.toList());
        return chats;
    }




}
