package com.gigaspaces.gigapro;

import com.gigaspaces.annotation.pojo.SpaceId;

/**
 * @author Denys_Novikov
 * Date: 28.03.2018
 */
public class GridStateEvent {

    private String id;
    private boolean processed;
    private boolean balanced;

    @SpaceId(autoGenerate = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public boolean isBalanced() {
        return balanced;
    }

    public void setBalanced(boolean balanced) {
        this.balanced = balanced;
    }
}
