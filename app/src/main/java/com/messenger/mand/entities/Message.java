package com.messenger.mand.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class Message {

    private String text;
    private String time;
    private String sender;
    private String recipient;
    private String imageUrl;
    private byte[] imageBytes;
    private boolean isMineMessage;
    private String isSeen;
}
