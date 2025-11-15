package com.hrservice.hrservice.security;

import com.hrservice.hrservice.annotation.RequiresRole;
import com.hrservice.hrservice.dto.UserInfo;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class RoleAuthorizationAspect {

    @Before("@annotation(requiresRole)")
    public void checkRole(JoinPoint joinPoint, RequiresRole requiresRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof UserInfo)) {
            throw new SecurityException("Unauthorized");
        }

        UserInfo userInfo = (UserInfo) authentication.getPrincipal();
        String userRole = userInfo.getRole().toLowerCase();
        String[] requiredRoles = requiresRole.value();

        boolean hasRole = Arrays.stream(requiredRoles)
                .anyMatch(role -> role.toLowerCase().equals(userRole));

        if (!hasRole) {
            throw new SecurityException("Forbidden: Insufficient permissions");
        }
    }
}

