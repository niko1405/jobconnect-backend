package com.acme.jobconnect.mail;

import com.acme.jobconnect.entity.JobOffer;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("ClassNamePrefixedWithPackageName")
public class MailService {
    private final StableValue<Logger> logger = StableValue.of();

    /// Objekt für _Jakarta Mail_, um Emails zu verschicken
    private final JavaMailSender mailSender;

    /// Injizierte Properties für _Spring Mail_.
    private final MailConfig mailConfig;

    /// Mailserver
    @Value("${spring.mail.host}")
    @SuppressWarnings("NullAway.Init")
    private String mailhost;

    MailService(final JavaMailSender mailSender, final MailConfig mailConfig) {
        this.mailSender = mailSender;
        this.mailConfig = mailConfig;
    }

    @Async
    public void send(final JobOffer newJobOffer) {
        final var mimeMessage = mailSender.createMimeMessage();

        try {
            final var mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(mailConfig.from());
            mimeMessageHelper.setTo(mailConfig.sales());
            mimeMessageHelper.setSubject("Neue JobOffer " + newJobOffer.getId());
            final var plainText = "Neue JobOffer: " + newJobOffer.getDescription().getTitle();
            final var htmlText = "<strong>Neuer JobOffer:</strong> <em>" + newJobOffer.getDescription().getTitle() + "</em>";
            mimeMessageHelper.setText(plainText, htmlText);

            mailSender.send(mimeMessage);
            getLogger().trace("send: Thread-ID={}, mailConfig={}, jobOffer={}",
                Thread.currentThread().threadId(), mailhost, newJobOffer);
        } catch (MailException | MessagingException _) {
            // TODO Wiederholung, um die Email zu senden
            getLogger().warn("Email nicht gesendet: Ist der Mailserver {} erreichbar?", mailhost);
        }
    }

    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(MailService.class));
    }
}
