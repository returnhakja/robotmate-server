package kr.robotmate.server.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentLikeResponse {
    private boolean liked;
    private long likeCount;
}
