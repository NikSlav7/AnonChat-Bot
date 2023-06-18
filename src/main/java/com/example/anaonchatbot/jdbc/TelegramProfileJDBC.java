package com.example.anaonchatbot.jdbc;

import com.example.anaonchatbot.domains.TelegramAnonChat;
import com.example.anaonchatbot.domains.TelegramProfile;
import com.example.anaonchatbot.repository.TelegramAnonChatRepository;
import com.example.anaonchatbot.repository.TelegramProfileRepository;
import org.springframework.stereotype.Component;

@Component
public class TelegramProfileJDBC {


    private final TelegramAnonChatRepository anonChatRepository;
    private final TelegramProfileRepository telegramProfileRepository;

    public TelegramProfileJDBC(TelegramAnonChatRepository anonChatRepository, TelegramProfileRepository telegramProfileRepository) {
        this.anonChatRepository = anonChatRepository;
        this.telegramProfileRepository = telegramProfileRepository;
    }

    public void createProfileIfNotExists(Long chatId, String phoneNumber, String username){
        if (existsByProfileId(chatId)) return;
        TelegramProfile profile = new TelegramProfile(chatId, phoneNumber, username);
        telegramProfileRepository.save(profile);
    }

    public boolean existsByProfileId(Long profileId){
        return telegramProfileRepository.existsByProfileId(profileId);
    }

    public boolean existsByPhoneNumber(String phoneNum){
        return telegramProfileRepository.existsByPhoneNumber(phoneNum);
    }

    public TelegramProfile getProfileById(Long id){
        return telegramProfileRepository.getTelegramProfileByProfileId(id).orElseThrow();
    }
    public void saveProfiles(TelegramProfile... profiles){
        for (TelegramProfile profile : profiles){
            telegramProfileRepository.save(profile);
        }
    }

    public void setCurrentChatId(Long id, String chatId){
        TelegramProfile profile = telegramProfileRepository.getTelegramProfileByProfileId(id).orElseThrow();
        TelegramAnonChat chat = anonChatRepository.getTelegramAnonChatById(chatId).orElseThrow();
        profile.setCurrentChat(chat);
        telegramProfileRepository.save(profile);
    }

    public boolean checkIfBanned(TelegramProfile profile, Long chatId){
        return profile.getBannedIds().contains(chatId);
    }

    public void setCurrentChatIdToNull(Long id){
        TelegramProfile profile = telegramProfileRepository.getTelegramProfileByProfileId(id).orElseThrow();
        profile.setCurrentChat(null);
        telegramProfileRepository.save(profile);
    }

    public TelegramAnonChat getCurrentAnonChat(Long id){
        return getProfileById(id).getCurrentChat();
    }


    public void ban(TelegramProfile baner, TelegramProfile banee){
        baner.getBannedIds().add(banee.getProfileId());
        saveProfiles(baner);
    }

    public TelegramProfile getChatsOtherPart(TelegramProfile telegramProfile){
        TelegramAnonChat cur = telegramProfile.getCurrentChat();
        return cur.getOwner().getProfileId().equals(telegramProfile.getProfileId()) ? cur.getRecipient() : cur.getOwner();
    }

    public void deleteTelegramProfile(Long id){
        telegramProfileRepository.deleteById(id);
    }
}
