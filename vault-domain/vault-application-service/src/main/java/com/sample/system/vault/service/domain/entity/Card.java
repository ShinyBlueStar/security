package com.sample.system.vault.service.domain.entity;

import com.pcp.banking.system.entity.BaseEntity;
import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class Card {
    private String plainPin;
    private String hashPin;
}
