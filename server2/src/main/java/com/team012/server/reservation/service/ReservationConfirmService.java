package com.team012.server.reservation.service;

import com.team012.server.common.config.userDetails.PrincipalDetails;
import com.team012.server.company.entity.Company;
import com.team012.server.company.service.CompanyService;
import com.team012.server.reservation.dto.TotalReservationDto;
import com.team012.server.reservation.entity.Reservation;
import com.team012.server.reservation.repository.ReservationRepository;
import com.team012.server.users.entity.DogCard;
import com.team012.server.users.service.DogCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class ReservationConfirmService {

    private final CompanyService companyService;
    private final ReservationRepository reservationRepository;
    private final DogCardService dogCardService;

    @Transactional(readOnly = true)
    public List<TotalReservationDto> confirmReservation(PrincipalDetails principalDetails, Long reservationId) {

        List<TotalReservationDto> confirmReservationList = new ArrayList<>();

        Long userId = principalDetails.getUsers().getId();

        Optional<Reservation> byUsersIdAndReservedId = reservationRepository.findByUsersIdAndReservedId(userId, reservationId);

        Reservation findReservation = byUsersIdAndReservedId.orElse(null);



        // 예약한 강아지 한마리 마다 확인.
        for (int i = 0; i < findReservation.getDogIdList().size(); i++) {
            Long aLong = findReservation.getDogIdList().get(i);
            DogCard findDogCard = dogCardService.findById(aLong);

            // 회사정보 조회
            Company companyByCompanyId = companyService.getCompanyByCompanyId(findReservation.getCompanyId());


            TotalReservationDto reservationDogCard = TotalReservationDto.builder()
                    .dogName(findDogCard.getDogName())
                    .photoImgUrl(findDogCard.getPhotoImgUrl())
                    .type(findDogCard.getType())
                    .gender(findDogCard.getGender())
                    .age(findDogCard.getAge())
                    .weight(findDogCard.getWeight())
                    .snackMethod(findDogCard.getSnackMethod())
                    .bark(findDogCard.getBark())
                    .surgery(findDogCard.getSurgery())
                    .bowelTrained(findDogCard.getBowelTrained())
                    .etc(findDogCard.getEtc())
                    .name(findReservation.getUserInfo().getName())
                    .phone(findReservation.getUserInfo().getPhone())
                    .address(companyByCompanyId.getAddress())
                    .checkInDate(findReservation.getCheckInDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .checkOutDate(findReservation.getCheckOutDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .checkInTime(findReservation.getCheckInTime().format(DateTimeFormatter.ofPattern("a hh:mm").withLocale(Locale.KOREAN)))
                    .checkOutTime(findReservation.getCheckOutTime().format(DateTimeFormatter.ofPattern("a hh:mm").withLocale(Locale.KOREAN)))
                    .totalPrice(findReservation.getTotalPrice())
                    .build();

            confirmReservationList.add(reservationDogCard);

        }
        return confirmReservationList;
    }
}
