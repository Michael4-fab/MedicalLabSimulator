import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.sql.*;

/**
 * BillingPage class
 * -----------------
 * Displays a patient's billing records from the database.
 * Allows navigation back to the patient dashboard.
 */
public class BillingPage {

    /**
     * Opens the billing page for the given patient.
     *
     * @param stage     The current JavaFX stage
     * @param patientId The ID of the logged-in patient
     */
    public void open(Stage stage, String patientId) {

        // ====== PAGE TITLE ======
        Label title = new Label("Billing Information");
        title.setStyle("-fx-text-fill: gold; -fx-font-size: 22px; -fx-font-weight: bold;");

        // ====== TEXT AREA TO DISPLAY BILLS ======
        TextArea billsArea = new TextArea();
        billsArea.setEditable(false);
        billsArea.setPrefHeight(250);
        billsArea.setStyle("-fx-control-inner-background: black; -fx-text-fill: white;");

        // ====== BACK BUTTON ======
        Button backBtn = new Button("Back");
        backBtn.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> {
            // Return to the patient dashboard
            new PatientDashboard(patientId).show(stage);
        });

        // ====== MAIN LAYOUT ======
        VBox layout = new VBox(15, title, billsArea, backBtn);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: black;");

        // ====== LOAD BILLING DATA FROM DATABASE ======
        StringBuilder bills = new StringBuilder();

        try (Connection conn = sqlconnector.connect()) {

            if (conn == null) {
                bills.append("❌ Database connection failed.");
            } else {
                String sql = "SELECT amount, description FROM billing WHERE patient_id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, patientId);
                ResultSet rs = ps.executeQuery();

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    bills.append("💰 Amount: ₦").append(rs.getString("amount"))
                         .append("\n📋 Description: ").append(rs.getString("description"))
                         .append("\n------------------------------\n");
                }

                if (!found) {
                    bills.append("No billing records found for this patient.");
                }
            }

        } catch (SQLException ex) {
            bills.append("❌ Error fetching billing data: ").append(ex.getMessage());
        }

        // Set results in the text area
        billsArea.setText(bills.toString());

        // ====== SCENE SETUP ======
        Scene scene = new Scene(layout, 550, 450, Color.BLACK);
        stage.setScene(scene);
        stage.setTitle("Patient Billing");
        stage.show();
    }
}
