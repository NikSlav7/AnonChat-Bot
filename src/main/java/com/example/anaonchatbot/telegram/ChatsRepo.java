package com.example.anaonchatbot.telegram;

import com.example.anaonchatbot.domains.TelegramAnonChat;

import java.util.HashMap;
import java.util.*;

public class ChatsRepo {
    private static Map<Long,List<TelegramAnonChat>> chats = new HashMap<>();


    public static Map<Long,List<TelegramAnonChat>> getChats(){
        return chats;
    }


    public static List<TelegramAnonChat> getChatsById(Long userId){
        return getChats().get(userId);
    }

    public static void setChatId(Long userId, List<TelegramAnonChat> chat){
        getChats().put(userId, chat);
    }
}
