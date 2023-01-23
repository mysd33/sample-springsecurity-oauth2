package com.example.bff;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
public class SecurityConfig {
    // Spring Securityのデバッグモード
    @Value("${spring.websecurity.debug:false}")
    boolean webSecurityDebug;
    
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // @formatter:off
            // フォーム認証によるログイン処理
        http.formLogin(login -> login.loginProcessingUrl("/authenticate") // ログイン処理のパス
                .loginPage("/login") // ログインページの指定
                .failureUrl("/login?error") // ログイン失敗時の遷移先
                .usernameParameter("userId") // ログインページのユーザーID
                .passwordParameter("password") // ログインページのパスワード
                .defaultSuccessUrl("/menu", true) // ログイン成功後の遷移先
                .permitAll())
            // OAuth認証によるログイン処理
            .oauth2Login(login -> login.loginPage("/oauth-home")  // ログインページの指定
                    .defaultSuccessUrl("/menu", true))  // ログイン成功後の遷移先
                // ログアウト処理
                .logout(logout -> logout.logoutUrl("/logout") // ログアウトのURL
                        .logoutSuccessUrl("/")) // ログアウト成功後のURL
                // 認可設定
                .authorizeHttpRequests(authz -> authz.antMatchers("/webjars/**").permitAll() // webjarsへアクセス許可
                        .antMatchers("/css/**").permitAll()// cssへアクセス許可
                        .antMatchers("/js/**").permitAll()// jsへアクセス許可
                        .antMatchers("/login").permitAll() // ログインページは直リンクOK
                        .antMatchers("/actuator/**").permitAll() // actuatorのAPIへアクセス許可
                        .antMatchers("/v3/api-docs/**").permitAll() // Springdoc-openapiのドキュメントへのアクセス許可
                        .antMatchers("/v3/api-docs*").permitAll() // Springdoc-openapiのドキュメントへのアクセス許可
                        .antMatchers("/swagger-ui/**").permitAll() // Springdoc-openapiのドキュメントへのアクセス許可
                        .antMatchers("/swagger-ui.html").permitAll() // Springdoc-openapiのドキュメントへのアクセス許可
                        .antMatchers("/api/**").permitAll()// REST APIへアクセス許可
                        .antMatchers("/admin").hasAuthority("ROLE_ADMIN") // ユーザ管理画面は管理者ユーザーのみ許可
                        .antMatchers("/user*").hasAuthority("ROLE_ADMIN") // ユーザ管理画面は管理者ユーザーのみ許可
                        
                        .antMatchers("/oauth-home").permitAll() // OAuthログイン用のページへのアクセス許可
                        
                        .anyRequest().authenticated() // それ以外は認証・認可が必要
                )
           
                // REST APIはCSRF保護不要
                .csrf().ignoringAntMatchers("/api/**");
        
        
        // 
		/*http.csrf()
			.and()
			.authorizeHttpRequests(authz -> 
				authz.mvcMatchers("/").permitAll()
				.anyRequest().authenticated())
			.oauth2Login()
			.and()
			.logout()
			.logoutSuccessUrl("/");*/
			
		return http.build();
	}

    /**
     * H2 Consoleのアクセス許可対応
     */
    @Profile("dev")
    @Order(1)
    @Bean
    public SecurityFilterChain securityFilterChainForH2Console(HttpSecurity http) throws Exception {
        // @formatter:off
        //H2 ConsoleのURLに対して
        http.antMatcher("/h2-console/**")
            .authorizeHttpRequests(
                // 認証不要でアクセス許可
                authz -> authz.anyRequest().permitAll())
            // CSRF保護不要         
            .csrf().disable()
            // H2 Consoleの表示ではframeタグを使用しているのでX-FrameOptionsを無効化
            .headers().frameOptions().disable();
        // @formatter:on        
        return http.build();
    }
    
    /**
     * パスワードエンコーダ
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}