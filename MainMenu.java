
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

    // Popup used for Test List
    private Stage testPopup;

    @Override
    public void start(Stage stage) {

        // ====== HEADER ======
        Label title = new Label("WELCOME TO "
                + "FAB'S MEDICAL LAB");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.GOLD);

        // ===== TOP RIGHT BUTTON =====
        Button testListBtn = new Button("🧪 Tests & Prices");
        testListBtn.setStyle("-fx-background-color: #0d47a1; -fx-text-fill: white; -fx-font-weight: bold;");
        testListBtn.setOnAction(e -> showTestListPopup());

        HBox topRight = new HBox(testListBtn);
        topRight.setAlignment(Pos.TOP_RIGHT);
        topRight.setPadding(new Insets(0, 0, 10, 0));

        // ===== MAIN BUTTONS =====
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
        VBox layout = new VBox(15, topRight, title, patientLogin, registerPatient, practitionerLogin, feedback);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: black;");

        stage.setScene(new Scene(layout, 500, 420));
        stage.setTitle("Fab's Medical Lab");
        stage.show();
    }

    // =====================================================
    //  PATIENT LOGIN
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
    //  PRACTITIONER LOGIN
    // =====================================================
    private void showPractitionerLogin(Stage stage, Label feedback) {
        try {
            new PractitionerLoginPage().show(stage);
        } catch (Exception ex) {
            feedback.setText("❌ Error loading Practitioner Login: " + ex.getMessage());
        }
    }

    // =====================================================
//  REGISTER NEW PATIENT  (UPDATED VERSION)
// =====================================================
    private void showRegisterPatientDialog(Label feedback) {
        // Create dialog window
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register New Patient");
        dialog.getDialogPane().setStyle("-fx-background-color: black;");

        // === Input fields ===
        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField ageField = new TextField();
        ageField.setPromptText("Age");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");

        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Confirm Password");

        // === Password Fields with Eye Icon Toggle ===
        passField.setPromptText("Password");

        TextField passVisibleField = new TextField();
        passVisibleField.setManaged(false);
        passVisibleField.setVisible(false);
        passVisibleField.setPromptText("Password");
        passVisibleField.textProperty().bindBidirectional(passField.textProperty());

// Eye toggle button for password
        Button togglePass = new Button("👁️");
        togglePass.setStyle("-fx-background-color: transparent; -fx-text-fill: gold; -fx-font-size: 14;");
        togglePass.setOnAction(e -> {
            boolean showing = passVisibleField.isVisible();
            passVisibleField.setVisible(!showing);
            passVisibleField.setManaged(!showing);
            passField.setVisible(showing);
            passField.setManaged(showing);
        });

// Combine field + toggle into one line
        HBox passwordBox = new HBox(passField, passVisibleField, togglePass);
        passwordBox.setAlignment(Pos.CENTER_RIGHT);
        passwordBox.setSpacing(5);

// === Confirm Password Field with Eye Icon Toggle ===
        confirmPassField.setPromptText("Confirm Password");

        TextField confirmVisibleField = new TextField();
        confirmVisibleField.setManaged(false);
        confirmVisibleField.setVisible(false);
        confirmVisibleField.setPromptText("Confirm Password");
        confirmVisibleField.textProperty().bindBidirectional(confirmPassField.textProperty());

        Button toggleConfirm = new Button("👁️");
        toggleConfirm.setStyle("-fx-background-color: transparent; -fx-text-fill: gold; -fx-font-size: 14;");
        toggleConfirm.setOnAction(e -> {
            boolean showing = confirmVisibleField.isVisible();
            confirmVisibleField.setVisible(!showing);
            confirmVisibleField.setManaged(!showing);
            confirmPassField.setVisible(showing);
            confirmPassField.setManaged(showing);
        });

// Combine confirm field + toggle
        HBox confirmBox = new HBox(confirmPassField, confirmVisibleField, toggleConfirm);
        confirmBox.setAlignment(Pos.CENTER_RIGHT);
        confirmBox.setSpacing(5);

// === Layout (replace old VBox form section with this) ===
        VBox form = new VBox(10,
                label("Enter New Patient Details:", Color.GOLD, 16, true),
                nameField, ageField, emailField,
                passwordBox, confirmBox
        );
        form.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // === On dialog confirmation ===
        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String pass = passField.getText().trim();
                String confirm = confirmPassField.getText().trim();
                String ageText = ageField.getText().trim();

                // === Basic field validation ===
                if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty() || ageText.isEmpty()) {
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
                    if (age <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    feedback.setText("❌ Invalid age. Please enter a valid number.");
                    feedback.setTextFill(Color.RED);
                    return;
                }

                try (Connection conn = sqlconnector.connect()) {
                    if (conn == null) {
                        feedback.setText("❌ Database connection failed.");
                        feedback.setTextFill(Color.RED);
                        return;
                    }

                    // === Prevent duplicate emails ===
                    PreparedStatement check = conn.prepareStatement("SELECT email FROM patients WHERE email = ?");
                    check.setString(1, email);
                    ResultSet rs = check.executeQuery();
                    if (rs.next()) {
                        feedback.setText("❌ Email already registered. Try logging in.");
                        feedback.setTextFill(Color.RED);
                        return;
                    }

                    // === Generate unique patient ID ===
                    String newId = generatePatientId(conn);

                    // === Insert new record into database ===
                    String sql = "INSERT INTO patients (patient_id, full_name, age, email, password) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, newId);
                    ps.setString(2, name);
                    ps.setInt(3, age);
                    ps.setString(4, email);
                    ps.setString(5, pass);
                    ps.executeUpdate();

                    // === Send confirmation email ===
                    try {
                        EmailSender.sendEmail(
                                email,
                                "Registration Successful - Fab's Medical Lab System",
                                "Hello " + name + ",\n\n"
                                + "🎉 Welcome to our Fab's Medical Lab \n\n"
                                + "Your unique Patient ID is: " + newId + "\n"
                                + "Please keep it safe — you’ll need it to log in.\n\n"
                                + "Best regards,\nFab's Medical Lab Team"
                        );
                    } catch (Exception mailEx) {
                        System.out.println("Email sending failed: " + mailEx.getMessage());
                    }

                    // === Show confirmation alert ===
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Registration Successful");
                    success.setHeaderText(null);
                    success.setContentText("🎉 Registration complete!\n\nYour unique Patient ID is:\n"
                            + newId + "\n\nKeep it safe — you'll need it to log in.\n\n"
                            + "A confirmation email has been sent to " + email + ".");
                    success.getDialogPane().setStyle("-fx-background-color: black;");
                    ((Label) success.getDialogPane().lookup(".content.label")).setTextFill(Color.GOLD);
                    success.show();

                    feedback.setTextFill(Color.LIGHTGREEN);
                    feedback.setText("✅ Registered successfully! ID: " + newId);

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
   private void showResetPasswordDialog(Connection conn, String email) {

    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Reset Password");
    dialog.getDialogPane().setStyle("-fx-background-color: black;");

    PasswordField newPass = new PasswordField();
    newPass.setPromptText("Enter new password");

    PasswordField confirmPass = new PasswordField();
    confirmPass.setPromptText("Confirm new password");

    VBox form = new VBox(10,
            label("Reset Password for: " + email, Color.GOLD, 16, true),
            newPass,
            confirmPass
    );
    form.setPadding(new Insets(10));

    dialog.getDialogPane().setContent(form);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    dialog.showAndWait().ifPresent(result -> {
        if (result == ButtonType.OK) {

            String p1 = newPass.getText().trim();
            String p2 = confirmPass.getText().trim();

            if (p1.isEmpty() || p2.isEmpty()) {
                alert("Error", "❌ Please fill all fields.", Alert.AlertType.ERROR);
                return;
            }

            if (!p1.equals(p2)) {
                alert("Error", "❌ Passwords do not match!", Alert.AlertType.ERROR);
                return;
            }

            try {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE patients SET password=? WHERE email=?");
                ps.setString(1, p1);
                ps.setString(2, email);

                int updated = ps.executeUpdate();

                if (updated > 0) {
                    alert("Success", "✅ Password reset successfully!", Alert.AlertType.INFORMATION);
                } else {
                    alert("Error", "❌ Failed to reset password.", Alert.AlertType.ERROR);
                }

                conn.close();

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    });
}
   private void showForgotPasswordDialog() {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Forgot Password");
    dialog.getDialogPane().setStyle("-fx-background-color: black;");

    TextField emailField = new TextField();
    emailField.setPromptText("Enter your registered email");

    VBox form = new VBox(10,
            label("Enter your email to reset your password:", Color.GOLD, 16, true),
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
            }
        }
    });
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

    // =====================================================
    //  TEST LIST POPUP
    // =====================================================
    private void showTestListPopup() {

        testPopup = new Stage();
        testPopup.setTitle("Lab Test List");

        Label title = new Label("Available Laboratory Tests");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #cfd8dc;");

        TextArea list = new TextArea();
        list.setEditable(false);
        list.setStyle("-fx-control-inner-background: #0d47a1; -fx-text-fill: white; -fx-font-size: 14px;");
        list.setPrefSize(420, 350);

        list.setText(
                "🔬 LABORATORY TESTS & PRICES\n\n"
                + "1. Full Blood Count (FBC) ........... ₦14,700\n"
                + "2. Malaria Parasite Test ............ ₦15,000\n"
                + "3. HIV Screening Test ............... ₦19,000\n"
                + "4. Genotype Test ..................... ₦17,750\n"
                + "5. Blood Group Test .................. ₦12,300\n"
                + "6. Urinalysis ........................ ₦11,750\n"
                + "7. Cholesterol Test .................. ₦16,500\n"
                + "8. Blood Sugar (BS) ............ ₦14,000\n"
                + "9. Liver Function Test (LFT) ........ ₦49,000\n"
                + "10. Kidney Function Test (KFT) ...... ₦55,550\n"
        );

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white;");
        closeBtn.setOnAction(e -> testPopup.close());

        VBox layout = new VBox(15, title, list, closeBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #102027;");
        layout.setPadding(new Insets(20));

        testPopup.setScene(new Scene(layout, 450, 470));
        testPopup.show();
    }

    // =====================================================
    //  HELPER FUNCTIONS
    // =====================================================
    private Label label(String text, Color color, int size, boolean bold) {
        Label lbl = new Label(text);
        lbl.setTextFill(color);
        lbl.setFont(Font.font("Arial", bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        return lbl;
    }

    private String generatePatientId(Connection conn) throws SQLException {
        ResultSet rs = conn.prepareStatement(
                "SELECT patient_id FROM patients ORDER BY patient_id DESC LIMIT 1").executeQuery();

        if (rs.next()) {
            String last = rs.getString(1).replace("PATIENT", "");
            int next = Integer.parseInt(last) + 1;
            return "PATIENT" + String.format("%03d", next);
        }
        return "PATIENT001";
    }

    public static void main(String[] args) {
        launch();
    }
}
