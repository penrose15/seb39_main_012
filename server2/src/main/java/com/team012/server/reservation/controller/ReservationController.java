package com.team012.server.reservation.controller;

import com.team012.server.common.exception.BusinessLogicException;
import com.team012.server.common.exception.ExceptionCode;
import com.team012.server.reservation.entity.Reservation;
import com.team012.server.common.config.userDetails.PrincipalDetails;
import com.team012.server.common.response.MultiResponseDto;
import com.team012.server.company.service.CompanyService;
import com.team012.server.reservation.dto.ReservationResponseDto;
import com.team012.server.reservation.service.ReservationService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.team012.server.common.utils.constant.Constant.DELETE_RESERVATION_SUCCESS;

@RestController
@RequestMapping("/v1/company/reservation")
public class ReservationController {

    private final ReservationService reservationService;
    private final CompanyService companyService;
    public ReservationController(ReservationService reservationService, CompanyService companyService) {
        this.reservationService = reservationService;
        this.companyService = companyService;
    }

    // 예약현황 조회 API (회사기준)
    @GetMapping("/list/{companyId}")
    public ResponseEntity showReservation(@PathVariable("companyId") Long companyId,
                                          @RequestParam Integer page,
                                          @RequestParam Integer size,
                                          @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long findCompanyId = companyService.getCompany(principalDetails.getUsers().getId()).getId();
        if(companyId != findCompanyId) throw new BusinessLogicException(ExceptionCode.COMPANY_ID_NOT_MATCHED);

        Page<Reservation> reservationPage= reservationService.getReservation(companyId, page -1, size);
        List<Reservation> reservation = reservationPage.getContent();

        return new ResponseEntity<>(new MultiResponseDto<>(reservation, reservationPage), HttpStatus.OK);
    }

    // 예약확정 API (회사기준)
    @GetMapping("/confirm/{reservationId}")
    public ResponseEntity confirmReservation(@PathVariable("reservationId") Long reservationId) {
        reservationService.confirmReservation(reservationId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 예약취소 API (회사) --> 삭제
    @DeleteMapping("/cancel/{reservationId}")
    public ResponseEntity cancelReservation(@PathVariable("reservationId") Long id){

        reservationService.cancelReservation(id);
        ReservationResponseDto response = ReservationResponseDto.builder()
                                .message(DELETE_RESERVATION_SUCCESS.getMessage())
                                .build();

        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }
}
