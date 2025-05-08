package com.anthat.cineflix.util;

public class AppConstants {
    public static final int CHUNK_SIZE = 1024*1024;

    public static final String QUERY_FIND_ALL_WITH_TAGS = "SELECT v FROM Video v LEFT JOIN FETCH v.videoTags";

    public static final String QUERY_FIND_ALL_BY_QUERY = "SELECT v FROM Video v WHERE LOWER(v.videoTitle) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(v.videoDescription) LIKE LOWER(CONCAT('%', :query, '%'))";

    public static final String QUERY_FIND_ALL_BY_CATEGORY = "SELECT v FROM Video v LEFT JOIN FETCH v.videoTags vt WHERE LOWER(vt.catId) = :category";


}
