package fr.ratti.sample.api.rest.impl;

import fr.ratti.sample.api.annotation.Mandatory;
import fr.ratti.sample.api.annotation.Seachable;
import org.codehaus.jackson.annotate.JsonIgnore;

import javax.xml.bind.annotation.*;

/**
 * Created by bratti on 20/08/2016.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SampleData {

    @XmlTransient
    @JsonIgnore
    @Seachable("name")
    private Object optionalField;

    @XmlTransient
    @JsonIgnore
    @Mandatory
    private Object mandatoryField;

    @Seachable
    private String id;

    @XmlTransient
    private ItemData itemData;

    public ItemData getItemData() {
        return itemData;
    }

    public void setItemData(ItemData itemData) {
        this.itemData = itemData;
    }

    public Object getOptionalField() {
        return optionalField;
    }

    public void setOptionalField(Object optionalField) {
        this.optionalField = optionalField;
    }

    public Object getMandatoryField() {
        return mandatoryField;
    }

    public void setMandatoryField(Object mandatoryField) {
        this.mandatoryField = mandatoryField;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }


    @Override
    public String toString() {
        return "SampleData{" +
                "optionalField=" + optionalField +
                ", mandatoryField=" + mandatoryField +
                ", id='" + id + '\'' +
                ", itemData=" + itemData +
                '}';
    }




}
