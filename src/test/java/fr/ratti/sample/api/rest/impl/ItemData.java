package fr.ratti.sample.api.rest.impl;

import javax.xml.bind.annotation.*;

/**
 * Created by bratti on 22/08/2016.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemData {

    private String item1;
    private String item2;
    private String item3;

    @XmlTransient
    private SubItemData subItemData;

    public String getItem1() {
        return item1;
    }

    public void setItem1(String item1) {
        this.item1 = item1;
    }

    public String getItem2() {
        return item2;
    }

    public void setItem2(String item2) {
        this.item2 = item2;
    }

    public String getItem3() {
        return item3;
    }

    public void setItem3(String item3) {
        this.item3 = item3;
    }

    public SubItemData getSubItemData() {
        return subItemData;
    }

    public void setSubItemData(SubItemData subItemData) {
        this.subItemData = subItemData;
    }
}
