package com.team012.server.usersPack.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class DogCardDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Post {

        private String dogName;
        private String type;
        private String gender;
        private Integer age;
        private Double weight;
        private String snackMethod;
        private String bark;
        private String surgery;
        private String bowelTrained;
        private String etc;

    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Response {

        private Long id;
        private String photoImgUrl;
        private String dogName;
        private String type;
        private String gender;
        private Integer age;
        private Double weight;
        private String snackMethod;
        private String bark;
        private String surgery;
        private String bowelTrained;
        private String username;
        private String etc;
    }
}