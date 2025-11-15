import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailSender {

    public static void sendEmail(String toEmail, String subject, String body) {
        // your Gmail
        final String fromEmail = "fabsmedlab@gmail.com";  
        // Gmail App Password
        final String password = "tzwa ibwz oanq bfol";  

        try {
            // ==================== PRIMARY METHOD: TLS (Port 587) ====================
            Properties tlsProps = new Properties();
            tlsProps.put("mail.smtp.host", "smtp.gmail.com");
            tlsProps.put("mail.smtp.port", "587");
            tlsProps.put("mail.smtp.auth", "true");
            tlsProps.put("mail.smtp.starttls.enable", "true");  // Use TLS
            tlsProps.put("mail.smtp.connectiontimeout", "10000");
            tlsProps.put("mail.smtp.timeout", "10000");

            Session session = Session.getInstance(tlsProps, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            System.out.println("🔄 Sending email using TLS...");
            Transport.send(message);
            System.out.println("✅ Email sent successfully to: " + toEmail);
            return;

        } catch (Exception tlsError) {
            System.out.println("⚠️ TLS send failed: " + tlsError.getMessage());
            System.out.println("↩️ Retrying using SSL...");
        }

        // ==================== BACKUP METHOD: SSL (Port 465) ====================
        try {
            Properties sslProps = new Properties();
            sslProps.put("mail.smtp.host", "smtp.gmail.com");
            sslProps.put("mail.smtp.port", "465");
            sslProps.put("mail.smtp.auth", "true");
            sslProps.put("mail.smtp.ssl.enable", "true");
            sslProps.put("mail.smtp.connectiontimeout", "10000");
            sslProps.put("mail.smtp.timeout", "10000");

            Session sslSession = Session.getInstance(sslProps, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, password);
                }
            });

            Message sslMessage = new MimeMessage(sslSession);
            sslMessage.setFrom(new InternetAddress(fromEmail));
            sslMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            sslMessage.setSubject(subject);
            sslMessage.setText(body);

            Transport.send(sslMessage);
            System.out.println("✅ Email sent successfully (via SSL backup) to: " + toEmail);

        } catch (Exception sslError) {
            System.out.println("❌ SSL send failed too. Please check your internet connection or firewall.");
            sslError.printStackTrace();
        }
    }
}
