package com.task.controller;

import com.task.exception.MyException;
import com.task.models.User;
import com.task.service.UserService;
import com.task.utils.CookieUtil;
import com.task.utils.DESUtil;
import com.task.utils.EncryptionUtil;
import com.task.utils.RegexUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * 关于用户的功能在此controller中
 * 注册，登陆，修改密码，登出
 */
@Controller
public class UserController {
    @Autowired
    private UserService userService;
    private Logger logger = Logger.getLogger(UserController.class);

    /**
     *登陆
     * @param username 用户输入的用户名
     * @param password 用户输入的密码
     * @return 返回对应的页面
     * @throws Exception 程序出现的异常
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String doLogin(@RequestParam String username, String password, HttpServletResponse response) throws Exception {
        DESUtil desUtil=new DESUtil();
//        try {
            //按照username将数据库中对应的数据取出来
            User user = userService.listByName(username);
            if (null==user){
                throw new MyException("用户不存在");
            }
            //hashKey1为存在数据库中的hashKey
            String hashKey1=user.getHashKey();
            String salt=user.getSalt();
            String str=password+salt;
            //hashKey2为输入密码结合数据库中盐重新加密得到的hashKey
            String hashKey2= EncryptionUtil.getSHA256Str(str);
            //将输入的密码得到的hashKey与之前存储的hashKey比对，正确就跳转到主页
            if (hashKey1.equals(hashKey2)) {
                long currentTime=System.currentTimeMillis();
                user.setLoginTime(currentTime);
                userService.updateLoginTime(user);//这时候其实只有username和loginTime起作用
                String strLogin=desUtil.encryptFromLong(currentTime);
                CookieUtil.addCookie(response,"lid",strLogin);
                String uid=desUtil.encrypt(username);
                CookieUtil.addCookie(response,"uid",uid);
               CookieUtil.addCookie(response,"username",username);
                return "redirect:/homepage";
            } else
                //错误就跳转到错误页面
            throw new MyException("输入密码不正确");
    }

    //帮助login 页面跳转到注册页面
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String goRegister(){
        return "page05";
    }

    /**
     *注册信息，在此页面将成功的账户信息存入数据库并直接跳转至主页
     * @param username 用户注册的用户名
     * @param password 用户注册的密码
     * @param repassword 用户重复输入的密码
     * @return 按照输入的用户名和密码确定注册是否成功返回到
     * @throws Exception 程序出现的异常
     */
    @RequestMapping(value = "/doregister", method = RequestMethod.POST)
    public String doregister(@RequestParam String username, String password, String repassword,HttpServletResponse response) throws Exception {
        DESUtil desUtil=new DESUtil();
        User user;
            if ((!password.equals(repassword)) || !RegexUtil.usernameRegex(username)||!RegexUtil.passwordRegex(password)) {
                //如果两次密码输入不一致，或者输入密码为空则跳转到errorRigister页面
                throw new MyException("用户名，密码有误");
            } else {
                //因为设置了loginTime默认值是0，所以这里不需要带上loginTime
              try{
                  user = new User(System.currentTimeMillis(), username, password);
                userService.justAdd(user);
              }
                catch (Exception e){
            //用户名重复则跳转到errorName页面
           throw new MyException("注册信息重复");
                }
            }
        //没有报错就直接登陆首页
        long currentTime=System.currentTimeMillis();
        user.setLoginTime(currentTime);
        userService.updateLoginTime(user);//这时候其实只有username和loginTime起作用
        String strLogin=desUtil.encryptFromLong(currentTime);//通过DES对象调用加密方法
        CookieUtil.addCookie(response,"lid",strLogin);//存入cookie中
        String uid=desUtil.encrypt(username);//通过DES对象调用加密方法
        CookieUtil.addCookie(response,"uid",uid);//存入cookie中
        CookieUtil.addCookie(response,"username",username);//存入cookie中
        return "redirect:/homepage";
    }


    //修改密码,综合了登陆，注册和调用cookie的知识点
    @RequestMapping(value = "changePassword",method = RequestMethod.POST)
    public ModelAndView goPassword(@RequestParam String oldpassword, String password, String repassword, HttpServletResponse response, HttpServletRequest request)throws Exception{
        ModelAndView mav=new ModelAndView();

            //将cookie中的用户id取出来当作username
            String username=CookieUtil.getCookieValue(request,"username");
//            if(null==username){
//                throw new MyException("请先登陆再修改密码");
//            }通过权限解决
            //使用此用户名查找原密码
            User user=userService.listByName(username);
            String salt1=user.getSalt();
            String hashKey1=user.getHashKey();
            String str1=oldpassword+salt1;
            String hashKey2=EncryptionUtil.getSHA256Str(str1);
            //确定旧密码是否正确
            if (hashKey1.equals(hashKey2)){
                //原密码正确，判断两个新密码是否相等,且密码是否符合规范
                if(password.equals(repassword)&&RegexUtil.passwordRegex(password)){
                    //两个新密码相等，并且符合规范，将其储存并修改
                    user.setPassword(password);
                    user.setUpdatedAt(System.currentTimeMillis());
                    userService.justUpdate(user);
                    logger.info(username+"修改密码为"+password);
                    //因为cookie中只储存了未转化的username，所以此时不需要再对cookie进行操作
                    mav.setViewName("redirect:homepage");
                    return mav;
                }
               else {
                    //两次新密码不相等
                    throw  new MyException("请确认两次新密码必须相等且符合规范");
                }
            }else {
                //原密码不正确
                throw new MyException("原密码错误，请确认后重新输入");
            }
    }

    //登出功能
    @RequestMapping(value = "/signout", method = RequestMethod.GET)
    public String signOut(HttpServletResponse response,HttpServletRequest request){
        CookieUtil.delCookie(response,request,"username");
        CookieUtil.delCookie(response,request,"uid");
        CookieUtil.delCookie(response,request,"lid");
        return "redirect:homepage";
    }
}
