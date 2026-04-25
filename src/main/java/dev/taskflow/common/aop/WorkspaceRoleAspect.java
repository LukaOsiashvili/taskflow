package dev.taskflow.common.aop;

import dev.taskflow.common.exception.AccessDeniedException;
import dev.taskflow.user.UserService;
import dev.taskflow.workspace.WorkspaceMemberRepository;
import dev.taskflow.workspace.WorkspaceRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkspaceRoleAspect {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserService userService;

    @Around("@annotation(RequiresWorkspaceRole)")
    public Object checkWorkspaceRole(ProceedingJoinPoint joinPoint) throws Throwable{
        MethodSignature sig = (MethodSignature) joinPoint.getSignature(); //read annotation value
        RequiresWorkspaceRole annotation = sig.getMethod().getAnnotation(RequiresWorkspaceRole.class);
        WorkspaceRole requiredRole = annotation.value();

        UUID workspaceId = findWorkspaceId(joinPoint.getArgs());
        if (workspaceId == null){
            log.warn("@RequiresWorkspaceRole on {} but no UUID argument found - skipping check", sig.getMethod().getName());
            return joinPoint.proceed();
        }

        var currentUser = userService.resolveCurrentUser();
        var member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, currentUser.getId()).orElseThrow(() -> new AccessDeniedException("You are not a member of this workspace"));

        if(member.getRole().ordinal() > requiredRole.ordinal()){
            throw new AccessDeniedException("This action requires workspace role: " + requiredRole + " (your role: " + member.getRole() + ")");
        }

        return joinPoint.proceed();
    }

    private UUID findWorkspaceId(Object[] args){
        for(Object arg : args){
            if(arg instanceof UUID uuid) return uuid; // very first UUID argument is workspaceId
        }
        return null;
    }
}
