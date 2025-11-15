/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author BSIIT
 */
public class Practitioner {

    String Specialty;
    String FullName;
    String Email;
    String Password;
    String PractitionerID;

    public Practitioner(String Specialty, String FullName, String Password, String Email, String PractitionerID) {
        this.Specialty = Specialty;
        this.FullName = FullName;
        this.Email = Email;
        this.Password = Password;
        this.PractitionerID = PractitionerID;
    }

    Practitioner(String dname, String spec, String did) {
        
    }

    public String getSpecialty() {
        return Specialty;
    }
    public void updateSpecialty(String newSpecialty) {
        this.Specialty = newSpecialty;
    }
    
}
    

