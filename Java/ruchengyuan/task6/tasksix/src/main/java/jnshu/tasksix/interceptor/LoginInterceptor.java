package jnshu.tasksix.interceptor;

import jnshu.tasksix.model.Student;
import jnshu.tasksix.service.StudentService;
import jnshu.tasksix.util.DesUtils;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by Administrator on 2017-10-03.
 */

public class LoginInterceptor extends HandlerInterceptorAdapter {

    private static Logger loggerLI = LoggerFactory.getLogger(LoginInterceptor.class);

    @Autowired
    private StudentService studyService;
    //处理器执行之前处理

    @Override
    public boolean  preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)throws Exception{

        loggerLI.info("拦截器preHandle开始，处理器执行之前处理");

        //将网页url存入session
        String lastUrl = request.getRequestURI();
        HttpSession session = request.getSession();
        session.setAttribute("lastUrl",lastUrl);
        session.setMaxInactiveInterval(-1);

        String DES_KEY = "12345678";
        Cookie[] cookies = request.getCookies();
//        loggerLI.info("cookie数量: " + cookies.length);
        for(int i = 0;i<cookies.length;i++) {

            //找到名为token的cookie
            if ("token".equals(cookies[i].getName()) && (cookies[i].getValue() != null )) {
/*
//                loggerLI.info("Cookie-name: " + cookies[i].getName() +
//                        "    Cookie-MaxAge: " + cookies[i].getMaxAge() +
//                        "    Cookie-Value: " + cookies[i].getValue()+ "\n");
                 //将value进行分割
                String[] results = cookies[i].getValue().split(":");
//                loggerLI.info("results： "+ results[0] + "   " +results[1]);
                //对value进行base64逆编码
                byte[] str = Base64.decodeBase64(results[0]);

                //将str用DES进行解密
                DesUtils desUtils = new DesUtils();
//                loggerLI.info(new String(desUtils.decrypt(str,DES_KEY)));
                Integer id = Integer.valueOf(new String(desUtils.decrypt(str,DES_KEY)));

                loggerLI.info("拦截器中cookie的id： "+ id);
                    Student student =new Student();
                    student.setId(id);
                    Integer signal = studyService.countStudentUser(student);
//                    if((selectId == id)&&((request.getRequestURL().toString()).equals(request.getContextPath()+"/a/login"))){
//                        response.sendRedirect(request.getContextPath()+"/a/home");
//                        return false;
//                    }
                     if(signal != 0){
                         session.setAttribute("lastUrl",null);
                        return true;
                    }
                    else{
                    loggerLI.error("没有名为： "+ id +"的id");
                    response.sendRedirect(request.getContextPath()+"/a/login");
                    return false;
                }
*/
                return true;

            }
        }
//        loggerLI.info("没用名为token的cookie");
        response.sendRedirect(request.getContextPath()+"/a/login");
        return false;
    }



    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        loggerLI.info("postHandle，处理器执行完毕之后");
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        loggerLI.info("拦截器afterCompletion开始，请求处理完成之后处理");
    }

}
