package com.sample.system.vault.service.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Status {
    private Long id;
    private String code;
    private String persianDescription;
}
