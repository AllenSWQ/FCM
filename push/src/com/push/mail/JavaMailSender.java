package com.push.mail;

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class JavaMailSender{   
    /** 邮件服务器身份验证 */
    private Authenticator authenticator;
    /** 邮件会话 */
    private Session sendMailSession;


    public boolean sendMail(String[] receivers, String[] copyTo, String subject, String content){    	

        try {
        	Properties pro = new Properties();
       	    pro.load(JavaMailSender.class.getResourceAsStream("conf/mail.properties"));
       	    authenticator = new JavaMailAuth(pro.getProperty("serverUname"),pro.getProperty("serverPass"));
            // 创建 session
            sendMailSession = Session.getDefaultInstance(pro, authenticator);
            // 创建 邮件消息
            Message mail = new MimeMessage(sendMailSession);
            // 发送者账号
            mail.setFrom(new InternetAddress(pro.getProperty("serverUname")));

            // 添加接收者
            Address[] receivAddrs = new Address[receivers.length];
            for (int i = 0; i < receivers.length; i++) {
                receivAddrs[i] = new InternetAddress(receivers[i], "", "utf-8");
            }
            mail.setRecipients(Message.RecipientType.TO, receivAddrs);

            // 添加抄送者
            if (null != copyTo && copyTo.length > 0) {
                Address[] copyToAddrs = new Address[copyTo.length];
                for (int i = 0; i < copyTo.length; i++) {
                    copyToAddrs[i] = new InternetAddress(copyTo[i], "", "utf-8");
                }
                mail.setRecipients(Message.RecipientType.CC, copyToAddrs);
            }

            mail.setSubject(subject);
            mail.setSentDate(new Date());
            mail.setText(content);

            Transport.send(mail);

            return true;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }
    
	public boolean sendMailContent(String[] receivers, String[] copyTo,
			String subject, String content) {
		try {
			Properties pro = new Properties();
			pro.load(JavaMailSender.class
					.getResourceAsStream("/conf/mail.properties"));
			authenticator = new JavaMailAuth(pro.getProperty("serverUname"),
					pro.getProperty("serverPass"));
			// 创建 session
			sendMailSession = Session.getDefaultInstance(pro, authenticator);
			// 创建 邮件消息
			MimeMessage mail = new MimeMessage(sendMailSession);
			// 发送者账号
			mail.setFrom(new InternetAddress(pro.getProperty("serverUname")));

			// 添加接收者
			Address[] receivAddrs = new Address[receivers.length];
			for (int i = 0; i < receivers.length; i++) {
				receivAddrs[i] = new InternetAddress(receivers[i], "", "utf-8");
			}
			mail.setRecipients(Message.RecipientType.TO, receivAddrs);

			// 添加抄送者
			if (null != copyTo && copyTo.length > 0) {
				Address[] copyToAddrs = new Address[copyTo.length];
				for (int i = 0; i < copyTo.length; i++) {
					copyToAddrs[i] = new InternetAddress(copyTo[i], "", "utf-8");
				}
				mail.setRecipients(Message.RecipientType.CC, copyToAddrs);
			}

			mail.setSubject(subject);
			mail.setSentDate(new Date());
			mail.setContent(content, "text/html;charset=UTF-8");

			Transport.send(mail);

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return false;
	}

}
