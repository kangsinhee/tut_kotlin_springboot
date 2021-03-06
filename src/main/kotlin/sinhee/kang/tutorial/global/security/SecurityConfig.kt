package sinhee.kang.tutorial.global.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.web.cors.CorsUtils
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

import sinhee.kang.tutorial.global.security.errorHandler.ExceptionHandlerConfigurer
import sinhee.kang.tutorial.global.security.jwt.JwtConfigurer
import sinhee.kang.tutorial.global.security.jwt.JwtTokenProvider
import sinhee.kang.tutorial.global.security.requestLog.RequestLogConfigurer
import sinhee.kang.tutorial.infra.api.slack.service.SlackReportService

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val slackReportService: SlackReportService
): WebSecurityConfigurerAdapter(), WebMvcConfigurer {

    override fun configure(http: HttpSecurity) {
        http
            .csrf()
                .disable()
            .cors().and()
            .formLogin()
                .disable()
            .headers()
            .frameOptions()
                .disable().and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests()
                .requestMatchers(RequestMatcher { CorsUtils.isPreFlightRequest(it) }).permitAll()
                .antMatchers("/swagger-ui.html").permitAll()
                .antMatchers("/auth").permitAll()
                .antMatchers("/posts").permitAll()
                .antMatchers("/users").permitAll()
                .antMatchers("/users/password").permitAll()
                .antMatchers("/users/email/password/verify").permitAll()
                .antMatchers("/users/email/verify").permitAll().and()
            .apply(JwtConfigurer(jwtTokenProvider)).and()
            .apply(ExceptionHandlerConfigurer(slackReportService)).and()
            .apply(RequestLogConfigurer())
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
    }

    @Bean
    override fun authenticationManager(): AuthenticationManager =
        super.authenticationManagerBean()

    @Bean
    fun passwordEncoder(): PasswordEncoder =
        BCryptPasswordEncoder()
}
