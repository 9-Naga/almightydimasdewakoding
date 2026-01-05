package com.example.projectbinar.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

  private final JavaMailSender mailSender;

  @Value("${app.mail.from:noreply@loanapp.com}")
  private String fromEmail;

  @Value("${app.mail.from-name:Loan Management System}")
  private String fromName;

  @Value("${app.frontend-url:http://localhost:3000}")
  private String frontendUrl;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  /**
   * Send password reset email with reset link
   *
   * @param toEmail recipient email address
   * @param resetToken the reset token
   * @param userName user's name for personalization
   */
  @Async
  public void sendPasswordResetEmail(String toEmail, String resetToken, String userName) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail, fromName);
      helper.setTo(toEmail);
      helper.setSubject("Reset Password - Loan Management System");

      String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
      String htmlContent = buildPasswordResetEmailContent(userName, resetLink);

      helper.setText(htmlContent, true);

      mailSender.send(message);
      logger.info("Password reset email sent successfully to: {}", toEmail);

    } catch (MessagingException | MailException e) {
      logger.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
      throw new RuntimeException("Failed to send password reset email", e);
    } catch (Exception e) {
      logger.error("Unexpected error sending email to {}: {}", toEmail, e.getMessage());
      throw new RuntimeException("Failed to send email", e);
    }
  }

  /** Build HTML content for password reset email */
  private String buildPasswordResetEmailContent(String userName, String resetLink) {
    return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #007bff; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background-color: #f9f9f9; }
                    .button { display: inline-block; background-color: #007bff; color: white;
                              padding: 12px 30px; text-decoration: none; border-radius: 5px;
                              margin: 20px 0; }
                    .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                    .warning { color: #dc3545; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Loan Management System</h1>
                    </div>
                    <div class="content">
                        <h2>Hello, %s!</h2>
                        <p>Kami menerima permintaan untuk mereset password akun Anda.</p>
                        <p>Klik tombol di bawah ini untuk membuat password baru:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Reset Password</a>
                        </p>
                        <p>Atau salin link berikut ke browser Anda:</p>
                        <p style="word-break: break-all; background: #eee; padding: 10px;">%s</p>
                        <p class="warning">
                            <strong>Penting:</strong> Link ini akan kadaluarsa dalam 1 jam.
                            Jika Anda tidak meminta reset password, abaikan email ini.
                        </p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 Loan Management System. All rights reserved.</p>
                        <p>Email ini dikirim secara otomatis, mohon tidak membalas email ini.</p>
                    </div>
                </div>
            </body>
            </html>
            """
        .formatted(userName, resetLink, resetLink);
  }

  /** Send loan status notification email */
  @Async
  public void sendLoanStatusEmail(
      String toEmail, String userName, String loanStatus, String message) {
    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

      helper.setFrom(fromEmail, fromName);
      helper.setTo(toEmail);
      helper.setSubject("Update Status Pinjaman - " + loanStatus);

      String htmlContent =
          """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #28a745; color: white; padding: 20px; text-align: center; }
                        .content { padding: 30px; background-color: #f9f9f9; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Update Status Pinjaman</h1>
                        </div>
                        <div class="content">
                            <h2>Halo, %s!</h2>
                            <p>%s</p>
                            <p>Status: <strong>%s</strong></p>
                            <p>Login ke aplikasi untuk melihat detail lengkap.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
              .formatted(userName, message, loanStatus);

      helper.setText(htmlContent, true);
      mailSender.send(mimeMessage);
      logger.info("Loan status email sent to: {}", toEmail);

    } catch (Exception e) {
      logger.error("Failed to send loan status email: {}", e.getMessage());
    }
  }
}
