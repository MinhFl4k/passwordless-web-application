package com.app.demo.controller;

import com.app.demo.dto.request.EmailReqDto;
import com.app.demo.dto.request.UserSignupDto;
import com.app.demo.dto.response.UserVerifiedResDto;
import com.app.demo.enums.UserTokenType;
import com.app.demo.service.*;
import com.app.demo.validation.sequence.ValidationSequence;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class IdentityController {

    private final UserService userService;

    private final EmailService emailService;

    private final OtpLoginService otpLoginService;

    private final UserTokenService userTokenService;


    // LOGIN:
    @GetMapping("/login")
    public String showLoginPage(Authentication authentication,
                                HttpSession session,
                                Model model
    ) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }

        Object error = session.getAttribute("FLASH_ERROR");
        if (error != null) {
            model.addAttribute("error", error);
            session.removeAttribute("FLASH_ERROR");
        }

        return "login";
    }

    // SIGNUP:
    @GetMapping("/signup")
    public String showSignupPage(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }
        model.addAttribute("user", new UserSignupDto());
        return "signup";
    }

    @PostMapping("/signup")
    public String signupUser(
            @Validated(ValidationSequence.class) @ModelAttribute("user") UserSignupDto userSignupDto,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return "signup";
        }
        userService.signupUser(userSignupDto);
        return "redirect:/login?signupSuccess";
    }

    // OTP:
    @GetMapping("/login-with-otp")
    public String showOtpLoginPage(HttpSession session, Model model, Authentication authentication) {

        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }
        Object error = session.getAttribute("FLASH_ERROR");

        if (error != null) {
            model.addAttribute("error", error);
            session.removeAttribute("FLASH_ERROR");
        }
        model.addAttribute("emailReq", new EmailReqDto());
        model.addAttribute("email", session.getAttribute("OTP_LOGIN_EMAIL"));

        return "login-with-otp";
    }

    @PostMapping("/send-otp")
    public String sendOtp(
            @Validated(ValidationSequence.class) @ModelAttribute("emailReq") EmailReqDto emailReqDto,
            BindingResult bindingResult,
            HttpSession session
    ) {
        if (bindingResult.hasErrors()) {
            return "login-with-otp";
        }
        String email = emailReqDto.getEmail();
        try {
            String otp = otpLoginService.generateOtp(email);
            emailService.sendOtp(email, otp);
            session.setAttribute("OTP_LOGIN_EMAIL", email);

        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
        return "redirect:/login-with-otp?sent=true";
    }

    // TOTP:
    @GetMapping("/login-with-totp")
    public String showTotpLoginPage(
            HttpSession session,
            Authentication authentication,
            Model model
    ) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }

        Object error = session.getAttribute("FLASH_ERROR");

        if (error != null) {
            model.addAttribute("error", error);
            session.removeAttribute("FLASH_ERROR");
        }

        model.addAttribute("email", session.getAttribute("TOTP_LOGIN_EMAIL"));
        return "login-with-totp";
    }

    // TOKEN LOGIN:
    @GetMapping("/login-with-link")
    public String showTokenLoginPage(
            Authentication authentication,
            Model model
    ) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }
        model.addAttribute("emailReq", new EmailReqDto());
        return "login-with-link";
    }

    @PostMapping("/auth/send-login-token")
    public String sendLoginToken(
            @Validated(ValidationSequence.class) @ModelAttribute("emailReq") EmailReqDto emailReqDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "login-with-link";
        }

        String email = emailReqDto.getEmail();

        try {
            userTokenService.sendUserTokenLink(email, UserTokenType.LOGIN);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Please check your email inbox");
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
        return "redirect:/login-with-link";
    }

    @PostMapping("/auth/send-verify-token")
    public String sendVerifyToken(
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "redirect:/login";
        }
        String email = authentication.getName();

        try {
            userTokenService.sendUserTokenLink(email, UserTokenType.VERIFY);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Please check your email inbox");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/home";
    }

    @GetMapping("/auth/verify")
    public String postTokenVerify(
            @RequestParam String token,
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            UserVerifiedResDto userVerifiedResDto = userTokenService.verifyWithUserToken(token, request);
            redirectAttributes.addFlashAttribute("name", userVerifiedResDto.getName());
            redirectAttributes.addFlashAttribute("email", userVerifiedResDto.getEmail());

            if (authentication != null) {
                new SecurityContextLogoutHandler().logout(request, response, authentication);
                SecurityContextHolder.clearContext();
            }

            return "redirect:/account-verified";
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            return "redirect:/error";
        }
    }

    // OTHERS:
    @GetMapping("/account-locked")
    public String showAccountLockedPage(HttpSession session, Model model) {
        Object error = session.getAttribute("FLASH_ERROR");
        if (error != null) {
            model.addAttribute("error", error);
            session.removeAttribute("FLASH_ERROR");
        }
        return "account-locked";
    }

    @GetMapping("/account-verified")
    public String showAccountVerifiedPage() {
        return "account-verified";
    }

    @GetMapping("/access-denied")
    public String showAccessDeniedPage() {
        return "access-denied";
    }

    @GetMapping("/session-alert")
    public String showSessionAlertPage() {
        return "session-alert";
    }

    @GetMapping("/session-timeout")
    public String showSessionTimeoutPage() {
        return "session-timeout";
    }
}
