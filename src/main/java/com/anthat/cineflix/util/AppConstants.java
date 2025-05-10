package com.anthat.cineflix.util;

public class AppConstants {
    public static final int CHUNK_SIZE = 1024*1024;

    public static final String QUERY_FIND_ALL_VIDEO_NEW_ARRIVALS = "SELECT v FROM Video v WHERE v.createdAt >= :startTime";

    public static final String QUERY_FIND_ALL_WITH_TAGS = "SELECT v FROM Video v LEFT JOIN FETCH v.videoTags";

    public static final String QUERY_FIND_ALL_BY_QUERY = "SELECT v FROM Video v WHERE LOWER(v.videoTitle) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(v.videoDescription) LIKE LOWER(CONCAT('%', :query, '%'))";

    public static final String QUERY_FIND_ALL_BY_CATEGORY = "SELECT v FROM Video v LEFT JOIN FETCH v.videoTags vt WHERE LOWER(vt.catId) = :category";

    public static final String QUERY_FIND_VIDEO_PROG_BY_USER_AND_VIDEO = "SELECT v FROM VideoProgress v WHERE v.user.userName = :username AND v.video.videoId = :videoId";

    public static final String QUERY_FIND_VIDEO_PROG_BY_USER = "SELECT v FROM VideoProgress v WHERE v.user.userName = :username";


}
