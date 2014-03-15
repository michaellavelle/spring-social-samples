package org.springframework.social.showcase.config;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.socialsignin.springsocial.security.signup.SpringSocialSecurityConnectionSignUp;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.connect.jdbc.JdbcUsersConnectionRepository;

@Configuration
@ComponentScan({"org.socialsignin.springsocial.security.signup"})
public class SocialSignUpConfig {

	@Inject
	private JdbcUsersConnectionRepository usersConnectionRepository;
	
	@Inject
	private SpringSocialSecurityConnectionSignUp connectionSignUp;
	
	@PostConstruct
	public void registerConnectionSignUp()
	{
		// Optionally register implicit sign up - allows an account to be
		// created in the event that sufficient details can be obtained
		// from social provider.  Otherwise the user will be taken to an
		// explicit sign up form.
		// Can be removed if implicit sign up is not required
		usersConnectionRepository.setConnectionSignUp(connectionSignUp);
	}
}
