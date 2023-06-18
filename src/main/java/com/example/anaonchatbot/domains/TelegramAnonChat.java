package com.example.anaonchatbot.domains;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.*;
import org.w3c.dom.stylesheets.LinkStyle;

@Table
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TelegramAnonChat {

    @Id
    private String id;

    private boolean isActive;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private TelegramProfile owner;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private TelegramProfile recipient;


    private String ownerName;

    public TelegramAnonChat(String id, TelegramProfile owner, TelegramProfile recipient,String ownerName){
        this.id = id;
        this.owner = owner;
        this.recipient = recipient;
        this.ownerName = ownerName;
    }


}
