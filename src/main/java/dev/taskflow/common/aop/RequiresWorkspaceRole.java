package dev.taskflow.common.aop;

import dev.taskflow.workspace.WorkspaceRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresWorkspaceRole {

    WorkspaceRole value();

}
