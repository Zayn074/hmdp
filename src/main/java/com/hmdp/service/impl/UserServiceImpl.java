package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.log.Log;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Objects;
import java.util.Random;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            //2.不符合返回异常
            return Result.fail("手机号格式错误");
        }
        //3.符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        //4.保存验证码到session
        session.setAttribute("code", code);
        //5.发送验证码
        log.debug("发送验证码:" + code);
        //6.返回ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //1.检验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
            //2.校验验证码
            Object cacheCode = session.getAttribute("code");
            String code = loginForm.getCode();
            if (cacheCode == null || !cacheCode.toString().equals(code)) {
                //3.不一致返回异常
                log.debug("cacheCode"+cacheCode);
                log.debug("code"+code);

                return Result.fail("验证码错误");
            }

            //4.一致，去数据库根据手机号查询用户
            User user = query().eq("phone", phone).one();
            //5.判断用户是否存在
            if (user == null) {
                log.info("用户不存在，开始创建用户");
                //6.不存在，创建新用户并保存
                user = createUserWithPhone(phone);
                log.info("用户注册成功，id："+user.getNickName());

            }

            //7.保存用户信息到session
            session.setAttribute("user", BeanUtil.copyProperties(   user, UserDTO.class));
            log.info("用户登录成功");
            return Result.ok();
        }
    private User createUserWithPhone(String phone) {
        //1.创建用户
        User user = new User();
        //2.填入字段
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        //3.保存
        save(user);
        return user;
    }
}




