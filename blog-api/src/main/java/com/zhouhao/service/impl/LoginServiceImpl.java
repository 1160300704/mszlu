package com.zhouhao.service.impl;

import com.alibaba.fastjson.JSON;
import com.zhouhao.dao.pojo.SysUser;
import com.zhouhao.service.LoginService;
import com.zhouhao.service.SysUserService;
import com.zhouhao.utils.JWTUtils;
import com.zhouhao.vo.ErrorCode;
import com.zhouhao.vo.Result;
import com.zhouhao.vo.params.LoginParam;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class LoginServiceImpl implements LoginService {
    private static final String slat = "mszlu!@#";
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public Result login(LoginParam loginParam) {
        /**
         * 1. 检查参数是否合法
         * 2. 根据用户名和密码去user表中查询
         * 3. 不存在登录失败
         * 4. 存在，生成token给前端
         * 5. token放入redis token：user信息 设置过期时间（登录的时候，先认证字符串是否合法，然后去redis认证存在）
         */
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        if(StringUtils.isBlank(account) || StringUtils.isBlank(password)){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), ErrorCode.PARAMS_ERROR.getMsg());
        }
        password = DigestUtils.md5Hex(password + slat);
        SysUser sysUser = sysUserService.findUser(account, password);
        if(sysUser == null){
            return Result.fail(ErrorCode.ACCOUNT_PWD_NOT_EXIST.getCode(), ErrorCode.ACCOUNT_PWD_NOT_EXIST.getMsg());
        }
        String token = JWTUtils.createToken(sysUser.getId());

        redisTemplate.opsForValue().set("Token_" + token, JSON.toJSONString(sysUser), 1, TimeUnit.DAYS);
        return Result.success(token);
    }

    @Override
    public SysUser checkToken(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        Map<String, Object> stringObjectMap = JWTUtils.checkToken(token);
        if(stringObjectMap == null){
            return null;
        }
        String userJson = redisTemplate.opsForValue().get("Token_" + token);
        if(StringUtils.isBlank(userJson)){
            return null;
        }

        return JSON.parseObject(userJson, SysUser.class);
    }

    @Override
    public Result logout(String token) {
        redisTemplate.delete("Token_" + token);
        return Result.success(null);
    }

    @Override
    public Result register(LoginParam loginParam) {
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        String nickname = loginParam.getNickname();

        if(StringUtils.isBlank(account) || StringUtils.isBlank(password) || StringUtils.isBlank(nickname)){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), ErrorCode.PARAMS_ERROR.getMsg());
        }
        SysUser sysUser = sysUserService.findUserByAccount(account);
        if(sysUser != null){
            return Result.fail(ErrorCode.ACCOUNT_EXIST.getCode(), ErrorCode.ACCOUNT_EXIST.getMsg());
        }
        sysUser = new SysUser();
        sysUser.setNickname(nickname);
        sysUser.setAccount(account);
        sysUser.setPassword(DigestUtils.md5Hex(password+slat));
        sysUser.setCreateDate(System.currentTimeMillis());
        sysUser.setLastLogin(System.currentTimeMillis());
        sysUser.setAvatar("/static/img/logo.b3a48c0.png");
        sysUser.setIsAdmin(1); //1 为true
        sysUser.setDeleted(0); // 0 为false
        sysUser.setSalt("");
        sysUser.setStatus("");
        sysUser.setEmail("");
        this.sysUserService.save(sysUser);

        String token = JWTUtils.createToken(sysUser.getId());
        redisTemplate.opsForValue().set("Token_" + token, JSON.toJSONString(sysUser), 1, TimeUnit.DAYS);

        return Result.success(token);
    }
}
