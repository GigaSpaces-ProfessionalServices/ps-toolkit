package com.gigaspaces.gigapro;

/**
 * @author Svitlana_Pogrebna
 *
 */
public enum LrmiThreadCount implements Statistic {
    SELECTOR_READ,
    SELECTOR_WRITE,
    ASYNC_SELECTOR,
    MONITORING,
    LIVENESS,
    LEASE_RENEWAL_MANAGER,
    LEASE_RENEWAL_MANAGER_TASK,
    BACKGROUND_FIFO_THREAD,
    PROCESSOR_POOL,
    PENDING_ANSWERS,
    BATCH_NOTIFIER,
    LOOKUP_DISCOVERY,
    LEASE_MANAGER_REAPER,
    CONNECTION
}
