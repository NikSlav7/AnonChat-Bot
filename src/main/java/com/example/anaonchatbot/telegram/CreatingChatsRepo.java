package com.example.anaonchatbot.telegram;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CreatingChatsRepo {
    private static Map<Long,String> chatIds = new HashMap<>();


    public static Map<Long,String> chatIds(){
        return chatIds;
    }


    public static String getChatId(Long userId){
        return chatIds().get(userId);
    }

    public static void setChatId(Long userId, String chatId){
        chatIds().put(userId, chatId);
    }
}
