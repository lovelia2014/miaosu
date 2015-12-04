package com.miaosu.flux.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

/**
 * Application Security
 * Created by angus on 15/6/13.
 */
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class ApplicationSecurity extends WebSecurityConfigurerAdapter {

    @Autowired
    private SecurityProperties security;

    @Value("${remember-me.token-validity-seconds}")
    private int tokenValiditySeconds;

    @Value("${remember-me.internal-secret-key}")
    private String internalSecretKey = "internalSecretKey";

    @Value("${remember-me.cookie-name}")
    private String cookieName = "REMEMBER_ME_COOKIE";

    @Value("${remember-me.parameter}")
    private String parameter = "remember_me";

    @Autowired
    private UserDetailsManager userDetailsManager;

    @Autowired
    private PersistentTokenRepository persistentTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public RememberMeServices rememberMeServices(String internalSecretKey) {
        PersistentTokenBasedRememberMeServices rememberMeServices = new PersistentTokenBasedRememberMeServices(
                internalSecretKey, userDetailsManager, persistentTokenRepository);
        // rememberMeServices.setAlwaysRemember(true);
        rememberMeServices.setTokenValiditySeconds(tokenValiditySeconds);
        rememberMeServices.setCookieName(cookieName);
        rememberMeServices.setParameter(parameter);
        return rememberMeServices;
    }

    protected void configure(HttpSecurity http) throws Exception {
//        http.csrf().disable().authorizeRequests().antMatchers("/css/**").permitAll().antMatchers("/favicon.gif")
//                .permitAll().antMatchers("/jquery/**").permitAll().antMatchers("/data/**").permitAll()
//                .antMatchers("/images/**").permitAll().anyRequest().fullyAuthenticated().and().formLogin()
//                .loginPage("/login").failureUrl("/login?error").permitAll().and().logout().permitAll();

        http.csrf().disable().authorizeRequests()
                .antMatchers("/").fullyAuthenticated()
                .antMatchers("/index").fullyAuthenticated()
                .antMatchers("/api/**").fullyAuthenticated()
                .antMatchers("/**").permitAll()
                .and().formLogin().loginPage("/login").failureUrl("/login?error").permitAll()
                .and().logout().permitAll();

        http.rememberMe().rememberMeServices(rememberMeServices(internalSecretKey)).key(internalSecretKey);
    }

    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsManager).passwordEncoder(passwordEncoder);
//         auth.jdbcAuthentication().dataSource(this.dataSource).passwordEncoder(passwordEncoder).getUserDetailsService().setEnableGroups(true);
    }

}