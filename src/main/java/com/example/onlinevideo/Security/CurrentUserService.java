package com.example.onlinevideo.Security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    /**
     * 获取当前登录的用户Id
     */
    public Integer getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
//            System.out.println("当前登录的userId：    " + ((CustomUserDetails) principal).getUserId());
            return ((CustomUserDetails) principal).getUserId();
        } else {
            throw new IllegalStateException("当前用户信息无效");
        }
    }

    public boolean isAdmin(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * 检查当前用户是否是目标用户（防止越权操作）
     *
     * @param targetUserId 目标用户ID
     * @return 是否匹配
     */
    public boolean isCurrentUser(Integer targetUserId) {
//        System.out.println("请求修改的userId：    " + targetUserId);
        return targetUserId != null && targetUserId.equals(getCurrentUserId());
    }
}