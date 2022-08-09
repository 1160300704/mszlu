package com.zhouhao.service;

import com.zhouhao.vo.CategoryVo;
import com.zhouhao.vo.Result;

public interface CategoryService {
    CategoryVo findCategoryById(Long categoryId);

    Result findAll();

    Result findAllDetial();

    Result categoriesDetailById(Long id);
}
