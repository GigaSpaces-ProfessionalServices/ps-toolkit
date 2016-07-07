package com.gigaspaces.gigapro.model;

import com.gigaspaces.gigapro.convert.PropertyKey;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class ClusterReplicationPolicy {
    
    @PropertyKey("replication_mode")
    private String replicationMode;
    @PropertyKey("repl_policy_type")
    private String policyType;
    @PropertyKey("repl_find_timeout")
    private long replFindTimeout;
    @PropertyKey("repl_full_take")
    private boolean replFullTake;
    @PropertyKey("replicate_notify_templates")
    private boolean replNotifyTemplates;
    @PropertyKey("trigger_notify_templates")
    private boolean triggerNotifyTemplates;
    @PropertyKey("repl_chunk_size")
    private int replChunkSize;
    @PropertyKey("repl_interval_millis")
    private long replIntervalMillis;
    @PropertyKey("repl_interval_opers")
    private int replIntervalOpers;
    @PropertyKey("async_channel_shutdown_timeout")
    private long asyncChannelShutdownTimeout;
    @PropertyKey("on_conflicting_packets")
    private String onConflictingPackates;
    @PropertyKey("throttle_when_inactive")
    private boolean throttleWhenInactive;
    @PropertyKey("max_throttle_tp_when_inactive")
    private int maxThrottleTpWhenInactive;
    @PropertyKey("min_throttle_tp_when_active")
    private int minThrottleTpWhenInactive;
    @PropertyKey("multiple_opers_chunk_size")
    private int multipleOpersChunkSize;
    @PropertyKey("target_consume_timeout")
    private long targetConsumeTimeout;
    @PropertyKey("mirror_name")
    private String mirrorName;
    @PropertyKey("mirror_url")
    private String mirrorUrl;
    @PropertyKey("mirror_bulk_size")
    private int mirrorBulkSize;
    @PropertyKey("mirror_interval_millis")
    private long mirrorIntervalMillis;
    @PropertyKey("mirror_interval_opers")
    private int mirrorIntervalOpers;
    @PropertyKey("on_redo_log_capacity_exceeded")
    private String onRedoLogCapacityExceeded;
    @PropertyKey("redo_log_capacity")
    private Long redoLogCapacity;
    
    public String getReplicationMode() {
        return replicationMode;
    }

    public void setReplicationMode(String replicationMode) {
        this.replicationMode = replicationMode;
    }

    public String getPolicyType() {
        return policyType;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    public long getReplFindTimeout() {
        return replFindTimeout;
    }

    public void setReplFindTimeout(long replFindTimeout) {
        this.replFindTimeout = replFindTimeout;
    }

    public boolean getReplFullTake() {
        return replFullTake;
    }

    public void setReplFullTake(boolean replFullTake) {
        this.replFullTake = replFullTake;
    }

    public boolean isReplNotifyTemplate() {
        return replNotifyTemplates;
    }

    public void setReplNotifyTemplate(boolean replNotifyTemplate) {
        this.replNotifyTemplates = replNotifyTemplate;
    }

    public boolean isTriggerNotifyTemplate() {
        return triggerNotifyTemplates;
    }

    public void setTriggerNotifyTemplate(boolean triggerNotifyTemplate) {
        this.triggerNotifyTemplates = triggerNotifyTemplate;
    }

    public int getReplChunkSize() {
        return replChunkSize;
    }

    public void setReplChunkSize(int replChunkSize) {
        this.replChunkSize = replChunkSize;
    }

    public long getReplIntervalMillis() {
        return replIntervalMillis;
    }

    public void setReplIntervalMillis(long replIntervalMillis) {
        this.replIntervalMillis = replIntervalMillis;
    }

    public int getReplIntervalOpers() {
        return replIntervalOpers;
    }

    public void setReplIntervalOpers(int replIntervalOpers) {
        this.replIntervalOpers = replIntervalOpers;
    }

    public long getAsyncChannelShutdownTimeout() {
        return asyncChannelShutdownTimeout;
    }

    public void setAsyncChannelShutdownTimeout(long asyncChannelShutdownTimeout) {
        this.asyncChannelShutdownTimeout = asyncChannelShutdownTimeout;
    }

    public String getOnConflictingPackates() {
        return onConflictingPackates;
    }

    public void setOnConflictingPackates(String onConflictingPackates) {
        this.onConflictingPackates = onConflictingPackates;
    }

    public boolean isThrottleWhenInactive() {
        return throttleWhenInactive;
    }

    public void setThrottleWhenInactive(boolean throttleWhenInactive) {
        this.throttleWhenInactive = throttleWhenInactive;
    }

    public int getMaxThrottleTpWhenInactive() {
        return maxThrottleTpWhenInactive;
    }

    public void setMaxThrottleTpWhenInactive(int maxThrottleTpWhenInactive) {
        this.maxThrottleTpWhenInactive = maxThrottleTpWhenInactive;
    }

    public int getMinThrottleTpWhenInactive() {
        return minThrottleTpWhenInactive;
    }

    public void setMinThrottleTpWhenInactive(int minThrottleTpWhenInactive) {
        this.minThrottleTpWhenInactive = minThrottleTpWhenInactive;
    }

    public int getMultipleOpersChunkSize() {
        return multipleOpersChunkSize;
    }

    public void setMultipleOpersChunkSize(int multipleOpersChunkSize) {
        this.multipleOpersChunkSize = multipleOpersChunkSize;
    }

    public long getTargetConsumeTimeout() {
        return targetConsumeTimeout;
    }

    public void setTargetConsumeTimeout(long targetConsumeTimeout) {
        this.targetConsumeTimeout = targetConsumeTimeout;
    }

    public boolean isReplNotifyTemplates() {
        return replNotifyTemplates;
    }

    public void setReplNotifyTemplates(boolean replNotifyTemplates) {
        this.replNotifyTemplates = replNotifyTemplates;
    }

    public boolean isTriggerNotifyTemplates() {
        return triggerNotifyTemplates;
    }

    public void setTriggerNotifyTemplates(boolean triggerNotifyTemplates) {
        this.triggerNotifyTemplates = triggerNotifyTemplates;
    }

    public String getMirrorName() {
        return mirrorName;
    }

    public void setMirrorName(String mirrorName) {
        this.mirrorName = mirrorName;
    }

    public long getMirrorIntervalMillis() {
        return mirrorIntervalMillis;
    }

    public void setMirrorIntervalMillis(long mirrorIntervalMillis) {
        this.mirrorIntervalMillis = mirrorIntervalMillis;
    }

    public int getMirrorIntervalOpers() {
        return mirrorIntervalOpers;
    }

    public void setMirrorIntervalOpers(int mirrorIntervalOpers) {
        this.mirrorIntervalOpers = mirrorIntervalOpers;
    }

    public String getOnRedoLogCapacityExceeded() {
        return onRedoLogCapacityExceeded;
    }

    public void setOnRedoLogCapacityExceeded(String onRedoLogCapacityExceeded) {
        this.onRedoLogCapacityExceeded = onRedoLogCapacityExceeded;
    }

    public Long getRedoLogCapacity() {
        return redoLogCapacity;
    }

    public void setRedoLogCapacity(Long redoLogCapacity) {
        this.redoLogCapacity = redoLogCapacity;
    }

    public int getMirrorBulkSize() {
        return mirrorBulkSize;
    }

    public void setMirrorBulkSize(int mirrorBulkSize) {
        this.mirrorBulkSize = mirrorBulkSize;
    }

    public String getMirrorUrl() {
        return mirrorUrl;
    }

    public void setMirrorUrl(String mirrorUrl) {
        this.mirrorUrl = mirrorUrl;
    }
}
