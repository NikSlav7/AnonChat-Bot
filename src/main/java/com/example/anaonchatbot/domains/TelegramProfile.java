package com.example.anaonchatbot.domains;

import java.util.*;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TelegramProfile {

    @Id
    //equal to chatId with same person
    private Long profileId;

    private String phoneNumber, username;


    @OneToMany(mappedBy = "owner", fetch = FetchType.EAGER)
    private List<TelegramAnonChat> ownedChats;

    @OneToMany(mappedBy = "recipient", fetch = FetchType.EAGER)
    private List<TelegramAnonChat> receivedChats;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "banned_ids",joinColumns = @JoinColumn(name = "banned_ids"))
    @Column(name = "ids")
    private Set<Long> bannedIds;

    @JoinColumn
    @ManyToOne()
    private TelegramAnonChat currentChat;


    public TelegramProfile(Long profileId, String phoneNumber, String username){
        this.profileId = profileId;
        this.phoneNumber = phoneNumber;
        this.username = username;
    }

    public Set<Long> getBannedIds() {
        return bannedIds == null ? new HashSet() : bannedIds;
    }
}
