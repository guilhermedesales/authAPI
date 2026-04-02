package com.api.auth.Application.Service;

import com.api.auth.Application.Utils.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationCode(String toEmail, String code) {
        log.info("[EMAIL] Sending verification code - email={}", LogSanitizer.maskEmail(toEmail));
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Seu código de verificação");
        message.setText("Seu código de acesso é: " + code + "\nEle expira em 5 minutos.");
        mailSender.send(message);
        log.info("[EMAIL] Verification code sent - email={}", LogSanitizer.maskEmail(toEmail));
    }
}
