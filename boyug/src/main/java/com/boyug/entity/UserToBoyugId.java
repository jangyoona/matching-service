package com.boyug.entity;

import jakarta.persistence.IdClass;

import java.io.Serializable;

public class UserToBoyugId implements Serializable {
    private int userId;
    private int boyugProgramDetailId;
}
