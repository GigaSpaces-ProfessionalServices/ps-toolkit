package com.gigaspaces.gigapro;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;

/**
 * Created by IntelliJ IDEA.
 * User: jason
 * Date: 7/18/16
 * Time: 3:51 PM
 * Provides...
 */
@SpaceClass
public class Sku {

    private String spaceId;
    private String skuNumber;
    private Integer inventoryCount;


    public Sku(){}

    public int getInventoryCount() {
        return inventoryCount;
    }

    public void setInventoryCount(Integer inventoryCount) {
        this.inventoryCount = inventoryCount;
    }

    public String getSkuNumber() {
        return skuNumber;
    }

    public void setSkuNumber(String skuNumber) {
        this.skuNumber = skuNumber;
    }

    @SpaceId(autoGenerate = false)
    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sku sku = (Sku) o;

        if (spaceId != null ? !spaceId.equals(sku.spaceId) : sku.spaceId != null) return false;
        return skuNumber != null ? skuNumber.equals(sku.skuNumber) : sku.skuNumber == null;

    }

    @Override
    public int hashCode() {
        int result = spaceId != null ? spaceId.hashCode() : 0;
        result = 31 * result + (skuNumber != null ? skuNumber.hashCode() : 0);
        return result;
    }
}
