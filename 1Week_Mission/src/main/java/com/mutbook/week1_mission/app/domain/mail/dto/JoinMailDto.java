package com.mutbook.week1_mission.app.domain.mail.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class JoinMailDto {
    @NotNull
    private String address;
    @NotNull
    private String subject;
    @NotNull
    private String body;
}