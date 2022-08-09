package com.zhouhao.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhouhao.dao.dos.Archives;
import com.zhouhao.dao.pojo.Article;

import java.util.List;

public interface ArticleMapper extends BaseMapper<Article> {
    List<Archives> listArchives();

    IPage<Article> listArticles(Page<Article> page, Long categoryId, Long tagId, String year, String month);
}
