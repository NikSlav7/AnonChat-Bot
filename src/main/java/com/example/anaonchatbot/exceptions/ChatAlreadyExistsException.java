package com.example.anaonchatbot.exceptions;

public class ChatAlreadyExistsException extends Exception{
    public ChatAlreadyExistsException(String message) {
        super(message);
    }
}
