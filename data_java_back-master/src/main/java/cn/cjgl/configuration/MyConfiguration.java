package cn.cjgl.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class MyConfiguration extends WebMvcConfigurationSupport {

	@Override
	protected void addInterceptors(InterceptorRegistry registry) {
		String[] excludePathPatterns = {"/index", "/static/**"};
		registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/**").excludePathPatterns(excludePathPatterns);
		super.addInterceptors(registry);
	}

	@Override
	protected void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("static/**").addResourceLocations("classpath:/static/");
		super.addResourceHandlers(registry);
	}

}
