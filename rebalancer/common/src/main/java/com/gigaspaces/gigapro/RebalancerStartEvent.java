package com.gigaspaces.gigapro;

import com.gigaspaces.annotation.pojo.SpaceId;

/**
 * @author Denys_Novikov
 * Date: 23.02.2018
 */
public class RebalancerStartEvent {

    private String id;

    @SpaceId(autoGenerate = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}
