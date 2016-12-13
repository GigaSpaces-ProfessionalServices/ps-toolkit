package com.gigaspaces.gigapro.convert.property;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;

/**
 * Created by IntelliJ IDEA.
 * User: jason
 * Date: 12/7/16
 * Time: 11:59 AM
 * Provides...
 */
@SpaceClass
public class Parent {

    private String id;
    protected String parentProtected;

    public Parent() {
    }

    @SpaceId(autoGenerate=true)
    private String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getParentProtected() {
        return parentProtected;
    }

    public void setParentProtected(String parentProtected) {
        this.parentProtected = parentProtected;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Parent{");
        sb.append("id='").append(id).append('\'');
        sb.append(", parentProtected='").append(parentProtected).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
