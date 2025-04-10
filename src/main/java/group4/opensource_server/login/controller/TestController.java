package group4.opensource_server.login.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class TestController {

    @Value("${kakao.api_key}")
    private String kakaoApiKey;

    @Value("${kakao.redirect_uri}")
    private String kakaoRedirectUri;

    @Value("${apple.client_id}")
    private String appleClientId;

    @GetMapping("/")
    public String loginPage(Model model) {
        model.addAttribute("kakaoApiKey", kakaoApiKey);
        model.addAttribute("kakaoRedirectUri", kakaoRedirectUri);
        model.addAttribute("appleClientId", appleClientId);
        return "index";
    }

    @GetMapping("/auth/login/kakao/callback")
    public String kakaoCallback(@RequestParam String code, Model model) {
        model.addAttribute("authCode", code);
        return "kakaoCallback";  // templates/kakaoCallback.html
    }


    @GetMapping("/auth/login/apple/callback")
    public String appleCallback(Model model, @RequestParam String code) {
        model.addAttribute("authCode", code);
        return "appleCallback"; // templates/appleCallback.html
    }
}