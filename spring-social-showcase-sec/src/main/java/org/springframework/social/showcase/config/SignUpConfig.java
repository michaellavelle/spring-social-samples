package org.springframework.social.showcase.config;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.socialsignin.springsocial.security.signup.SpringSocialSecurityConnectionSignUp;
import org.socialsignin.springsocial.security.signup.SpringSocialSecurityProfileFactory;
import org.socialsignin.springsocial.security.signup.SpringSocialSecuritySignUpController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.connect.jdbc.JdbcUsersConnectionRepository;

@Configuration
@ComponentScan({"org.socialsignin.springsocial.security.signup"})
public class SignUpConfig {

	@Inject
	private JdbcUsersConnectionRepository usersConnectionRepository;
	
	@Inject
	private SpringSocialSecurityConnectionSignUp connectionSignUp;
	
	@PostConstruct
	public void registerConnectionSignUp()
	{
		usersConnectionRepository.setConnectionSignUp(connectionSignUp);
	}
}
