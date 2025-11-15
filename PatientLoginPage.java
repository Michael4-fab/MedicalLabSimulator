import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.sql.*;

public class PatientLoginPage {

    public void show(Stage stage) {
        // ===== TITLE =====
        Label title = new Label("PATIENT LOGIN");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.GOLD);

        // ===== INPUT FIELDS =====
        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");

        Label msg = new Label();
        msg.setTextFill(Color.RED);

        // ===== BUTTONS =====
        Button login = new Button("Login");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold;");

        Button back = new Button("Back");
        back.setStyle("-fx-background-color: transparent; -fx-text-fill: gold;");
        back.setOnAction(e -> new MainMenu().start(stage));

        // ===== LAYOUT =====
        VBox root = new VBox(12, title, emailField, passField, login, msg, back);
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: black;");

        // ===== LOGIN LOGIC =====
        login.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passField.getText().trim();

            // Check empty fields
            if (email.isEmpty() || password.isEmpty()) {
                msg.setText("Please fill all fields.");
                return;
            }

            try (Connection conn = sqlconnector.connect()) {
                if (conn == null) {
                    msg.setText("Database connection failed.");
                    return;
                }

                // Query database for patient credentials
                String sql = "SELECT * FROM patients WHERE email=? AND password=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, email);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                // If found → move to dashboard
                if (rs.next()) {
                    String pid = rs.getString("patient_id");

                    // ✅ FIX: Pass patientId correctly to the constructor
                    new PatientDashboard(pid).show(stage);

                } else {
                    msg.setText("Invalid credentials.");
                }

            } catch (SQLException ex) {
                msg.setText("Error: " + ex.getMessage());
            }
        });

        // ===== SCENE SETUP =====
        stage.setScene(new Scene(root, 520, 420));
        stage.setTitle("Patient Login");
        stage.show();
    }
}
