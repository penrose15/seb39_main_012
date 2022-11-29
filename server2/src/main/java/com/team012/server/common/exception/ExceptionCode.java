package com.team012.server.common.exception;

import lombok.Getter;

public enum ExceptionCode {

    COMPANY_NOT_FOUND(404, "Company Not Found"),
    COMPANY_ID_NOT_MATCHED(404, "Company id not matched"),
    INTERNAL_SERVER_ERROR(500, "Method Not Allowed"),
    POST_NOT_FOUND(404, "posts not found"),
    HASH_TAG_NOT_FOUND(404, "hashTag not found"),
    SERVICE_TAG_NOT_FOUND(404, "serviceTag not found"),
    CHECKIN_CHECKOUT_ERROR(405,"checkIn must be lesser then checkOut" ),
    SAME_DAY_RESERVATION_NOT_ALLOWED(405,"Same-day reservations not available"),
    SAME_DAY_RESERVATION_CANCEL_NOT_ALLOWED(405, "Reservation can be canceled the day before"),
    REVIEW_NOT_FOUND(404,"review not found"),
    ROOM_NOT_EXIST(404,"room not exist"),
    DOG_NOT_FOUND(404,"Dog Not Found"),
    MEMBER_NOT_FOUND(404, "Member Not Found");

    @Getter
    private final int status;

    @Getter
    private final String message;

    ExceptionCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
