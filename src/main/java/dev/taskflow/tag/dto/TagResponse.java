package dev.taskflow.tag.dto;

import dev.taskflow.tag.Tag;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TagResponse {

    private final UUID id;
    private final UUID workspaceId;
    private final String name;
    private final String color;

    private TagResponse(Tag tag){
        this.id = tag.getId();
        this.workspaceId = tag.getWorkspace().getId();
        this.name = tag.getName();
        this.color = getColor();
    }

    public static TagResponse from(Tag tag){
        return new TagResponse(tag);
    }
}
