Spring Social Showcase Security
===============================

This is a fork of the spring-social-samples/spring-social-showcase-sec project with a number of edits, replacing
Spring's spring-social-security mechanism with SocialSignIn's spring-social-security

This sample app demonstrates many of the capabilities of the Spring Social project, including:
* Connect to Facebook
* Connect to Twitter
* Sign in using Facebook
* Sign in using Twitter
* Using ProviderSignInController/SpringSocialSecurityAuthenticationFilter for provider-signin instead of SocialAuthenticationFilter
* Fine-grained provider-specific security role access - e.g ROLE_USER_TWITTER, ROLE_USER_LINKEDIN
* Using UsersConnectionRepository for local account details persistence instead of requiring custom Account management
* Using out-of-the-box controllers from SocialSignin's Spring-Social-Security for SignUp flow

To run, simply import the project into your IDE and deploy to a Servlet 2.5 or > container such as Tomcat 6 or 7.
Access the project at http://localhost:8080/spring-social-showcase

Discuss at forum.springsource.org and collaborate with the development team at jira.springframework.org/browse/SOCIAL.
