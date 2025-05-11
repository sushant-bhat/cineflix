package com.anthat.cineflix.repo;

import com.anthat.cineflix.model.VideoProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.anthat.cineflix.util.AppConstants.QUERY_FIND_VIDEO_PROG_BY_USER;
import static com.anthat.cineflix.util.AppConstants.QUERY_FIND_VIDEO_PROG_BY_USER_AND_VIDEO;

@Repository
public interface VideoProgressSQLRepo extends JpaRepository<VideoProgress, String> {
    @Query(QUERY_FIND_VIDEO_PROG_BY_USER_AND_VIDEO)
    Optional<VideoProgress> findByUserNameAndVideoId(String username, String videoId);

    @Query(QUERY_FIND_VIDEO_PROG_BY_USER)
    List<VideoProgress> findAllByUserName(String username);
}
