package com.zhouhao.controller;

import com.zhouhao.common.aop.LogAnnotation;
import com.zhouhao.common.cache.Cache;
import com.zhouhao.service.ArticleService;
import com.zhouhao.vo.Result;
import com.zhouhao.vo.params.ArticleParam;
import com.zhouhao.vo.params.PageParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/articles")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @PostMapping
    @LogAnnotation(module = "文章", operator = "获取文章列表")
//    @Cache(expire = 5 * 60 * 1000, name = "articles")
    public Result articles(@RequestBody PageParams pageParams){
        return articleService.articles(pageParams);
    }

    /**
     * 首页 最热文章
     * @return
     */
    @PostMapping("hot")
//    @Cache(expire = 5 * 60 * 1000, name = "hot_article")
    public Result hotArticle(){
        int limit = 5;
        return articleService.hotArticle(limit);
    }

    /**
     * 首页 最新文章
     * @return
     */
    @PostMapping("new")
//    @Cache(expire = 5 * 60 * 1000, name = "new_article")
    public Result newArticle(){
        int limit = 5;
        return articleService.newArticle(limit);
    }

    /**
     * 首页 最新文章
     * @return
     */
    @PostMapping("listArchives")
    public Result listArchives(){
        return articleService.listArchives();
    }

    @PostMapping("/view/{id}")
    public Result findArticleById(@PathVariable("id") Long articleId){
        return articleService.findArticleById(articleId);
    }

    @PostMapping("publish")
    public Result publish(@RequestBody ArticleParam articleParam){
        return articleService.publish(articleParam);
    }
}
