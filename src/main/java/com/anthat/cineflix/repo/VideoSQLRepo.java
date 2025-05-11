package com.anthat.cineflix.repo;

import com.anthat.cineflix.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.anthat.cineflix.util.AppConstants.QUERY_FIND_ALL_BY_CATEGORY;
import static com.anthat.cineflix.util.AppConstants.QUERY_FIND_ALL_BY_QUERY;
import static com.anthat.cineflix.util.AppConstants.QUERY_FIND_ALL_VIDEO_NEW_ARRIVALS;
import static com.anthat.cineflix.util.AppConstants.QUERY_FIND_ALL_WITH_TAGS;

@Repository
public interface VideoSQLRepo extends JpaRepository<Video, String> {

    @Override
    @Query(QUERY_FIND_ALL_WITH_TAGS)
    List<Video> findAll();

    @Query(QUERY_FIND_ALL_BY_QUERY)
    List<Video> findAllByQuery(String query);

    @Query(QUERY_FIND_ALL_BY_CATEGORY)
    List<Video> findAllByCategory(String category);

    @Query(QUERY_FIND_ALL_VIDEO_NEW_ARRIVALS)
    List<Video> findAllNewArrivals(long startTime);
}
