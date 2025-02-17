/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.selfservice.util;

import java.util.Date;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author zqin
 */
public class Mail {

    
    public static void send(String toEmail,String subject, String msg) throws MessagingException {
       
        Properties mailProperties = FileUtil.getProperties("/configuration.properties");
        
        mailProperties.put("mail.smtp.auth", "true");   
        mailProperties.put("mail.smtp.starttls.enable", "true");   
        mailProperties.put("mail.smtp.timeout", 5000);
        
        Session session = Session.getInstance(mailProperties, new Authenticator(){
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailProperties.getProperty("mail.smtp.user"),mailProperties.getProperty("mail.smtp.pass"));
            }
        });
        
        Multipart mainPart = new MimeMultipart();
        BodyPart html = new MimeBodyPart();
        html.setContent(msg, "text/html; charset=utf-8");
        mainPart.addBodyPart(html);

        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setContent(mainPart);
        mimeMessage.setSubject(subject);
        Address from = new InternetAddress(mailProperties.getProperty("mail.smtp.user"));
        mimeMessage.setFrom(from);
        Address to = new InternetAddress(toEmail);
        mimeMessage.setRecipient(Message.RecipientType.TO, to);
        mimeMessage.setSentDate(new Date());
        
        mimeMessage.saveChanges();
        
        Transport.send(mimeMessage);       
    }
}
