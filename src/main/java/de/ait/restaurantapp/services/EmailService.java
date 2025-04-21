package de.ait.restaurantapp.services;

import de.ait.restaurantapp.dto.EmailDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendHTMLEmail(EmailDto dto) throws MessagingException {
        Context context = new Context();
        context.setVariable("name", dto.getName());
        context.setVariable("reservationCode", dto.getReservationCode());

        String html = templateEngine.process("email-template", context);

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        mimeMessageHelper.setTo(dto.getTo());
        mimeMessageHelper.setSubject(dto.getSubject());
        mimeMessageHelper.setText(html, true);

        mailSender.send(mimeMessage);
    }
}
