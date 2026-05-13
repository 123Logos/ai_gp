package com.aigp.demo.web.security;

import com.aigp.demo.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 将 {@link JwtAuthenticationFilter} 写入请求的 {@link JwtUserClaims} 绑定到控制器参数。
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterAnnotation(CurrentUser.class) != null
				&& parameter.getParameterType().equals(JwtUserClaims.class);
	}

	@Override
	public Object resolveArgument(
			MethodParameter parameter,
			ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) {
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		if (request == null) {
			throw new UnauthorizedException("无法解析当前请求");
		}
		Object raw = request.getAttribute(JwtAuthenticationFilter.REQUEST_ATTR_CLAIMS);
		if (!(raw instanceof JwtUserClaims claims)) {
			throw new UnauthorizedException("未认证");
		}
		return claims;
	}
}
