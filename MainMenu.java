import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.sql.*;

public class MainMenu extends Application {

    public void start(Stage stage) {

        // ===== HEADER =====
        Label title = new Label("FAB'S MEDICAL LAB");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.GOLD);

        // ===== BUTTONS =====
        Button patientLogin = new Button("Patient Login");
        Button registerPatient = new Button("Register New Patient");
        Button practitionerLogin = new Button("Practitioner Login");

        for (Button b : new Button[]{patientLogin, registerPatient, practitionerLogin}) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 14px;");
        }

        Label feedback = new Label();
        feedback.setTextFill(Color.WHITE);

        // ===== ACTIONS =====
        patientLogin.setOnAction(e -> showPatientLogin(stage, feedback));
        practitionerLogin.setOnAction(e -> showPractitionerLogin(stage, feedback));
        registerPatient.setOnAction(e -> showRegisterPatientDialog(feedback));

        // ===== MAIN LAYOUT =====
        VBox layout = new VBox(15, title, patientLogin, registerPatient, practitionerLogin, feedback);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: black;");

        stage.setScene(new Scene(layout, 500, 420));
        stage.setTitle("Fab's Medical Lab");
        stage.show();
    }

    // =====================================================
    //  PATIENT LOGIN + FORGOT PASSWORD FEATURE
    // =====================================================
    private void showPatientLogin(Stage stage, Label feedback) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Patient Login");
        dialog.getDialogPane().setStyle("-fx-background-color: black;");

        TextField patientIdField = new TextField();
        patientIdField.setPromptText("Enter Patient ID");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");

        Hyperlink forgotPasswordLink = new Hyperlink("Forgot Password?");
        forgotPasswordLink.setTextFill(Color.GOLD);
        forgotPasswordLink.setOnAction(ev -> showForgotPasswordDialog());

        VBox form = new VBox(10,
                label("Enter your login details:", Color.GOLD, 16, true),
                patientIdField, passwordField, forgotPasswordLink
        );
        form.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String id = patientIdField.getText().trim();
                String pass = passwordField.getText().trim();

                if (id.isEmpty() || pass.isEmpty()) {
                    feedback.setText("❌ Please fill all fields.");
                    return;
                }

                try (Connection conn = sqlconnector.connect()) {
                    String sql = "SELECT * FROM patients WHERE patient_id=? AND password=?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, id);
                    ps.setString(2, pass);
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        new PatientDashboard(id).show(stage);
                    } else {
                        feedback.setText("❌ Invalid credentials.");
                    }
                } catch (Exception ex) {
                    feedback.setText("❌ Error: " + ex.getMessage());
                }
            }
        });
    }

    // =====================================================
    //  PRACTITIONER LOGIN (NOW USING SEPARATE PAGE)
    // =====================================================
    private void showPractitionerLogin(Stage stage, Label feedback) {
        try {
            PractitionerLoginPage loginScreen = new PractitionerLoginPage();
            loginScreen.show(stage);
        } catch (Exception ex) {
            feedback.setText("❌ Error loading Practitioner Login: " + ex.getMessage());
        }
    }

    // =====================================================
    //  REGISTER NEW PATIENT  
    // =====================================================
    private void showRegisterPatientDialog(Label feedback) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register New Patient");
        dialog.getDialogPane().setStyle("-fx-background-color: black;");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField ageField = new TextField();
        ageField.setPromptText("Age");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");

        TextField visiblePassword = new TextField();
        visiblePassword.setManaged(false);
        visiblePassword.setVisible(false);
        visiblePassword.textProperty().bindBidirectional(passField.textProperty());

        Button togglePass = new Button("👁️");
        togglePass.setStyle("-fx-background-color: transparent; -fx-text-fill: gold; -fx-font-size: 14;");
        togglePass.setOnAction(e -> {
            boolean showing = visiblePassword.isVisible();
            visiblePassword.setVisible(!showing);
            visiblePassword.setManaged(!showing);
            passField.setVisible(showing);
            passField.setManaged(showing);
        });

        HBox passBox = new HBox(passField, visiblePassword, togglePass);
        passBox.setAlignment(Pos.CENTER_RIGHT);
        passBox.setSpacing(5);

        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Confirm Password");

        TextField visibleConfirm = new TextField();
        visibleConfirm.setManaged(false);
        visibleConfirm.setVisible(false);
        visibleConfirm.textProperty().bindBidirectional(confirmPassField.textProperty());

        Button toggleConfirm = new Button("👁️");
        toggleConfirm.setStyle("-fx-background-color: transparent; -fx-text-fill: gold; -fx-font-size: 14;");
        toggleConfirm.setOnAction(e -> {
            boolean showing = visibleConfirm.isVisible();
            visibleConfirm.setVisible(!showing);
            visibleConfirm.setManaged(!showing);
            confirmPassField.setVisible(showing);
            confirmPassField.setManaged(showing);
        });

        HBox confirmBox = new HBox(confirmPassField, visibleConfirm, toggleConfirm);
        confirmBox.setAlignment(Pos.CENTER_RIGHT);
        confirmBox.setSpacing(5);

        VBox form = new VBox(10,
                label("Enter New Patient Details:", Color.GOLD, 16, true),
                nameField, ageField, emailField,
                passBox, confirmBox
        );
        form.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {

                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String pass = passField.getText().trim();
                String confirm = confirmPassField.getText().trim();
                String ageText = ageField.getText().trim();

                if (name.isEmpty() || email.isEmpty() || pass.isEmpty()
                        || confirm.isEmpty() || ageText.isEmpty()) {
                    feedback.setText("❌ All fields are required.");
                    feedback.setTextFill(Color.RED);
                    return;
                }

                if (!pass.equals(confirm)) {
                    feedback.setText("❌ Passwords do not match.");
                    feedback.setTextFill(Color.RED);
                    return;
                }

                int age;
                try {
                    age = Integer.parseInt(ageText);
                    if (age <= 0) throw new NumberFormatException();
                } catch (Exception ex) {
                    feedback.setText("❌ Invalid age.");
                    feedback.setTextFill(Color.RED);
                    return;
                }

                try (Connection conn = sqlconnector.connect()) {

                    PreparedStatement check = conn.prepareStatement("SELECT email FROM patients WHERE email = ?");
                    check.setString(1, email);
                    ResultSet rs = check.executeQuery();
                    if (rs.next()) {
                        feedback.setText("❌ Email already registered.");
                        feedback.setTextFill(Color.RED);
                        return;
                    }

                    String newId = generatePatientId(conn);

                    PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO patients (patient_id, full_name, age, email, password) VALUES (?, ?, ?, ?, ?)");
                    ps.setString(1, newId);
                    ps.setString(2, name);
                    ps.setInt(3, age);
                    ps.setString(4, email);
                    ps.setString(5, pass);
                    ps.executeUpdate();

                    feedback.setText("✅ Registered successfully! ID: " + newId);
                    feedback.setTextFill(Color.LIGHTGREEN);

                } catch (SQLException ex) {
                    feedback.setText("❌ Error: " + ex.getMessage());
                    feedback.setTextFill(Color.RED);
                }
            }
        });
    }

    // =====================================================
    //  FORGOT PASSWORD → RESET PASSWORD
    // =====================================================
    private void showForgotPasswordDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Forgot Password");
        dialog.getDialogPane().setStyle("-fx-background-color: black;");

        TextField emailField = new TextField();
        emailField.setPromptText("Enter your registered email");

        VBox form = new VBox(10,
                label("Enter your email to reset password:", Color.GOLD, 16, true),
                emailField
        );
        form.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {

                String email = emailField.getText().trim();

                try (Connection conn = sqlconnector.connect()) {

                    PreparedStatement ps = conn.prepareStatement(
                            "SELECT * FROM patients WHERE email=?");
                    ps.setString(1, email);
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        showResetPasswordDialog(conn, email);
                    } else {
                        alert("Error", "❌ Email not found.", Alert.AlertType.ERROR);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void showResetPasswordDialog(Connection conn, String email) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Reset Password");
        dialog.getDialogPane().setStyle("-fx-background-color: black;");

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("Enter new password");

        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Confirm new password");

        StackPane newPassPane = createPasswordFieldWithToggle(newPass);
        StackPane confirmPassPane = createPasswordFieldWithToggle(confirmPass);

        VBox form = new VBox(10,
                label("Set your new password:", Color.GOLD, 16, true),
                newPassPane,
                confirmPassPane
        );
        form.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {

                String pass = newPass.getText().trim();
                String confirm = confirmPass.getText().trim();

                if (pass.isEmpty() || confirm.isEmpty()) {
                    alert("Error", "❌ All fields are required.", Alert.AlertType.ERROR);
                    return;
                }

                if (!pass.equals(confirm)) {
                    alert("Error", "❌ Passwords do not match.", Alert.AlertType.ERROR);
                    return;
                }

                try {
                    PreparedStatement ps = conn.prepareStatement(
                            "UPDATE patients SET password=? WHERE email=?");
                    ps.setString(1, pass);
                    ps.setString(2, email);
                    ps.executeUpdate();

                    alert("Success", "✅ Password updated successfully!", Alert.AlertType.INFORMATION);

                } catch (SQLException ex) {
                    alert("Error", "❌ " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private StackPane createPasswordFieldWithToggle(PasswordField passwordField) {
        TextField visibleField = new TextField();
        visibleField.setManaged(false);
        visibleField.setVisible(false);
        visibleField.textProperty().bindBidirectional(passwordField.textProperty());

        Button toggleButton = new Button("👁");
        toggleButton.setStyle("-fx-background-color: transparent; -fx-text-fill: gold;");
        toggleButton.setFocusTraversable(false);

        toggleButton.setOnAction(e -> {
            boolean showing = visibleField.isVisible();
            visibleField.setVisible(!showing);
            visibleField.setManaged(!showing);
            passwordField.setVisible(showing);
            passwordField.setManaged(showing);
        });

        StackPane.setAlignment(toggleButton, Pos.CENTER_RIGHT);
        return new StackPane(passwordField, visibleField, toggleButton);
    }

    private void alert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.getDialogPane().setStyle("-fx-background-color: black;");
        ((Label) a.getDialogPane().lookup(".content.label")).setTextFill(Color.GOLD);
        a.show();
    }

    private Label label(String text, Color color, int size, boolean bold) {
        Label lbl = new Label(text);
        lbl.setTextFill(color);
        lbl.setFont(Font.font("Arial", bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        return lbl;
    }

    private String generatePatientId(Connection conn) throws SQLException {
        String lastId = null;

        PreparedStatement ps = conn.prepareStatement(
                "SELECT patient_id FROM patients ORDER BY patient_id DESC LIMIT 1");
        ResultSet rs = ps.executeQuery();

        if (rs.next()) lastId = rs.getString("patient_id");

        int nextNum = 1;
        if (lastId != null && lastId.matches("PATIENT\\d+")) {
            nextNum = Integer.parseInt(lastId.replace("PATIENT", "")) + 1;
        }

        return String.format("PATIENT%03d", nextNum);
    }

    public static void main(String[] args) {
        launch();
    }
}
