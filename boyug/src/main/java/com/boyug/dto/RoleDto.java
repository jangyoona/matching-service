package com.boyug.dto;

import com.boyug.entity.RoleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDto {

    private int roleId;
    private String roleName;
    private String roleDesc;

    public RoleEntity toEntity() {
        return RoleEntity.builder()
                .roleName(roleName)
                .roleDesc(roleDesc)
                .build();
    }

    public static RoleDto of(RoleEntity roleEntity) {
        return RoleDto.builder()
                .roleId(roleEntity.getRoleId())
                .roleName(roleEntity.getRoleName())
                .roleDesc(roleEntity.getRoleDesc())
                .build();

    }
}