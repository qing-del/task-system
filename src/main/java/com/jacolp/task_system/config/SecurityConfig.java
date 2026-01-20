package com.jacolp.task_system.config;

import com.jacolp.task_system.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity  // 开启 Spring Security
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService; // 这里我们需要注入这个服务

    // 构造器注入（推荐写法，比 @Autowired 更安全）
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * 1. 配置安全过滤器链 (SecurityFilterChain)
     * 这是 Spring Security 的核心，决定了哪些请求要拦截，哪些放行。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 前端带 Token 的时候处理一下浏览器偷偷发的的 OPTIONS
                .cors(Customizer.withDefaults())

                // 禁用 CSRF (因为我们是前后端分离，使用 Token 认证，不需要 CSRF 防护)
                .csrf(csrf -> csrf.disable())

                // 配置请求白名单
                .authorizeHttpRequests(auth -> auth
                        // 允许所有人访问登录和注册接口 (注意：这里要根据你的 Controller 路径来填)
                        .requestMatchers("/auth/**").permitAll()
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )

                // 设置 Session 管理策略：无状态 (Stateless)
                // 因为我们用 JWT，服务器不需要保存 Session，这能大幅节省服务器内存
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 指定认证提供者
                .authenticationProvider(authenticationProvider())

                // 核心：把我们的 JWT 过滤器加在 UsernamePasswordAuthenticationFilter 之前
                // 意思是：先检查 Token，如果没有 Token，再走原本的用户名密码逻辑
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 定义具体的跨域规则
     * @return 返回设置好的规则
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的前端域名（测试先用 *）
        configuration.setAllowedOrigins(List.of("*"));

        // 允许的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 允许的请求头（这一步很重要，否则 Authorization 头会被拦截）
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // 注册配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 2. 配置认证提供者 (AuthenticationProvider)
     * 告诉 Spring Security 去哪里找用户，以及怎么校验密码
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // 告诉它去哪里查用户
        authProvider.setPasswordEncoder(passwordEncoder()); // 告诉它密码是用什么加密的
        return authProvider;
    }

    /**
     * 3. 配置密码加密器
     * 使用 BCrypt 算法
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 4. 配置认证管理器 (AuthenticationManager)
     * 我们在登录接口里需要手动调用它来验证用户名密码
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}