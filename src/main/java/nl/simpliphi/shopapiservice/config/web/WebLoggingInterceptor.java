package nl.simpliphi.shopapiservice.config.web;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.TextStringBuilder;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class WebLoggingInterceptor implements AsyncHandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    logRequest(request);
    return true;
  }

  private void logRequest(HttpServletRequest request) {
    TextStringBuilder stringBuilder = new TextStringBuilder();
    stringBuilder.append("<-? | Incoming Request: %s %s %s %s",
            request.getMethod(),
            request.getRequestURI(),
            request.getQueryString(),
            request.getRemoteUser());
    log.info(stringBuilder.toString());
  }

}
