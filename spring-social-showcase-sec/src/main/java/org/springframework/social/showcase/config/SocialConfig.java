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

import javax.inject.Inject;
import javax.sql.DataSource;

import org.socialsignin.springsocial.security.connect.SpringSocialSecurityConnectionFactory;
import org.socialsignin.springsocial.security.signin.SpringSocialSecurityAuthenticationFactory;
import org.socialsignin.springsocial.security.signin.SpringSocialSecuritySignInService;
import org.socialsignin.springsocial.security.signup.ConnectionRepositorySignUpService;
import org.socialsignin.springsocial.security.signup.SignUpService;
import org.socialsignin.springsocial.security.web.CustomCallbackUrlConnectController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurerAdapter;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.jdbc.JdbcUsersConnectionRepository;
import org.springframework.social.connect.web.ConnectController;
import org.springframework.social.connect.web.ConnectInterceptor;
import org.springframework.social.connect.web.ProviderSignInController;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.facebook.web.DisconnectController;
import org.springframework.social.linkedin.api.LinkedIn;
import org.springframework.social.linkedin.connect.LinkedInConnectionFactory;
import org.springframework.social.showcase.facebook.PostToWallAfterConnectInterceptor;
import org.springframework.social.showcase.facebook.SpringSocialSecurityFacebookConnectInterceptor;
import org.springframework.social.showcase.linkedin.SpringSocialSecurityLinkedInConnectInterceptor;
import org.springframework.social.showcase.soundcloud.SpringSocialSecuritySoundCloudConnectInterceptor;
import org.springframework.social.showcase.twitter.SpringSocialSecurityTwitterConnectInterceptor;
import org.springframework.social.showcase.twitter.TweetAfterConnectInterceptor;
import org.springframework.social.soundcloud.api.SoundCloud;
import org.springframework.social.soundcloud.connect.SoundCloudConnectionFactory;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;

/**
 * Spring Social Configuration. This configuration is demonstrating the use of
 * the simplified Spring Social configuration options from Spring Social 1.1.
 * 
 * @author Craig Walls
 */
@Configuration
@EnableSocial
@ComponentScan({ "org.socialsignin.springsocial.security.signin",
		"org.socialsignin.springsocial.security.userdetails",
		"org.socialsignin.springsocial.security.userauthorities",
		"org.socialsignin.springsocial.security.userauthorities",
		"org.socialsignin.springsocial.security.web"})
public class SocialConfig extends SocialConfigurerAdapter {

	@Inject
	private DataSource dataSource;

	@Inject
	private SpringSocialSecuritySignInService springSocialSecuritySignInService;

	private JdbcUsersConnectionRepository usersConnectionRepository;

	@Override
	public void addConnectionFactories(ConnectionFactoryConfigurer cfConfig,
			Environment env) {
		cfConfig.addConnectionFactory(new TwitterConnectionFactory(env
				.getProperty("twitter.consumerKey"), env
				.getProperty("twitter.consumerSecret")));
		cfConfig.addConnectionFactory(new FacebookConnectionFactory(env
				.getProperty("facebook.clientId"), env
				.getProperty("facebook.clientSecret")));
		cfConfig.addConnectionFactory(new LinkedInConnectionFactory(env
				.getProperty("linkedin.consumerKey"), env
				.getProperty("linkedin.consumerSecret")));
		
		cfConfig.addConnectionFactory(new SoundCloudConnectionFactory(env
				.getProperty("soundcloud.consumerKey"), env
				.getProperty("soundcloud.consumerSecret"),env
				.getProperty("soundcloud.redirectUri")));
		
		// We are using the UsersConnectionRepository to store local user details
		// as ConnectionData for the SpringSocialSecurity pseudo provider.
		cfConfig.addConnectionFactory(new SpringSocialSecurityConnectionFactory());

	}

	@Override
	public UserIdSource getUserIdSource() {
		return new UserIdSource() {
			@Override
			public String getUserId() {
				Authentication authentication = SecurityContextHolder
						.getContext().getAuthentication();
				if (authentication == null) {
					throw new IllegalStateException(
							"Unable to get a ConnectionRepository: no user signed in");
				}
				return authentication.getName();
			}
		};
	}

	@Override
	public UsersConnectionRepository getUsersConnectionRepository(
			ConnectionFactoryLocator connectionFactoryLocator) {
		usersConnectionRepository = new JdbcUsersConnectionRepository(
				dataSource, connectionFactoryLocator, Encryptors.noOpText());

		return usersConnectionRepository;
	}

	@Bean
	public ConnectController connectController(
			ConnectionFactoryLocator connectionFactoryLocator,
			ConnectionRepository connectionRepository) {
		
		// Register a CustomCallbackUrlConnectController here instead of a regular ConnectController to
		// support the use case where we require a callback url other than "/connect" for a given provider.
		
		// In this case we require the controller to support soundcloud callbacks which must be
		// a single url per application.  We use the ProviderSignInOrConnectController from spring-social-security
		// to support a single callback of "/signinOrConnect" which we register for SoundCloud.
		
		// The main ConnectController hard codes the callback to be "/connect" so we must
		// override this value for SoundCloud via this custom controller
		CustomCallbackUrlConnectController connectController = new CustomCallbackUrlConnectController(
				connectionFactoryLocator, connectionRepository);
		connectController.setOverriddenConnectCallbackBasePath("soundcloud", "/signinOrConnect");
		
		
		// Add connect interceptors for all our providers to prevent multiple users
		// sharing the same 3rd party provider account and also to maintain assigned
		// security roles appropriately on connection
		connectController
				.addInterceptor(springSocialSecurityTwitterConnectInterceptor());
		connectController
				.addInterceptor(springSocialSecurityFacebookConnectInterceptor());
		connectController
				.addInterceptor(springSocialSecurityLinkedInConnectInterceptor());
		connectController
				.addInterceptor(springSocialSecuritySoundCloudConnectInterceptor());
		
		
		connectController
				.addInterceptor(new PostToWallAfterConnectInterceptor());
		connectController.addInterceptor(new TweetAfterConnectInterceptor());
		return connectController;
	}

	/** Create a SpringSocialSecurityConnectInterceptor for Twitter.  This prevents multiple users connecting to
	 * the same Twitter account and also updates the users assigned security roles when connecting with Twitter*/
	@Bean
	public ConnectInterceptor<Twitter> springSocialSecurityTwitterConnectInterceptor() {
		return new SpringSocialSecurityTwitterConnectInterceptor();
	}

	/** Create a SpringSocialSecurityConnectInterceptor for Facebook.  This prevents multiple users connecting to
	 * the same Facebook account and also updates the users assigned security roles when connecting with Facebook*/
	@Bean
	public ConnectInterceptor<Facebook> springSocialSecurityFacebookConnectInterceptor() {
		return new SpringSocialSecurityFacebookConnectInterceptor();
	}

	/** Create a SpringSocialSecurityConnectInterceptor for LinkedIn.  This prevents multiple users connecting to
	 * the same LinkedIn account and also updates the users assigned security roles when connecting with LinkedIn*/
	@Bean
	public ConnectInterceptor<LinkedIn> springSocialSecurityLinkedInConnectInterceptor() {
		return new SpringSocialSecurityLinkedInConnectInterceptor();
	}
	
	/** Create a SpringSocialSecurityConnectInterceptor for LinkedIn.  This prevents multiple users connecting to
	 * the same LinkedIn account and also updates the users assigned security roles when connecting with LinkedIn*/
	@Bean
	public ConnectInterceptor<SoundCloud> springSocialSecuritySoundCloudConnectInterceptor() {
		return new SpringSocialSecuritySoundCloudConnectInterceptor();
	}

	@Bean
	public ProviderSignInController providerSignInController(
			ConnectionFactoryLocator connectionFactoryLocator,
			UsersConnectionRepository connectionRepository) {
		ProviderSignInController providerSignInController = new ProviderSignInController(
				connectionFactoryLocator, connectionRepository,
				springSocialSecuritySignInService);
		providerSignInController.setPostSignInUrl("/authenticate");
		return providerSignInController;
	}

	@Bean
	public DisconnectController disconnectController(
			UsersConnectionRepository usersConnectionRepository,
			Environment environment) {
		return new DisconnectController(usersConnectionRepository,
				environment.getProperty("facebook.clientSecret"));
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
	public Facebook facebook(ConnectionRepository repository) {
		Connection<Facebook> connection = repository
				.findPrimaryConnection(Facebook.class);
		return connection != null ? connection.getApi() : null;
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
	public Twitter twitter(ConnectionRepository repository) {
		Connection<Twitter> connection = repository
				.findPrimaryConnection(Twitter.class);
		return connection != null ? connection.getApi() : null;
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
	public LinkedIn linkedin(ConnectionRepository repository) {
		Connection<LinkedIn> connection = repository
				.findPrimaryConnection(LinkedIn.class);
		return connection != null ? connection.getApi() : null;
	}
	
	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
	public SoundCloud soundcloud(ConnectionRepository repository) {
		Connection<SoundCloud> connection = repository
				.findPrimaryConnection(SoundCloud.class);
		return connection != null ? connection.getApi() : null;
	}


}
