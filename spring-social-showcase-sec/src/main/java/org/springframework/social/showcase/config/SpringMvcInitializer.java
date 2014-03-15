package org.springframework.social.showcase.config;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.social.lastfm.pseudooauth2.connect.web.LastFmPseudoOAuth2Filter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class SpringMvcInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	private String contextPath;
	
	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class<?>[] { MainConfig.class, SecurityConfig.class, SocialConfig.class, SocialSignUpConfig.class,WebMvcConfig.class };
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class<?>[] {};
	}
	
	@Override
	public void onStartup(ServletContext servletContext)
			throws ServletException {
		contextPath = servletContext.getContextPath();
		super.onStartup(servletContext);
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}
	
	@Override
	protected Filter[] getServletFilters() {
		CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
		encodingFilter.setEncoding("UTF-8");
		encodingFilter.setForceEncoding(true);
		LastFmPseudoOAuth2Filter lastFmFilter = new LastFmPseudoOAuth2Filter();
		lastFmFilter.setSigninCallbackPath(contextPath + "/signin/lastfm");
		lastFmFilter.setConnectCallbackPath(contextPath + "/connect/lastfm");

		return new Filter[] { encodingFilter,lastFmFilter };
	}

	
	
}
