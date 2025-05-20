package me.sailex.secondbrain.llm.roles;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ChatRole implements BasicRole {
    @JsonProperty("system")
    SYSTEM("system"),

    @JsonProperty("user")
    USER("user"),

    @JsonProperty("assistant")
    ASSISTANT("assistant"),

    @JsonProperty("developer")
    DEVELOPER("developer");

    @Getter
    public final String roleName;
}