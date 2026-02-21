package ca.mcgill.ecse321.ecse321project03.model;

import jakarta.persistence.Entity;

@Entity
public class Employee extends Role {

    //------------------------
    // CONSTRUCTOR
    //------------------------

    public Employee(Account aAccount) {
        super(aAccount);
    }

    public Employee() {
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




