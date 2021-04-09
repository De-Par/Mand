package com.messenger.mand.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class Chat {

    private String initiator;
    private String recipient;
    private String iniPublicKey;
    private String recPublicKey;
}