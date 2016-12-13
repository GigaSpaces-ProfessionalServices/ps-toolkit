package com.gigaspaces.gigapro.convert.property;

import com.gigaspaces.annotation.pojo.SpaceClass;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jason
 * Date: 12/7/16
 * Time: 11:59 AM
 * Provides...
 */
@SpaceClass
public class Child extends Parent {

    private String childProperty;

    public Child() {
    }

    private List<String> childThings;

    public List<String> getChildThings() {
        return childThings;
    }

    public void setChildThings(List<String> childThings) {
        this.childThings = childThings;
    }

    public String getChildProperty() {
        return childProperty;
    }

    public void setChildProperty(String childProperty) {
        this.childProperty = childProperty;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Child{");
        sb.append("childProperty='").append(childProperty).append('\'');
        sb.append(", childThings=").append(childThings);
        sb.append('}');
        sb.append(super.toString());
        return sb.toString();
    }
}
