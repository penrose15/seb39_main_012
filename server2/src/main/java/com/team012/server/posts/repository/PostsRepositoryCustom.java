package com.team012.server.posts.repository;

import com.team012.server.posts.entity.Posts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostsRepositoryCustom {

    Page<Posts> searchPageByTitleAndContents(String title, String contents, Pageable pageable);
    Page<RoomPriceDto> findAllRoomMinPriceTitleOrContentsContaining(String title, String contents,Pageable pageable);

    Page<Posts> findByAddressContaining(String address, Pageable pageable);

    Page<RoomPriceDto> findAllRoomMinPriceAddressContaining(Pageable pageable, String address);


}
