
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.*;

public class PractitionerLoginPage {

    public void show(Stage stage) {
        // ===== TITLE =====
        Label title = new Label("PRACTITIONER LOGIN");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.GOLD);

        // ===== INPUT FIELDS =====
        TextField idField = new TextField();
        idField.setPromptText("Practitioner ID");

        // ===============================
// NEW: PASSWORD EYE-TOGGLE FIELD
// ===============================
        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");

        TextField visiblePass = new TextField();
        visiblePass.managedProperty().bind(visiblePass.visibleProperty());
        visiblePass.textProperty().bindBidirectional(passField.textProperty());

// Remove visibility bindings to avoid error
        visiblePass.setVisible(false);
        passField.setVisible(true);

        Button eye = new Button("👁");
        eye.setStyle("-fx-background-color: transparent; -fx-text-fill: gold;");
        eye.setOnAction(e -> {
            boolean showing = visiblePass.isVisible();
            visiblePass.setVisible(!showing);
            passField.setVisible(showing);
        });

        StackPane passPane = new StackPane(passField, visiblePass, eye);
        StackPane.setAlignment(eye, Pos.CENTER_RIGHT);

        // ===== MESSAGE LABEL =====
        Label msg = new Label();
        msg.setTextFill(Color.RED);

        // ===== BUTTONS =====
        Button login = new Button("Login");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold;");

        Button back = new Button("Back");
        back.setStyle("-fx-background-color: transparent; -fx-text-fill: gold;");
        back.setOnAction(e -> new MainMenu().start(stage));

        // ===== FORGOT PASSWORD LINK =====
        Hyperlink forgotPass = new Hyperlink("Forgot Password?");
        forgotPass.setTextFill(Color.GOLD);
        forgotPass.setOnAction(e -> openForgotPassword(stage));

        // ===== LAYOUT =====
        VBox root = new VBox(12,
                title,
                idField,
                passPane, // ⬅ replaced previous HBox(passField, visiblePass)
                login,
                msg,
                forgotPass,
                back
        );
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: black;");

        // ===== LOGIN ACTION =====
        login.setOnAction(e -> {
            String id = idField.getText().trim();
            String password = passField.getText().trim();

            if (id.isEmpty() || password.isEmpty()) {
                msg.setText("Please fill in all fields.");
                return;
            }

            try (Connection conn = sqlconnector.connect()) {
                if (conn == null) {
                    msg.setText("Database connection failed.");
                    return;
                }

                // ✅ Check practitioner ID + password
                String sql = "SELECT * FROM practitioner WHERE practitioner_id=? AND password=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, id);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    msg.setTextFill(Color.LIGHTGREEN);
                    msg.setText("Login successful!");
                    new PractitionerDashboard(id).show(stage); // ⬅ Pass practitioner ID
                } else {
                    msg.setText("Invalid ID or Password.");
                }

            } catch (SQLException ex) {
                msg.setText("Error: " + ex.getMessage());
            }
        });

        // ===== SCENE SETUP =====
        stage.setScene(new Scene(root, 520, 420));
        stage.setTitle("Practitioner Login");
        stage.show();
    }

    // ============================
    // FORGOT PASSWORD FEATURE
    // ============================
    private void openForgotPassword(Stage stage) {
        Stage forgotStage = new Stage();
        forgotStage.setTitle("Reset Password");

        Label info = new Label("Enter your Practitioner ID to reset your password:");
        info.setTextFill(Color.WHITE);

        TextField idField = new TextField();
        idField.setPromptText("Practitioner ID");

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New Password");

        Label msg = new Label();
        msg.setTextFill(Color.RED);

        Button resetBtn = new Button("Reset Password");
        resetBtn.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold;");

        // RESET PASSWORD LOGIC
        resetBtn.setOnAction(e -> {
            String id = idField.getText().trim();
            String np = newPass.getText().trim();

            if (id.isEmpty() || np.isEmpty()) {
                msg.setText("Please fill in all fields.");
                return;
            }

            try (Connection conn = sqlconnector.connect()) {
                if (conn == null) {
                    msg.setText("Database connection failed.");
                    return;
                }

                // Check if ID exists
                String check = "SELECT email FROM practitioner WHERE practitioner_id=?";
                PreparedStatement ps = conn.prepareStatement(check);
                ps.setString(1, id);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    // Update password
                    String email = rs.getString("email");
                    String update = "UPDATE practitioner SET password=? WHERE practitioner_id=?";
                    PreparedStatement ups = conn.prepareStatement(update);
                    ups.setString(1, np);
                    ups.setString(2, id);
                    ups.executeUpdate();

                    // Send email confirmation
                    EmailSender.sendEmail(email, "Password Reset", "Your new password is: " + np);

                    msg.setTextFill(Color.GREEN);
                    msg.setText("Password reset successful! Check your email.");
                } else {
                    msg.setText("Practitioner ID not found.");
                }

            } catch (SQLException ex) {
                msg.setText("Error: " + ex.getMessage());
            }
        });

        VBox layout = new VBox(12, info, idField, newPass, resetBtn, msg);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: black;");

        forgotStage.setScene(new Scene(layout, 400, 300));
        forgotStage.show();
    }
}
