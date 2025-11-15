/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
  import java.time.LocalDateTime;
/**
 *
 * @author BSIIT
 */
public class Appointment {
 
    private Patient patient;
    private Practitioner practitioner;
    private LocalDateTime dateTime;
    private String status;
    
    public Appointment (Patient p, Practitioner pr, LocalDateTime dt){
        this.patient = p;
        this.practitioner = pr;
        this.dateTime = dt;
        this.status = "SCHEDULED";
        
    }

    Appointment(String pID, String dID, LocalDateTime dt) {
       
    }

    public void cancel() { this.status = "CANCELLED"; }
    public String getStatus() { return status; }
    public Patient getPatient() { return patient; }
    public Practitioner getPractitioner() { return practitioner; }
    public LocalDateTime getDateTime() { return dateTime; }
    
    public void reschedule(LocalDateTime newDateTime) {
        this.dateTime = newDateTime;
        this.status = "RESCHEDULED";
    }

    Object getPatientId() {
        return null;
        
    }
}
        
    

    
    

