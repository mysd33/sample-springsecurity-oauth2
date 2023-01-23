package com.example.bff.app;

import java.time.Instant;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ログイン機能のコントローラクラス
 *
 */
@Slf4j
@Controller
public class LoginController {

    @Autowired
    OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @ModelAttribute
    public LoginForm setUpForm() {
        return new LoginForm();
    }

    /**
     * ログイン画面のGETメソッド用処理
     */
    @GetMapping("/login")
    public String getLogin(Model model, HttpSession session) {
        // ログイン画面へ遷移
        return "login/login";
    }

    /**
     * ログイン処理
     */
    @PostMapping("/login")
    public String postLogin(@Validated LoginForm form, BindingResult result, HttpSession session) {
        // 入力チェックエラー時
        if (result.hasErrors()) {
            return "login/login";
        }
        // 入力チェックが問題なければSpringSecurityのログイン処理へ転送
        return "forward:/authenticate";
    }

    /**
     * ログイン成功後のメニュー画面遷移処理
     */
    @GetMapping("/menu")
    public String menu(Model model) {
        return "menu/menu";
    }

    // ハンドラメソッドの引数でOIDC、OAuth関連データを取得する例
    @GetMapping("/menu_oauth")
    public String menu(@AuthenticationPrincipal OidcUser oidcUser, OAuth2AuthenticationToken oAuth2AuthenticationToken,
            Model model) {
        if (oidcUser != null) {
            // IDトークンの取得
            String idToken = oidcUser.getIdToken().getTokenValue();
            log.debug("[IdToken]:{}", idToken);
            // IDトークンの属性の取得
            oidcUser.getAttributes().forEach((k, v) -> log.debug("[{}]:{}", k, v));
        }
        if (oAuth2AuthenticationToken != null) {
            String registrationId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();
            String principalName = oAuth2AuthenticationToken.getName();

            OAuth2AuthorizedClient oAuth2AuthorizedClient = oAuth2AuthorizedClientService
                    .loadAuthorizedClient(registrationId, principalName);
            if (oAuth2AuthorizedClient != null) {
                // アクセストークンの取得
                OAuth2AccessToken accessToken = oAuth2AuthorizedClient.getAccessToken();
                String accessTokenValue = accessToken.getTokenValue();
                Instant accessTokenIssueAt = accessToken.getIssuedAt();
                Instant accessTokenExpiresAt = accessToken.getExpiresAt();
                log.debug("[AccessToken]:{}", accessTokenValue);
                log.debug("[AccessToken ValidPeriod]from {} to {}", accessTokenIssueAt, accessTokenExpiresAt);
                // リフレッシュトークンの取得
                OAuth2RefreshToken refreshToken = oAuth2AuthorizedClient.getRefreshToken();
                if (refreshToken != null) {
                    String refreshTokenValue = refreshToken.getTokenValue();
                    Instant refreshTokenIssueAt = refreshToken.getIssuedAt();
                    Instant refreshTokenExpiresAt = refreshToken.getExpiresAt();
                    log.debug("[RefreshToken]:{}", refreshTokenValue);
                    log.debug("[RefreshToken ValidPeriod]from {} to {}", refreshTokenIssueAt, refreshTokenExpiresAt);
                }

            }

        }

        return "menu/menu";

    }

    /**
     * 
     * OAuthログイン画面のページ遷移処理
     * 
     */
    @GetMapping("/oauth-home")
    public String oauthHome() {
        return "login/oauth-login";
    }

    /**
     * 管理者用ユーザ管理ページ遷移用処理
     */
    /*
     * @GetMapping("/admin") public String admin(Model model) { return
     * "redirect:/userList"; }
     */

}