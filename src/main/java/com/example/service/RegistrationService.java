package com.example.service;

import com.example.appUser.AppUser;
import com.example.appUser.AppUserRole;
import com.example.request.RegistrationRequest;
import com.example.token.ConfirmationToken;
import com.example.token.ConfirmationTokenService;
import com.example.utils.EmailValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class RegistrationService {

    private final AppUserService appUserService;
    private final EmailValidator emailValidator;
    private final ConfirmationTokenService confirmationTokenService;

    public String registerUser(RegistrationRequest request) {

        boolean isValidEmail = emailValidator.test(request.getEmail());

        if (!isValidEmail){
            throw new IllegalStateException("email is not valid");
        }

        return appUserService.signUpUser(
                new AppUser(
                        request.getFirstName(),
                        request.getLastName(),
                        request.getEmail(),
                        request.getPassword(),
                        AppUserRole.USER
                )
        );
    }

    @Transactional
    public String confirmToken(String token){

        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token)
                .orElseThrow(
                        () -> new IllegalStateException("token not found")
                );

        if (confirmationToken.getConfirmedAt() != null){
            throw new IllegalStateException("email already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if(expiredAt.isBefore(LocalDateTime.now())){
            throw new IllegalStateException("token expired");
        }

        confirmationTokenService.setConfirmedAt(token);
        appUserService.enableAppUser(
                confirmationToken.getAppUser().getEmail()
        );
        return "Confirmed";
    }
}
