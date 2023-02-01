package com.github.kardzhaliyski.container.events;

public class ApplicationEvent {
    Object source;
    private final long timestamp;
    public ApplicationEvent(Object source) {
        this.source = source;
        this.timestamp = System.currentTimeMillis();
    }

    public Object getSource() {
        return source;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
