package com.nutriguide.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * ActivityLog: Represents an audit trail or system event record corresponding to the 'activity_log' relation.
 */
public class ActivityLog {

    private Integer logId;
    private Integer userId;
    private String actionTaken;
    private LocalDateTime logTime;

    public ActivityLog() {
    }

    public ActivityLog(Integer userId, String actionTaken) {
        this.userId = userId;
        this.actionTaken = actionTaken;
        this.logTime = LocalDateTime.now();
    }

    public ActivityLog(Integer logId, Integer userId, String actionTaken, LocalDateTime logTime) {
        this.logId = logId;
        this.userId = userId;
        this.actionTaken = actionTaken;
        this.logTime = logTime;
    }

    public ActivityLog(ActivityLog other) {
        if (other != null) {
            this.logId = other.logId;
            this.userId = other.userId;
            this.actionTaken = other.actionTaken;
            this.logTime = other.logTime;
        }
    }

    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public LocalDateTime getLogTime() {
        return logTime;
    }

    public void setLogTime(LocalDateTime logTime) {
        this.logTime = logTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityLog that = (ActivityLog) o;
        return Objects.equals(logId, that.logId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logId);
    }

    @Override
    public String toString() {
        return "ActivityLog{" +
                "logId=" + logId +
                ", userId=" + userId +
                ", actionTaken='" + actionTaken + '\'' +
                ", logTime=" + logTime +
                '}';
    }
}
