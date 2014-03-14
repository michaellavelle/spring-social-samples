/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.social.showcase.config;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.socialsignin.springsocial.security.signin.SpringSocialSecurityAccessDeniedHandler;
import org.socialsignin.springsocial.security.signin.SpringSocialSecurityAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.social.UserIdSource;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.security.AuthenticationNameUserIdSource;
import org.springframework.social.security.SocialAuthenticationProvider;
import org.springframework.social.security.SocialUserDetailsService;
import org.springframework.social.security.SpringSocialConfigurer;
  
/**
 * Security Configuration.
 * @author Craig Walls
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter{

	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	@Qualifier(value="springSocialSecurityUserDetailsService")
	private UserDetailsService userDetailsService;
	
	@Autowired
	private SpringSocialSecurityAuthenticationFilter springSocialSecurityAuthenticationFilter;
	
	@Autowired
	private SpringSocialSecurityAccessDeniedHandler accessDeniedHandler;

	
	@Autowired
	public void registerAuthentication(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService);
	}
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		web
			.ignoring()
				.antMatchers("/resources/**");
	}
	
	@Bean
	public SocialAuthenticationProvider socialAuthenticationProvider(UsersConnectionRepository usersConnectionRepository,SocialUserDetailsService socialUserDetailsService)
	{
		return new SocialAuthenticationProvider(usersConnectionRepository,socialUserDetailsService);
	}
	
	@Bean
	public AuthenticationManager authenticationManager(UsersConnectionRepository usersConnectionRepository,SocialUserDetailsService socialUserDetailsService)
	{
		List<AuthenticationProvider> authProviders = new ArrayList<AuthenticationProvider>();
		authProviders.add(socialAuthenticationProvider(usersConnectionRepository,socialUserDetailsService));
		return new ProviderManager(authProviders);
	}
	
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		boolean allowFormLoginAsWellAsSocialLogin = true;
		
		// Form login can be optionally provided which allows admin users to login and
		// would allow social users the opportunity to login with username/password
		// in the even they disconnected from all social providers
		if (allowFormLoginAsWellAsSocialLogin)
		{
			http.formLogin()
			.loginPage("/signin")
			.loginProcessingUrl("/signin/authenticate")
			.failureUrl("/signin?param.error=bad_credentials");
		}
		else
		{
			http.exceptionHandling().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/signin"));
		}
		
		http
		.exceptionHandling().accessDeniedHandler(accessDeniedHandler);

		http.addFilterAfter(springSocialSecurityAuthenticationFilter,AbstractPreAuthenticatedProcessingFilter.class);
			http
				.logout()
					.logoutUrl("/signout")
					.deleteCookies("JSESSIONID")
			.and()
				.authorizeRequests()
				.antMatchers("/twitter/**").hasRole("USER_TWITTER")
				.antMatchers("/facebook/**").hasRole("USER_FACEBOOK")
				.antMatchers("/linkedin/**").hasRole("USER_LINKEDIN")
				.antMatchers("/soundcloud/**").hasRole("USER_SOUNDCLOUD")

					.antMatchers("/admin/**", "/favicon.ico", "/resources/**", "/auth/**", "/signin/**","/signinOrConnect/**", "/signup/**", "/disconnect/facebook").permitAll()
					.antMatchers("/**").authenticated()
			.and()
				.rememberMe()
			.and()
				.apply(new SpringSocialConfigurer());
	}
	
	@Bean
	public UserIdSource userIdSource() {
		return new AuthenticationNameUserIdSource();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}

	@Bean
	public TextEncryptor textEncryptor() {
		return Encryptors.noOpText();
	}

}
