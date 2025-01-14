package com.team012.server.reservation.controller;

import com.team012.server.common.config.userDetails.PrincipalDetails;
import com.team012.server.common.response.MultiResponseDto;
import com.team012.server.common.response.SingleResponseDto;
import com.team012.server.posts.dto.ReservationsInfoDto;
import com.team012.server.posts.service.ReservationsBeforeOrAfterCheckOutService;
import com.team012.server.reservation.dto.*;
import com.team012.server.reservation.entity.Reservation;
import com.team012.server.reservation.service.CustomerReservationService;
import com.team012.server.reservation.service.ReservationConfirmService;
import com.team012.server.users.service.DogCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/customer/reservation")
@RequiredArgsConstructor
public class CustomerReservationController {

    private final CustomerReservationService customerReservationService;
    private final ReservationsBeforeOrAfterCheckOutService reservationsBeforeOrAfterCheckOutService;
    private final ReservationConfirmService reservationConfirmService;

    private final DogCardService dogCardService;

    //posts 상세 페이지 ---> 예약 상세 페이지로 이동
    @PostMapping("/{postsId}")
    public ResponseEntity beforeReservation(@PathVariable("postsId") Long postsId,
                                            @RequestBody RegisterReservationDto registerReservationDto,
                                            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Long userId = principalDetails.getUsers().getId();

        // 예약
        ReservationCreateDto reservation
                = customerReservationService.registerReservation(registerReservationDto, userId, postsId);

        return new ResponseEntity<>(reservation, HttpStatus.OK);
    }

    //예약 상세 페이지 ---> 예약 완료 페이지
    @PostMapping("/{postsId}/confirm")
    public ResponseEntity confirmReservation(@PathVariable("postsId") Long postsId,
                                             @RequestBody ReservationConfirmDto dto,
                                             @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Long userId = principalDetails.getUsers().getId();

        ReservationCreateDto reservationCreateDto = dto.getReservationCreateDto();
        ReservationUserInfoDto reservationUserInfoDto = dto.getReservationUserInfoDto();

        Reservation reservation =
                customerReservationService.createReservation(reservationCreateDto,userId, postsId, reservationUserInfoDto);
        return new ResponseEntity<>(reservation, HttpStatus.CREATED);
    }

    //예약 확인 페이지
    @GetMapping("/{reservationId}/final")
    public ResponseEntity finalReservation(@PathVariable("reservationId") Long reservationId,
                                           @AuthenticationPrincipal PrincipalDetails principalDetails) {

        List<TotalReservationDto> totalReservationDtos =
                reservationConfirmService.confirmReservation(principalDetails, reservationId);

        return new ResponseEntity(new SingleResponseDto<>(totalReservationDtos), HttpStatus.OK);
    }

    //가기 전 호텔리스트(체크아웃 최신날짜 순)
    @GetMapping("/before")
    public ResponseEntity findReservationsBeforeCheckIn(@RequestParam int page, int size
            , @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUsers().getId();
        Page<Reservation> reservationList = customerReservationService.findReservationList(userId, page - 1, size);
        List<Reservation> reservations = reservationList.getContent();

        List<ReservationsInfoDto> reservationListDtos = reservationsBeforeOrAfterCheckOutService.reservedHotels(reservations);

        return new ResponseEntity<>(new MultiResponseDto<>(reservationListDtos, reservationList), HttpStatus.OK);
    }

    //갔다온 호텔 리스트 (체크아웃 최신날짜 순)
    @GetMapping("/after")
    public ResponseEntity findReservationAfterCheckOut(@RequestParam int page, int size
            , @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUsers().getId();
        Page<Reservation> reservationPages = customerReservationService.findReservationAfterCheckOutList(userId, page - 1, size);
        List<Reservation> reservations = reservationPages.getContent();

        List<ReservationsInfoDto> reservationListDtos = reservationsBeforeOrAfterCheckOutService.reservedHotels(reservations);

        return new ResponseEntity<>(new MultiResponseDto<>(reservationListDtos, reservationPages), HttpStatus.OK);
    }

    //reservListId 로 삭제
    @DeleteMapping("/{reservation-id}")
    public ResponseEntity deleteReservation(@PathVariable("reservation-id") Long reservationId,
                                            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUsers().getId();
        customerReservationService.deleteReservation(userId, reservationId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
