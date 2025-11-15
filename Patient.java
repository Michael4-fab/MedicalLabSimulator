/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author BSIIT
 */
public class Patient {
   
    String FullName;
    String password;
    String email;
     String PatientID;
     String medicalHistory;

    public Patient(String FullName, String password, String history,String PatientID) {
        this.FullName = FullName;
        this.password = password;
        this.medicalHistory = history;
         this.PatientID = PatientID;
    }

    Patient(String pname, int age, String pid) {
       
    }

    Patient(String pname, int age, String pid, String email) {
        
    }

    Patient(String pn, int pa, String pid, String pe, String ep) {
    }
    public String getName() {return FullName; }
    public String getMedicalHistory() { return medicalHistory; }
    
     public void addMedicalNote(String note) {
        medicalHistory += "; " + note;
    }
    
}
