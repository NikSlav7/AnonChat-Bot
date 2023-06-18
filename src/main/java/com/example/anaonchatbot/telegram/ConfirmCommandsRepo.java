package com.example.anaonchatbot.telegram;

import java.util.HashMap;
import java.util.Map;

public class ConfirmCommandsRepo {
    private static Map<Long,String> commands = new HashMap<>();


    public static Map<Long,String> getCommands(){
        return commands;
    }


    public static String getCommand(Long userId){
        return getCommands().get(userId);
    }

    public static void setCommand(Long userId, String chatId){
        getCommands().put(userId, chatId);
    }}
