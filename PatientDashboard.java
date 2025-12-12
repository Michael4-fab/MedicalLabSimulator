
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class PatientDashboard {

    private final String patientId;  // Logged-in patient’s ID

    public PatientDashboard(String patientId) {
        this.patientId = patientId;
    }

    // ===============================
    // MAIN PATIENT DASHBOARD
    // ===============================
    public void show(Stage stage) {

        // ===== HEADER =====
        Label title = new Label("PATIENT DASHBOARD");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.GOLD);

        Label info = new Label("Logged in as: " + patientId);
        info.setTextFill(Color.WHITE);

        // ===== BUTTONS =====
        Button viewAppointments = new Button("View My Appointments");
        Button bookAppointment = new Button("Book Appointment");
        Button viewBilling = new Button("View Billing");
        Button back = new Button("Back to Main Menu");
        Button logout = new Button("Logout");

        // Style main action buttons
        for (Button b : new Button[]{viewAppointments, bookAppointment, viewBilling}) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold;");
        }

        // Style navigation buttons
        back.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold;");
        logout.setStyle("-fx-background-color: transparent; -fx-text-fill: gold;");

        Label feedback = new Label();
        feedback.setTextFill(Color.WHITE);

        // ===============================
        // VIEW APPOINTMENTS (FANCY TABLE)
        // ===============================
        viewAppointments.setOnAction(e -> {

            Stage apptStage = new Stage();
            apptStage.setTitle("My Appointments");

            TableView<AppointmentRow> table = new TableView<>();

            TableColumn<AppointmentRow, String> colId = new TableColumn<>("Appointment ID");
            colId.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
            colId.setPrefWidth(130);

            TableColumn<AppointmentRow, String> colPract = new TableColumn<>("Practitioner ID");
            colPract.setCellValueFactory(new PropertyValueFactory<>("practitionerId"));
            colPract.setPrefWidth(150);

            TableColumn<AppointmentRow, String> colDate = new TableColumn<>("Date & Time");
            colDate.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
            colDate.setPrefWidth(200);

            TableColumn<AppointmentRow, String> colStatus = new TableColumn<>("Status");
            colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            colStatus.setPrefWidth(120);

            table.getColumns().addAll(colId, colPract, colDate, colStatus);

            ObservableList<AppointmentRow> rows = FXCollections.observableArrayList();

            try (Connection conn = sqlconnector.connect()) {
                String sql = "SELECT * FROM appointment WHERE patient_id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, patientId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    rows.add(new AppointmentRow(
                            rs.getString("appointment_id"),
                            rs.getString("practitioner_id"),
                            rs.getTimestamp("date_time").toString(),
                            rs.getString("status")
                    ));
                }

                if (rows.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("No Appointments");
                    alert.setContentText("You have no booked appointments.");
                    alert.show();
                    return;
                }

            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Error Loading Appointments");
                alert.setContentText(ex.getMessage());
                alert.show();
                return;
            }

            table.setItems(rows);

            Button backBtn = new Button("Back");
            backBtn.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold;");
            backBtn.setOnAction(ev -> apptStage.close());

            Label heading = new Label("My Appointments");
            heading.setTextFill(Color.GOLD);
            heading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            VBox box = new VBox(15, heading, table, backBtn);
            box.setPadding(new Insets(15));
            box.setStyle("-fx-background-color: black;");

            apptStage.setScene(new Scene(box, 650, 450));
            apptStage.show();
        });

        // ===============================
        // BOOK APPOINTMENT
        // ===============================
        bookAppointment.setOnAction(e -> {
            AppointmentManager manager = new AppointmentManager();
            manager.open(stage, patientId);
        });

        // ===============================
        // VIEW BILLING
        // ===============================
        viewBilling.setOnAction(e -> {
            StringBuilder sb = new StringBuilder();
            try (Connection conn = sqlconnector.connect()) {
                String sql = "SELECT * FROM billing WHERE patient_id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, patientId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    sb.append("Amount: ₦").append(rs.getDouble("amount"))
                            .append("\nDescription: ").append(rs.getString("description"))
                            .append("\nDate: ").append(rs.getTimestamp("date_created"))
                            .append("\n-----------------------------\n");
                }

                if (sb.length() == 0) {
                    sb.append("No billing records found.");
                }
            } catch (Exception ex) {
                sb.append("Error: " + ex.getMessage());
            }
            showTextWindow("Billing Records", sb.toString());
        });

        // ===============================
        // BACK BUTTON
        // ===============================
        back.setOnAction(e -> new MainMenu().start(stage));

        // ===============================
        // LOGOUT
        // ===============================
        logout.setOnAction(e -> {
            MainMenu menu = new MainMenu();
            menu.start(stage);
        });

        // ===============================
        // LAYOUT
        // ===============================
        VBox root = new VBox(12, title, info, viewAppointments, bookAppointment, viewBilling, feedback, back, logout);
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: black;");

        stage.setScene(new Scene(root, 600, 500));
        stage.setTitle("Patient Dashboard");
        stage.show();
    }

    // ===============================
    // POPUP TEXT WINDOW
    // ===============================
    private void showTextWindow(String title, String content) {
        Stage popup = new Stage();
        TextArea area = new TextArea(content);
        area.setEditable(false);

        Label heading = new Label(title);
        heading.setTextFill(Color.GOLD);
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        VBox layout = new VBox(10, heading, area);
        layout.setPadding(new Insets(10));
        layout.setStyle("-fx-background-color: black;");

        popup.setScene(new Scene(layout, 600, 400));
        popup.setTitle(title);
        popup.show();
    }

    // ===============================
    // TABLE ROW MODEL CLASS
    // ===============================
    public static class AppointmentRow {

        private final String appointmentId;
        private final String practitionerId;
        private final String dateTime;
        private final String status;

        public AppointmentRow(String appointmentId, String practitionerId, String dateTime, String status) {
            this.appointmentId = appointmentId;
            this.practitionerId = practitionerId;
            this.dateTime = dateTime;
            this.status = status;
        }

        public String getAppointmentId() {
            return appointmentId;
        }

        public String getPractitionerId() {
            return practitionerId;
        }

        public String getDateTime() {
            return dateTime;
        }

        public String getStatus() {
            return status;
        }
    }
}
