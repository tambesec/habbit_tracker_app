package com.example.finalproject.model;

import android.os.AsyncTask;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class GmailSender {

    public void sendEmail(final String username, final String password, String recipientEmail, String subject, String messageBody) {
        new SendEmailTask().execute(username, password, recipientEmail, subject, messageBody);
    }

    private static class SendEmailTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            String recipientEmail = params[2];
            String subject = params[3];
            String messageBody = params[4];

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject(subject);
                message.setText(messageBody);

                Transport.send(message);

                System.out.println("Email sent successfully.");

            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
