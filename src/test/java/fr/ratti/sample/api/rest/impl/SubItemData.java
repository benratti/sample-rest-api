package fr.ratti.sample.api.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by bratti on 22/08/2016.
 */
@XmlRootElement
public class SubItemData {

    private String subItem1;
    private String subItem2;

    public String getSubItem1() {
        return subItem1;
    }

    public void setSubItem1(String subItem1) {
        this.subItem1 = subItem1;
    }

    public String getSubItem2() {
        return subItem2;
    }

    public void setSubItem2(String subItem2) {
        this.subItem2 = subItem2;
    }
}
