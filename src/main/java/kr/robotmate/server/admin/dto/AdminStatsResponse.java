package kr.robotmate.server.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStatsResponse {
    private long totalUsers;
    private long totalPosts;
    private long totalMarketPosts;
    private long totalNews;
    private long pendingReports;
    private long newUsersToday;
    private long newPostsToday;
}
