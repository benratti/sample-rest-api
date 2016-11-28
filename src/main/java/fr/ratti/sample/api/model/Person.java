package fr.ratti.sample.api.model;

import fr.ratti.sample.api.annotation.Mandatory;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by bratti on 16/08/2016.
 */
@XmlRootElement
public class Person {

    private String firstname;
    private String lastname;
    private String id;

    @Mandatory
    private String mail;


    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}
