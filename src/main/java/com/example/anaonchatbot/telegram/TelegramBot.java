package com.example.anaonchatbot.telegram;

import com.example.anaonchatbot.domains.TelegramAnonChat;
import com.example.anaonchatbot.domains.TelegramProfile;
import com.example.anaonchatbot.exceptions.ChatAlreadyExistsException;
import com.example.anaonchatbot.jdbc.TelegramChatJDBC;
import com.example.anaonchatbot.jdbc.TelegramProfileJDBC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

import static com.example.anaonchatbot.telegram.MessageRepo.CHAT_ENTER_NOTE;


@Component
public class TelegramBot extends TelegramLongPollingBot {



    @Value("${telegram.bot.token}")
    private String botToken;

    private final static String botUsername = "secretchatty_bot";

    private final TelegramProfileJDBC telegramProfileJDBC;

    private final TelegramChatJDBC telegramChatJDBC;


    @Autowired
    public TelegramBot(TelegramProfileJDBC telegramProfileJDBC, TelegramChatJDBC telegramChatJDBC) {
        this.telegramProfileJDBC = telegramProfileJDBC;
        this.telegramChatJDBC = telegramChatJDBC;
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() && !update.hasCallbackQuery()) return;
        if (update.hasMessage() && update.getMessage().getFrom().getIsBot()) return;
        Map<Long, String> commands = LastCommandsRepo.lastCommands;
        long chatId = update.hasMessage() ? update.getMessage().getChatId() : update.getCallbackQuery().getMessage().getChatId();



        //if user hasn't setup their profile yet
        if ("setcontact".equals(commands.get(chatId)) && ! update.getMessage().hasContact()) {
            try {
                sendMessage("Please send your contact to proceed", chatId, null);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        if (!telegramProfileJDBC.existsByProfileId(chatId) && !"setcontact".equals(commands.get(chatId))){
            try {
                sendMessage("Please send me your contact. It will allow other users of the bot send you anonymous messages",
                        chatId, KeyboardMarkupManager.getSendContactMarkup());
                commands.put(chatId, "setcontact");
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        //checking if user needed to confirm, but instead sent some random info
        if ("confirm".equals(commands.get(chatId)) && !update.hasCallbackQuery()){
            commands.put(chatId, "chatting");
        }

        //if user is now in chat. In that case commands are disabled
        if ("chatting".equals(commands.get(chatId))){
            TelegramAnonChat currentChat = telegramProfileJDBC.getCurrentAnonChat(chatId);
            TelegramProfile recipient = currentChat.getRecipient().getProfileId() == chatId ? currentChat.getOwner() : currentChat.getRecipient();
            boolean areBothAtSameChat = recipient.getCurrentChat() != null && recipient.getCurrentChat().getId().equals(currentChat.getId());
            Message message = update.getMessage();
            if (!message.hasText()){
                try {
                    sendMessage("You can send only text messages",
                            chatId, KeyboardMarkupManager.getChatReplyKeyboardMarkup());

                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                return;
            }


            if (message.getText().equals("Exit chat")){
                try {
                    sendMessage("Leaving chat", chatId, KeyboardMarkupManager.getDefaultReplyKeyboardMarkup());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                telegramProfileJDBC.setCurrentChatIdToNull(chatId);
                commands.put(chatId, null);
            }
            //if user wants to ban
            else if (message.getText().equals("Ban")){
                try {
                    sendConfirmMessage(chatId);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                //setting what command is being confirmed
                ConfirmCommandsRepo.setCommand(chatId, "Ban");
                commands.put(chatId,"confirm");
            }
            //if user wants to delete chat
            else if (message.getText().equals("Delete chat")){
                try {
                    sendConfirmMessage(chatId);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                //setting what command is being confirmed

                ConfirmCommandsRepo.setCommand(chatId, "Delete chat");
                commands.put(chatId,"confirm");

            }
            //casual chat message
            else {
                Long writeToId = chatId == currentChat.getOwner().getProfileId() ? currentChat.getRecipient().getProfileId() : currentChat.getOwner().getProfileId();
                try {
                    StringBuilder builder = new StringBuilder();
                    if (!areBothAtSameChat)
                        builder.append(String.format("You have a new message in chat '%s':\n\n", currentChat.getOwnerName()));
                    sendMessage(builder.toString() + message.getText(), writeToId, KeyboardMarkupManager.getChatReplyKeyboardMarkup());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        //if that's a message
        else if (update.hasMessage()){
            String messageText = update.getMessage().hasText() ? update.getMessage().getText() : "";
            String commandToSet  = commands.get(chatId);
            //if message is a command
            if (isCommand(messageText)){
                if ("newchatusername".equals(commands.get(chatId))){
                    telegramChatJDBC.deleteChat(CreatingChatsRepo.getChatId(chatId));
                    CreatingChatsRepo.setChatId(chatId, null);
                }

                if (messageText.equals("/start")) {
                    try {
                        sendMessage(MessageRepo.HELLO_MESSAGE, chatId, KeyboardMarkupManager.getDefaultReplyKeyboardMarkup());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    if (!telegramProfileJDBC.existsByProfileId(chatId)){
                        try {
                            sendMessage("Please send me your contact. It will allow other users of the bot send you anonymous messages",
                                    chatId, KeyboardMarkupManager.getSendContactMarkup());
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                        commandToSet = "setcontact";
                    }

                }
                else if (messageText.equals("/chat")) {

                    //profile wasn't yet created


                    //profile is created
                    try {
                        sendMessage("You're ready to go!", chatId, KeyboardMarkupManager.getStartChattingKeyboardMarkup());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    commands.put(chatId, "/chat");


                }
                else if ("/allchats".equals(messageText)){
                    try {
                        printAllChats(chatId);
                        commandToSet="choosingchat";
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }

                else if ("/deletechat".equals(messageText)){
                    try {
                        printAllChats(chatId);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    commandToSet = "/deletechat";
                }

                //updating last command

            }
            //that's not a command
            else {
                if ("setcontact".equals(commands.get(chatId))) {
                    if (update.getMessage().hasContact()) {
                        Contact contact = update.getMessage().getContact();

                        //if the user sent his contact
                        if (contact.getUserId().equals(update.getMessage().getFrom().getId())){
                            telegramProfileJDBC.createProfileIfNotExists(chatId, contact.getPhoneNumber().replace("+",""), update.getMessage().getFrom().getUserName());
                            try {
                                sendMessage("You're ready to go!", chatId, KeyboardMarkupManager.getStartChattingKeyboardMarkup());
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                            commandToSet = "/chat";

                        }
                        //user send not his contact
                        else {
                            try {
                                sendMessage("That's not your contact, please send yours", chatId, KeyboardMarkupManager.getSendContactMarkup());
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    else {
                        if (telegramProfileJDBC.existsByProfileId(chatId)) {
                            try {
                                sendMessage("You're ready to go!", chatId, KeyboardMarkupManager.getStartChattingKeyboardMarkup());
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            try {
                                sendMessage("Please send me your contact. It will allow other users of the bot send you anonymous messages",
                                        chatId, KeyboardMarkupManager.getSendContactMarkup());
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                            commandToSet = "setcontact";
                        }
                    }
                    //user wants to create a new chat
                }
                else if ("choosingchat".equals(commands.get(chatId))){
                    Integer num = null;
                    try {
                        num = Integer.parseInt(messageText);
                    } catch (Exception e){
                        try {
                            sendMessage("Just type a number of the chat", chatId, KeyboardMarkupManager.getDefaultReplyKeyboardMarkup());
                            return;
                        } catch (TelegramApiException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    //user numbers start from 1
                    num--;
                    List<TelegramAnonChat> chats = ChatsRepo.getChatsById(chatId);
                    if (num >= chats.size() || num < 0) {
                        try {
                            sendMessage("There is no chat with such number",chatId, KeyboardMarkupManager.getDefaultReplyKeyboardMarkup());
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    telegramProfileJDBC.setCurrentChatId(chatId, chats.get(num).getId());
                    commandToSet = "chatting";
                    try {
                        sendMessage("Entering chat with..." + chats.get(num).getOwnerName() + "\n" + CHAT_ENTER_NOTE, chatId, KeyboardMarkupManager.getChatReplyKeyboardMarkup());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }else if ("/deletechat".equals(commands.get(chatId))){
                    Integer num = null;
                    try {
                        num = Integer.parseInt(messageText);
                    } catch (Exception e){
                        try {
                            sendMessage("Just type a number of the chat", chatId, KeyboardMarkupManager.getDefaultReplyKeyboardMarkup());
                            return;
                        } catch (TelegramApiException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    //user numbers start from 1
                    num--;
                    List<TelegramAnonChat> chats = ChatsRepo.getChatsById(chatId);
                    if (num >= chats.size() || num < 0) {
                        try {
                            sendMessage("There is no chat with such number",chatId, KeyboardMarkupManager.getDefaultReplyKeyboardMarkup());
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    //deleting chat and informing user
                    TelegramAnonChat toDelete = chats.get(num);
                    chats.remove(num);
                    TelegramProfile informee = toDelete.getOwner().getProfileId().equals(chatId) ? toDelete.getRecipient() : toDelete.getOwner();
                    if (telegramChatJDBC.isCurrentlyInChat(informee, chatId)) {
                        try {
                            informUserChatBeenDeleted(informee.getProfileId());
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                        commands.put(informee.getProfileId(), "");
                    }
                    telegramChatJDBC.deleteAndTurnOffChat(toDelete);


                    //
                    commandToSet = "";
                    try {
                        sendMessage("Deleting chat " + chats.get(num).getOwnerName(), chatId, KeyboardMarkupManager.getDefaultReplyKeyboardMarkup());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
                else if ("newchat".equals(commands.get(chatId))){
                    if (!update.getMessage().hasContact()){
                        try {
                            sendMessage("Please, send me a contact of the person", chatId, KeyboardMarkupManager.getDefaultReplyKeyboardMarkup());

                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else {
                        Contact contact = update.getMessage().getContact();

                        if (contact.getUserId().equals(chatId)){
                            try {
                                sendMessage("You can't send messages to yourself", chatId,KeyboardMarkupManager.getDefaultReplyKeyboardMarkup());
                                commandToSet = null;
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        //if the user doesn't exist
                        else if (!telegramProfileJDBC.existsByPhoneNumber(contact.getPhoneNumber().replace("+",""))){
                            try {
                                sendMessage("This user doesn't use our bot. You cannot send messages to him", chatId,KeyboardMarkupManager.getDefaultReplyKeyboardMarkup());
                                commandToSet =  null;
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        //if chat with that user exists
                        else if (telegramChatJDBC.existsByOwnerAndRecipient(chatId, contact.getUserId())){
                            try {
                                sendMessage("You already have chat with that person", chatId,KeyboardMarkupManager.getDefaultReplyKeyboardMarkup());
                                commandToSet = null;
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        //if that user banned that profile
                        else if (telegramProfileJDBC.checkIfBanned(telegramProfileJDBC.getProfileById(contact.getUserId()) , chatId)){
                            try {
                                sendMessage("You can't create chat with that user because they banned you", chatId, KeyboardMarkupManager.getStartChattingKeyboardMarkup());
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                            commandToSet = "";
                        }
                        //creating chat
                        else {
                            try {
                                String id = telegramChatJDBC.createTelegramAnonChatIfNotExists(telegramProfileJDBC.getProfileById(chatId),
                                        telegramProfileJDBC.getProfileById(contact.getUserId()), "").getId();
                                CreatingChatsRepo.setChatId(chatId, id);
                            } catch (ChatAlreadyExistsException e) {
                                throw new RuntimeException(e);
                            }
                            try {
                                sendMessage("What will the be name of the chat? This name will also be shown to the recipient", chatId, KeyboardMarkupManager.getDefaultReplyKeyboardMarkup());
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                            commandToSet = "newchatusername";
                        }
                    }

                } else if ("newchatusername".equals(commands.get(chatId))){
                    //creating chat
                    if (update.getMessage().hasText()){
                        telegramChatJDBC.activateChat(CreatingChatsRepo.getChatId(chatId));
                        telegramChatJDBC.setTelegramChatOwnerName(CreatingChatsRepo.getChatId(chatId), messageText);
                        try {
                            sendMessage("Entering the chat with.... "  + messageText + "\n" + CHAT_ENTER_NOTE, chatId, KeyboardMarkupManager.getChatReplyKeyboardMarkup());
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                        telegramProfileJDBC.setCurrentChatId(chatId, CreatingChatsRepo.getChatId(chatId));
                        CreatingChatsRepo.setChatId(chatId, null);
                        commandToSet = "chatting";
                    }

                    else {
                        try {
                            sendMessage("What will the be name of the chat? This name will also be shown to the recipient", chatId, KeyboardMarkupManager.getDefaultReplyKeyboardMarkup());
                            commandToSet = commands.get(chatId);
                            return;
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }



            }
            commands.put(chatId,commandToSet);


        }

        //if that's a callback query
        else {
            String query = update.getCallbackQuery().getData();
            if (query.equals("newchat")){
                commands.put(chatId, query);
                try {
                    sendMessage("Nice! Now send me a contact of the person you want to start chat with", chatId, KeyboardMarkupManager.getDefaultReplyKeyboardMarkup());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
            if ("browsechats".equals(query)){
                try {
                    printAllChats(chatId);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                commands.put(chatId, "choosingchat");
            }
            if ("confirm".equals(commands.get(chatId))){
                //canceled confirmation
                if (query.equals("yes")){
                    String commandToConfirm = ConfirmCommandsRepo.getCommand(chatId);
                    //user confirms ban
                    if (commandToConfirm.equals("Ban")){
                        TelegramProfile baner = telegramProfileJDBC.getProfileById(chatId);
                        TelegramProfile banee = telegramProfileJDBC.getChatsOtherPart(baner);
                        telegramProfileJDBC.ban(baner, banee);
                        TelegramAnonChat toDelete = telegramProfileJDBC.getCurrentAnonChat(chatId);
                        TelegramProfile informee = toDelete.getOwner().getProfileId().equals(chatId) ? toDelete.getRecipient() : toDelete.getOwner();
                        try {
                            if (telegramChatJDBC.isCurrentlyInChat(informee, chatId)) {
                                informUserChatBeenDeleted(informee.getProfileId());
                                commands.put(informee.getProfileId(), "");
                            }
                            sendMessage("You deleted the chat", chatId, KeyboardMarkupManager.getChatReplyKeyboardMarkup());
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                        telegramChatJDBC.deleteAndTurnOffChat(toDelete);
                        commands.put(chatId, "");
                    }
                    else if (commandToConfirm.equals("Delete chat")){
                        TelegramAnonChat toDelete = telegramProfileJDBC.getCurrentAnonChat(chatId);
                        TelegramProfile informee = toDelete.getOwner().getProfileId().equals(chatId) ? toDelete.getRecipient() : toDelete.getOwner();
                        try {
                            if (telegramChatJDBC.isCurrentlyInChat(informee, chatId)) {
                                informUserChatBeenDeleted(informee.getProfileId());
                                commands.put(informee.getProfileId(), "");
                            }
                            sendMessage("Chat was deleted", chatId, KeyboardMarkupManager.getChatReplyKeyboardMarkup());
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                        telegramChatJDBC.deleteAndTurnOffChat(toDelete);
                        commands.put(chatId, "");
                    }
                } else {
                    commands.put(chatId, "");
                    try {
                        sendMessage("You didn't confirm the action, but the chat was left. To enter the chat again user /allchats command",
                                chatId, KeyboardMarkupManager.getDefaultReplyKeyboardMarkup());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }



            }
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    //print and temp save all chats of a person
    private void printAllChats(long chatId) throws TelegramApiException {
        List<TelegramAnonChat> chats = telegramChatJDBC.getProfileChats(chatId);
        StringBuilder builder = new StringBuilder();
        builder.append("Here is the list of all chats\n");
        for (int i = 0; i < chats.size(); i++){
            builder.append(String.format("%s) %s", i+1, chats.get(i).getOwnerName()));
            builder.append("\n");
        }
        builder.append("Type the number of the chat to enter it");
        ChatsRepo.setChatId(chatId, chats);
        sendMessage(chats.size() != 0 ? builder.toString() : "No chats started yet", chatId, null);
    }


    private void sendMessage(String message, long chatId, ReplyKeyboard replyKeyboard) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        sendMessage.setReplyMarkup(replyKeyboard);
        execute(sendMessage);
    }

    private void sendConfirmMessage(long chatId) throws TelegramApiException{
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Are you sure?");
        sendMessage.setReplyMarkup(KeyboardMarkupManager.getConfirmationMarkup());
        execute(sendMessage);
    }
    private void informUserChatBeenDeleted(Long chatId) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("This user deleted chat with you");
        sendMessage.setReplyMarkup(KeyboardMarkupManager.getDefaultReplyKeyboardMarkup());
        execute(sendMessage);
    }

    private boolean isCommand(String message){
        return CommandsRepo.getCommands().contains(message);
    }
}
