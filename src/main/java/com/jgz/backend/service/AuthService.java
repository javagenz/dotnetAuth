package com.jgz.backend.service;

import com.jgz.backend.entity.UserActivity;
import com.jgz.backend.pojo.ReqRes;
import com.jgz.backend.repository.OurUserRepository;
import com.jgz.backend.entity.OurUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private OurUserRepository ourUserRepository;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private LogActivityService logActivityService;

    public ResponseEntity<ReqRes> signUp(ReqRes registrationRequest) {
        ReqRes resp = new ReqRes();
        UserActivity userActivity = new UserActivity();
        try {
            OurUser ourUser = new OurUser();
            ourUser.setEmail(registrationRequest.getEmail());
            ourUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            ourUser.setRole(registrationRequest.getRole().toUpperCase(Locale.ROOT));
            ourUser.setName(registrationRequest.getName());
            ourUser.setPhoneNumber(registrationRequest.getPhoneNumber());
            ourUser.setIdentity(registrationRequest.getIdentity());
            ourUser.setUsername(registrationRequest.getUsername());
            OurUser ourUserResult = ourUserRepository.save(ourUser);
            if (ourUserResult != null && ourUserResult.getId() > 0) {
                resp.setUsers(ourUserResult);
                resp.setMessage("User saved successfully");
                resp.setStatusCode(200);

                log.info("User {} successfully registered", registrationRequest.getEmail());

                return ResponseEntity.ok().body(resp);
            }
            return ResponseEntity.ok().body(resp);
        } catch (Exception e) {
            resp.setMessage(e.getMessage());
            resp.setStatusCode(500);

            log.error("Error while registering user {}", registrationRequest.getEmail());

            return ResponseEntity.internalServerError().body(resp);
        }
    }

    public ResponseEntity<ReqRes> signIn(ReqRes signInRequest) {
        ReqRes resp = new ReqRes();
        try {
            // Determine if the input is email or phone number
                Optional<OurUser> userOptional = signInRequest.getEmail().contains("@") ?
                    ourUserRepository.findByEmail(signInRequest.getEmail()) :
                    ourUserRepository.findByPhoneNumber(signInRequest.getEmail());

            if (!userOptional.isPresent()) {
                resp.setMessage("Email or Phone Number not found");
                resp.setStatusCode(400);
                log.error("Failed to login user, Email or Phone Number not found {}", signInRequest.getEmail());
                return ResponseEntity.badRequest().body(resp);
            }

            var user = userOptional.orElseThrow();

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), signInRequest.getPassword()));

            var jwt = jwtUtils.generateToken(user);
            var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

            resp.setMessage("Successfully Signed In");
            resp.setStatusCode(200);
            resp.setName(user.getName());
            resp.setToken(jwt);
            resp.setRefreshToken(refreshToken);
            resp.setExpirationTime("24Hr");

            log.info("User {} successfully logged in", user.getEmail());

            return ResponseEntity.ok().body(resp);
        } catch (AuthenticationException e) {
            resp.setMessage("Invalid Password");
            resp.setStatusCode(400);
            log.error("Failed to login user, Invalid Password {}", signInRequest.getEmail());
            return ResponseEntity.badRequest().body(resp);
        } catch (Exception e) {
            resp.setMessage(e.getMessage());
            resp.setStatusCode(500);
            log.error("Failed to login user {}", signInRequest.getEmail());
            return ResponseEntity.internalServerError().body(resp);
        }
    }

    public ResponseEntity<ReqRes> refreshToken(ReqRes refreshTokenRequest) {
        ReqRes resp = new ReqRes();
        String ourEmail = jwtUtils.extractUsername(refreshTokenRequest.getToken());
        OurUser users = ourUserRepository.findByEmail(ourEmail).orElseThrow();
        if (jwtUtils.isTokenValid(refreshTokenRequest.getToken(), users)) {
            var jwt = jwtUtils.generateToken(users);
            resp.setMessage("Successfully Refreshed Token");
            resp.setStatusCode(200);
            resp.setToken(jwt);
            resp.setRefreshToken(refreshTokenRequest.getToken());
            resp.setExpirationTime("25Hr");

            // record activity
            logActivityService.recordActivity(
                    users.getId(),
                    users.getEmail(),
                    "REFRESH TOKEN SUCCESS"
            );
            return ResponseEntity.ok().body(resp);
        } else {
            resp.setStatusCode(500);

            // record activity
            logActivityService.recordActivity(
                    ourEmail,
                    "REFRESH TOKEN FAILED"
            );
            return ResponseEntity.internalServerError().body(resp);
        }
    }
}

