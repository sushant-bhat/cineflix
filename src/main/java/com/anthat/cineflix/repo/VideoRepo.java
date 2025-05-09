package com.anthat.cineflix.repo;

import com.anthat.cineflix.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.anthat.cineflix.util.AppConstants.QUERY_FIND_ALL_BY_CATEGORY;
import static com.anthat.cineflix.util.AppConstants.QUERY_FIND_ALL_BY_QUERY;
import static com.anthat.cineflix.util.AppConstants.QUERY_FIND_ALL_WITH_TAGS;

@Repository
public interface VideoRepo extends JpaRepository<Video, String> {

    @Override
    @Query(QUERY_FIND_ALL_WITH_TAGS)
    List<Video> findAll();

    @Query(QUERY_FIND_ALL_BY_QUERY)
    List<Video> findAllByQuery(String query);

    @Query(QUERY_FIND_ALL_BY_CATEGORY)
    List<Video> findAllByCategory(String category);

    @Query("SELECT v FROM Video v LEFT JOIN FETCH v.videoTags")
    List<Video> findAllPending();
}
