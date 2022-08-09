package com.zhouhao.service;

import com.zhouhao.dao.pojo.SysUser;
import com.zhouhao.vo.Result;
import com.zhouhao.vo.UserVo;

public interface SysUserService {
    UserVo findUserVoById(Long id);

    SysUser findUserById(Long id);

    SysUser findUser(String account, String password);

    Result findUserByToken(String token);

    SysUser findUserByAccount(String account);

    void save(SysUser sysUser);
}
