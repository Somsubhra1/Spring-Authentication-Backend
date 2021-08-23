package com.somsubhra.springauth.appuser;

import com.somsubhra.springauth.email.EmailSender;
import com.somsubhra.springauth.registration.RegistrationService;
import com.somsubhra.springauth.registration.token.ConfirmationToken;
import com.somsubhra.springauth.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MSG = "user with email %s not found";
    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;
    private final AppUserService appUserService;
    private final RegistrationService registrationService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, email)));
    }

    public String signUpUser(AppUser appUser) {
        boolean userExists = appUserRepository.findByEmail(appUser.getEmail()).isPresent();

        if (userExists) {
            if (!appUser.getEnabled()) {
                String token = appUserService.signUpUser(
                        new AppUser(
                                appUser.getFirstName(),
                                appUser.getLastName(),
                                appUser.getEmail(),
                                appUser.getPassword(),
                                AppUserRole.USER
                        ));
                String link = "http://localhost:8080/api/v1/registration/confirm?token=" + token;
                emailSender.send(
                        appUser.getEmail(),
                        registrationService.buildEmail(appUser.getFirstName(), link)
                );
            }
            throw new IllegalStateException("Email already taken!");
        }
        String encodedPassword = bCryptPasswordEncoder.encode(appUser.getPassword());

        appUser.setPassword(encodedPassword);

        appUserRepository.save(appUser);

        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                appUser
        );

        confirmationTokenService.saveConfirmationToken(confirmationToken);

        return token;
    }

    public int enableAppUser(String email) {
        return appUserRepository.enableAppUser(email);
    }
}
