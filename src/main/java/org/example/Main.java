package org.example;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

@WebServlet(value = "/time")
public class Main extends HttpServlet {

    private TemplateEngine engine;

    @Override
    public void init() throws ServletException {
        engine = new TemplateEngine();

        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix("C:/Users/Denis/IdeaProjects/TestTemplet/templates");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    protected void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String timezoneParam = request.getParameter("timezone");
        TimeZone timeZone;
        String lastTimezone = getLastTimezoneFromCookie(request);

//        if (timezoneParam != null && !timezoneParam.isEmpty()) {
//            timezoneParam = timezoneParam.replaceAll(" ", "+").replaceAll("UTC", "GMT");
//            timeZone = TimeZone.getTimeZone(timezoneParam);
//        } else {
//            timeZone = TimeZone.getTimeZone("GMT");
//        }

        if (timezoneParam != null && !timezoneParam.isEmpty()) {
            timezoneParam = timezoneParam.replaceAll(" ", "+").replaceAll("UTC", "GMT");
            timeZone = TimeZone.getTimeZone(timezoneParam);
            saveToCookie(response, timezoneParam);
        } else if (lastTimezone != null && !lastTimezone.isEmpty()) {
            // Якщо параметр timezone в запиті не передано, але є у cookies, використовуємо його
            timeZone = TimeZone.getTimeZone(lastTimezone);
        } else {
            // Якщо немає параметра timezone у запиті та у cookies, використовуємо UTC
            timeZone = TimeZone.getTimeZone("UTC");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        dateFormat.setTimeZone(timeZone);
        String currentTime = dateFormat.format(new Date());

        response.setContentType("text/html");

        PrintWriter out = response.getWriter();

        if (timeZone != null) {
            out.println("<html><body><h2>Поточний час (" + timeZone.getID().replaceAll("GMT", "UTC") + "): " + currentTime.replaceAll("GMT", "UTC") + "</h2></body></html>");
        } else {
            out.println("<html><body><h2>Поточний час (" + timeZone.getID() + "): " + currentTime + "</h2></body></html>");
        }
        out.println("<meta http-equiv=\"refresh\" content=\"5\">");

        Map<String, String[]> parameterMap = request.getParameterMap();

        Map<String, Object> params = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> keyValue : parameterMap.entrySet()) {
            params.put(keyValue.getKey(), keyValue.getValue()[0]);
        }

        Context simpleContext = new Context(
                request.getLocale(),
                Map.of("queryParams", params)
        );

        engine.process("test", simpleContext, response.getWriter());
        response.getWriter().close();
    }

    private void saveToCookie (HttpServletResponse response, String timezone) {
        Cookie cookies = new Cookie("lastTimezone", timezone);
        cookies.setMaxAge(1000);
        response.addCookie(cookies);
    }

    private String getLastTimezoneFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("lastTimezone".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}