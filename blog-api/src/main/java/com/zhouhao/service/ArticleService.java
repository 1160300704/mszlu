package com.zhouhao.service;

import com.zhouhao.vo.Result;
import com.zhouhao.vo.params.ArticleParam;
import com.zhouhao.vo.params.PageParams;

public interface ArticleService {
    /**
     * 文章归档
     * @return
     */
    Result listArchives();

    Result articles(PageParams pageParams);

    Result hotArticle(int limit);

    Result newArticle(int limit);

    /**
     * 查看文章详情
     * @param articleId
     * @return
     */
    Result findArticleById(Long articleId);

    Result publish(ArticleParam articleParam);
}
