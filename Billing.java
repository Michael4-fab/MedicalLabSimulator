/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Enemona Michael Enyo-ojo
 */
public class Billing {
    
    private final Appointment appointment;
    private final double amount;
    private boolean paid;
    
    public Billing(Appointment appointment, double amount){
        this.appointment = appointment;
        this.amount = amount;
        this.paid = false;
    }

   // Billing(String bpid, double amt, String bid) {
   // }
    
    public void markPaid(){ this.paid = true; }
    public boolean isPaid() { return paid; }
    public double getAmount() { return amount; }
    public Appointment getAppointment() { return appointment; }
    
    public void printStatus() {
        System.out.println("Billing Status: " + (paid ? "Paid" : "Not Paid"));
    }

}
    

