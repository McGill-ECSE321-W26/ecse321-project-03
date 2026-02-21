package ca.mcgill.ecse321.ecse321project03.model;

import jakarta.persistence.Entity;

@Entity
public class Manager extends Role {

    //------------------------
    // CONSTRUCTOR
    //------------------------

    public Manager(Account aAccount) {
        super(aAccount);
    }

    public Manager() {
        super();
    }

    //------------------------
    // INTERFACE
    //------------------------

    // Delete method
    public void delete() {
        super.delete();
    }

}


