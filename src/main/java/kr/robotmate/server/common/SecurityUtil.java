package kr.robotmate.server.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtil {

    private SecurityUtil() {}

    /**
     * 현재 로그인한 유저의 ID(UUID)를 반환.
     * 비로그인 상태면 null 반환.
     */
    public static String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }

    /**
     * 로그인 여부 확인.
     */
    public static boolean isAuthenticated() {
        return getCurrentUserId() != null;
    }
}
