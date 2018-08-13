package com.ligl.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Date;
import java.util.Properties;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/10/17 0017 上午 9:56
 * Version: 1.0
 */
public class EmailUtils {

    //发件人邮箱
    private String fromEmailUser;
    //发件人邮箱密码
    private String fromEmailPass;
    //web邮件服务器地址
    public static final String WEB_EMAIL_ADDRESS = "webmail.zillionfortune.com";

    private static Logger logger = LoggerFactory.getLogger(EmailUtils.class);

    private Properties properties = new Properties();
    /*
     * 初始化方法
     */
//	public EmailUtils() {
//		InputStream in = EmailUtils.class.getResourceAsStream("MailServer.properties");
//		try {
//			properties.load(in);
//			this.WEB_EMAIL_ADDRESS = properties.getProperty("mail.smtp.emailAdr");
//			this.fromEmailUser = properties.getProperty("mail.sender.fromEmailUser");
//			this.fromEmailPass = properties.getProperty("mail.sender.fromEmailPass");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

    /**
     * @param toMail  收件人邮箱，多个收件人用,分隔
     * @param csMail  抄送人邮箱，多个抄送人用,分隔
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    public boolean sendMail(String toMail, String csMail, String subject, String content, String... files) {

        try {
            if (StringUtils.isEmpty(toMail)) {
                return false;
            }

            Properties p = new Properties();
            p.put("mail.smtp.auth", "true");
            p.put("mail.transport.protocol", "smtp");
            p.put("mail.smtp.host", "smtp.zillionfortune.com");
            p.put("mail.smtp.port", "25");

            // 建立会话
            Session session = Session.getInstance(p);
            Message msg = new MimeMessage(session);
            BodyPart messageBodyPart = new MimeBodyPart();
            // 发件人
            msg.setFrom(new InternetAddress(fromEmailUser));
            // 收件人
            InternetAddress[] iaToList = InternetAddress.parse(toMail);
            msg.setRecipients(Message.RecipientType.TO, iaToList);
            // 抄送人
            if (StringUtils.isNotEmpty(csMail)) {
                InternetAddress[] iaToListcs = InternetAddress.parse(csMail);
                msg.setRecipients(Message.RecipientType.CC, iaToListcs);
            }
            /*添加正文内容*/
            Multipart multipart = new MimeMultipart();
            BodyPart contentPart = new MimeBodyPart();
            contentPart.setText(content);

            contentPart.setHeader("Content-Type", "text/html; charset=GBK");
            multipart.addBodyPart(contentPart);
			/*添加附件*/
            for (String file : files) {
                File usFile = new File(file);
                MimeBodyPart fileBody = new MimeBodyPart();
                DataSource source = new FileDataSource(file);
                fileBody.setDataHandler(new DataHandler(source));
                sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();
//				fileBody.setFileName(MimeUtility.encodeText(new String(usFile.getName().getBytes(), "GB2312"), "GB2312", "B"));
//				fileBody.setFileName("=?GBK?B?"
//						+ enc.encode(usFile.getName().getBytes()) + "?=");
                fileBody.setFileName("123.jpg");
                multipart.addBodyPart(fileBody);
            }
            msg.setContent(multipart);
            // 发送日期
            msg.setSentDate(new Date());
            // 主题
            msg.setSubject(subject);
            // 内容
            msg.setText(content);
            // 邮件服务器进行验证
            Transport tran = session.getTransport("smtp");
            tran.connect(WEB_EMAIL_ADDRESS, this.fromEmailUser, this.fromEmailPass);
            // 发送
            tran.sendMessage(msg, msg.getAllRecipients());

            logger.info("邮件发送成功");
            return true;
        } catch (MessagingException e) {
            logger.info("邮件发送时异常", e);
            return false;
        }
    }

    public String getFromEmailUser() {
        return fromEmailUser;
    }

    public void setFromEmailUser(String fromEmailUser) {
        this.fromEmailUser = fromEmailUser;
    }

    public String getFromEmailPass() {
        return fromEmailPass;
    }

    public void setFromEmailPass(String fromEmailPass) {
        this.fromEmailPass = fromEmailPass;
    }

    public static void main(String args[]) {
        String tomail = "lgl19881005@163.com";
        String subject = "nihaoma";
        String content = "我很好";
        String csMail = "407455574@qq.com";
        EmailUtils emailUtils = new EmailUtils();
        emailUtils.setFromEmailUser("liguoliang@zillionfortune.com");
        emailUtils.setFromEmailPass("nihaoma");
        boolean result = emailUtils.sendMail(tomail, null, subject, content, "D:/sssss.jpg");
        System.out.println("=====" + result);
    }

}
