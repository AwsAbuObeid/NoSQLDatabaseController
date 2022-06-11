package com.nosqldb.controller.security;

import com.nosqldb.controller.DAO.ControllerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.HashSet;


/**
 * SecurityConfiguration Class is used to implement spring security
 * into the application, it uses the ControllerDao Autowired by spring
 * to get the user permissions.
 * it also sets up the permissions for the users.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	@Autowired
	ControllerDao dao;
	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(inMemoryUserDetailsManager());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/admin").hasRole("ADMIN")
				.antMatchers("/read").hasRole("USER")
				.antMatchers("/write/**").hasRole("USER")
				.and().formLogin().and().csrf().disable();
		http.httpBasic();
	}

	@Bean
	public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
		InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager(createAdmin());

		for(DBUser i : dao.getUsers())
			manager.createUser(i);

		return manager;
	}
	@Bean
	PasswordEncoder passwordEncoder() {
		//TODO change
		return NoOpPasswordEncoder.getInstance();
	}

	private UserDetails createAdmin(){
		HashSet<GrantedAuthority> x=new HashSet<>();
		x.add((GrantedAuthority) () -> "ROLE_ADMIN");
		return new User("admin","admin",x);
	}

}