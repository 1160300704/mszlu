package com.zhouhao.service;

import com.zhouhao.vo.Result;
import com.zhouhao.vo.TagVo;
import sun.rmi.runtime.Log;

import java.util.List;

public interface TagService {
    List<TagVo> findTagsByArticleId(Long articleId);

    Result hots(int limit);

    Result findAll();

    Result findAllDetail();

    Result findDetailById(Long id);
}
