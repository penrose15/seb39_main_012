package com.team012.server.posts.service;

import com.team012.server.posts.Tag.HashTag.converter.HashTagConverter;
import com.team012.server.posts.Tag.HashTag.dto.HashTagResponseDto;
import com.team012.server.posts.Tag.HashTag.entity.PostsHashTags;
import com.team012.server.posts.Tag.ServiceTag.converter.ServiceTagConverter;
import com.team012.server.posts.Tag.ServiceTag.dto.ServiceResponseDto;
import com.team012.server.posts.Tag.ServiceTag.entity.PostsServiceTag;
import com.team012.server.posts.dto.PostsResponseDto;
import com.team012.server.posts.entity.Posts;
import com.team012.server.posts.img.converter.PostsImgConverter;
import com.team012.server.posts.img.dto.ImageDto;
import com.team012.server.review.dto.ReviewPostsResponse;
import com.team012.server.room.converter.RoomConverter;
import com.team012.server.room.dto.RoomDto;
import com.team012.server.room.entity.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Component
public class PostsCombineService {

    private final PostsImgConverter postsImgConverter;
    private final HashTagConverter hashTagConverter;
    private final ServiceTagConverter serviceTagConverter;
    private final RoomConverter roomConverter;

    public PostsResponseDto combine(Long companyId, Posts posts, List<Room> roomList1,
                                    List<PostsHashTags> postsHashTags,
                                    List<PostsServiceTag> postsServiceTags) {
        List<ImageDto> imageDtos = postsImgConverter.toListDTO(posts.getPostsImgList());
        List<HashTagResponseDto> hashTags = hashTagConverter.toListDTO(postsHashTags);
        List<ServiceResponseDto> serviceTags = serviceTagConverter.toListDTO(postsServiceTags);
        List<RoomDto> roomDtos = roomConverter.toListDTO(roomList1);

        return PostsResponseDto.builder()
                .id(posts.getId())
                .title(posts.getTitle())
                .content(posts.getContent())
                .latitude(posts.getLatitude())
                .longitude(posts.getLongitude())
                .address(posts.getAddress())
                .detailAddress(posts.getDetailAddress())
                .phone(posts.getPhone())
                .companyId(companyId)
                .avgScore(posts.getAvgScore())
                .likesCount(posts.getLikesCount())
                .checkInTime(posts.getCheckInTime().format(DateTimeFormatter.ofPattern("a hh:mm").withLocale(Locale.KOREA)))
                .checkOutTime(posts.getCheckOutTime().format(DateTimeFormatter.ofPattern("a hh:mm").withLocale(Locale.KOREA)))
                .postsImgList(imageDtos)
                .hashTag(hashTags)
                .serviceTag(serviceTags)
                .roomDtos(roomDtos)
                .build();
    }

    public PostsResponseDto combine(Posts posts, List<ReviewPostsResponse> reviewList, List<Room> roomList) {
        List<ImageDto> imageDtos = postsImgConverter.toListDTO(posts.getPostsImgList());
        List<HashTagResponseDto> hashTagResponseDtos = hashTagConverter.toListDTO(posts.getPostsHashTags());
        List<ServiceResponseDto> serviceResponseDtos = serviceTagConverter.toListDTO(posts.getPostAvailableTags());
        List<RoomDto> roomDtos = roomConverter.toListDTO(roomList);

        return PostsResponseDto.builder()
                .id(posts.getId())
                .title(posts.getTitle())
                .content(posts.getContent())
                .latitude(posts.getLatitude())
                .longitude(posts.getLongitude())
                .address(posts.getAddress())
                .detailAddress(posts.getDetailAddress())
                .phone(posts.getPhone())
                .companyId(posts.getCompanyId())
                .avgScore(posts.getAvgScore())
                .checkInTime(posts.getCheckInTime().format(DateTimeFormatter.ofPattern("a hh:mm")))
                .checkOutTime(posts.getCheckOutTime().format(DateTimeFormatter.ofPattern("a hh:mm")))
                .likesCount(posts.getLikesCount())
                .postsImgList(imageDtos)
                .hashTag(hashTagResponseDtos)
                .serviceTag(serviceResponseDtos)
                .roomDtos(roomDtos)
                .reviewList(reviewList)
                .build();
    }


}
