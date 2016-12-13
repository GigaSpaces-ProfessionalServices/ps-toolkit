package com.gigaspaces.gigapro.syncendpoint;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceIndex;

import java.util.EnumSet;

@SpaceClass
public class RouteSequenceString{

    private String spaceId;
    private FlightStation station1;
    private FlightStation station2;
    private FlightStation station3;

    public RouteSequenceString() {
    }

    @SpaceIndex
    public FlightStation getStation1() {
        return station1;
    }

    public void setStation1(FlightStation station1) {
        this.station1 = station1;
    }

    @SpaceIndex
    public FlightStation getStation2() {
        return station2;
    }

    public void setStation2(FlightStation station2) {
        this.station2 = station2;
    }

    public FlightStation getStation3() {
        return station3;
    }

    public void setStation3(FlightStation station3) {
        this.station3 = station3;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

}