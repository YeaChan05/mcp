package com.yechan.client;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchResultEvent {
    private EventType type;
    private String data;
    private int progress;

    public enum EventType {
        STATUS,
        RESULT,
        ERROR
    }
}
