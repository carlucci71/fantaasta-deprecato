package com.daniele.asta;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Order(0)
@EnableWebSecurity
@Profile(value = "protetto")
public class BasicAuthConfigurationProtetto 
  extends WebSecurityConfigurerAdapter {

	@Value("${security.user.name}")
	private String nomeLega;
	
    @Override
    protected void configure(AuthenticationManagerBuilder auth)
      throws Exception {
		auth
          .inMemoryAuthentication()
          .withUser(nomeLega)
          .password("pwd")
          .roles("USER");
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
    	 httpSecurity
         .authorizeRequests()
             .antMatchers("/**").hasRole("USER")
//         .antMatchers("/login.html", "/loginAPI").permitAll().anyRequest()
//         .authenticated()
          .and()
          	 .formLogin()
             .loginPage("/login.html")
             .usernameParameter("nomeLega")
             .passwordParameter("password")
             .loginProcessingUrl("/loginAPI")
             .defaultSuccessUrl("/index.html")
             .failureUrl("/login.html")
             .permitAll()
          .and()
          	  .csrf()
              .disable();             
    }}