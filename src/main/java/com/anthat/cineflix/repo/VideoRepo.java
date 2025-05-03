package com.anthat.cineflix.repo;

import com.anthat.cineflix.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepo extends JpaRepository<Video, String> {
//    @Query("SELECT v FROM Video v LEFT JOIN FETCH v.videoTags WHERE v.videoId = :videoId")
//    Video findVideoByIdWithTags(String videoId);
//
//    @Query("SELECT v FROM Video v LEFT JOIN FETCH v.videoCast WHERE v.videoId = :videoId")
//    Video findVideoByIdWithCast(String videoId);
//
    @Override
    @Query("SELECT v FROM Video v LEFT JOIN FETCH v.videoTags")
    List<Video> findAll();

    @Query("SELECT v FROM Video v WHERE " +
            "LOWER(v.videoTitle) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(v.videoDescription) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Video> findAllByQuery(String query);

    @Query("SELECT v FROM Video v LEFT JOIN FETCH v.videoTags vt WHERE LOWER(vt.catId) = :category")
    List<Video> findAllByCategory(String category);
//
//    @Query("SELECT v FROM Video v LEFT JOIN FETCH v.videoCast")
//    List<Video> findAllWithCast();
}
