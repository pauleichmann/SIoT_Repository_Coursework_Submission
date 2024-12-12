package cn.cjgl.configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class LoginInterceptor implements HandlerInterceptor {
	
	private static final Logger log = LoggerFactory.getLogger(LoginInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		//log.debug("---------------------Start request address blocking----------------------------");
		//HttpSession session = request.getSession();
		//User user = (User)session.getAttribute("s_user");
		log.info("RequestURI : "+request.getRequestURI());
		//log.debug("---------------------End request address blocking----------------------------");
		return HandlerInterceptor.super.preHandle(request, response, handler);
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		//log.debug("--------------Processing operations before view rendering after request handling is complete---------------");
		HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		 //log.debug("---------------Operations after view rendering-------------------------");
		HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}

}
