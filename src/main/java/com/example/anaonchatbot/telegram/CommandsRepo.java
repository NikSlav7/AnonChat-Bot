package com.example.anaonchatbot.telegram;

import java.util.HashSet;
import java.util.Set;

public class CommandsRepo {


    private static Set<String> commands = null;


    public static Set<String> getCommands(){
        if (commands != null) return commands;
        commands = new HashSet<>();
        commands.add("/start");
        commands.add("/chat");
        commands.add("/allchats");
        commands.add("/deletechat");
        return commands;
    }
}
