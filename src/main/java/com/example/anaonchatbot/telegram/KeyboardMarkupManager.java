package com.example.anaonchatbot.telegram;


import java.util.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

public class KeyboardMarkupManager {


    public static ReplyKeyboardMarkup getSendContactMarkup(){
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow firstRow = new KeyboardRow();
        KeyboardButton keyboardButton = new KeyboardButton("Send contact");
        keyboardButton.setRequestContact(true);
        firstRow.add(keyboardButton);
        keyboardRows.add(firstRow);
        markup.setKeyboard(keyboardRows);
        return markup;
    }

    public static InlineKeyboardMarkup getStartChattingKeyboardMarkup(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton newChat = new InlineKeyboardButton();
        newChat.setCallbackData("newchat");
        newChat.setText("New Chat");

        InlineKeyboardButton browseChats = new InlineKeyboardButton();
        browseChats.setCallbackData("browsechats");
        browseChats.setText("Browse Chats");
        inlineKeyboardMarkup.setKeyboard(List.of(List.of(newChat), List.of(browseChats)));
        return inlineKeyboardMarkup;
    }
    public static ReplyKeyboardMarkup getChatReplyKeyboardMarkup(){
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow firstRow = new KeyboardRow();
        KeyboardButton exitButton = new KeyboardButton("Exit chat");
        firstRow.add(exitButton);


        KeyboardRow second = new KeyboardRow();
        KeyboardButton keyboardButton = new KeyboardButton("Ban");
        KeyboardButton keyboardButton1 = new KeyboardButton("Delete chat");
        second.add(keyboardButton);
        second.add(keyboardButton1);

        keyboardRows.add(firstRow);
        keyboardRows.add(second);
        markup.setKeyboard(keyboardRows);
        return markup;
    }

    public static ReplyKeyboardRemove getDefaultReplyKeyboardMarkup(){
        ReplyKeyboardRemove remove = new ReplyKeyboardRemove();
        remove.setRemoveKeyboard(true);
        return remove;
    }

    public static InlineKeyboardMarkup getConfirmationMarkup(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton yes = new InlineKeyboardButton();
        yes.setCallbackData("yes");
        yes.setText("Yes");

        InlineKeyboardButton no = new InlineKeyboardButton();
        no.setCallbackData("no");
        no.setText("No");
        inlineKeyboardMarkup.setKeyboard(List.of(List.of(yes), List.of(no)));
        return inlineKeyboardMarkup;
    }



}
