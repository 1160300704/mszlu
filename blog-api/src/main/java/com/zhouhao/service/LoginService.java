package com.zhouhao.service;

import com.zhouhao.dao.pojo.SysUser;
import com.zhouhao.vo.Result;
import com.zhouhao.vo.params.LoginParam;

public interface LoginService {
    /**
     * 登录功能
     * @param loginParam
     * @return
     */
    Result login(LoginParam loginParam);

    SysUser checkToken(String token);

    Result logout(String token);

    /**
     * 注册
     * @param loginParam
     * @return
     */
    Result register(LoginParam loginParam);
}
