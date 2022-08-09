package com.zhouhao.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhouhao.dao.mapper.ArticleMapper;
import com.zhouhao.dao.pojo.Article;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ThreadService {

    @Async("asyncServiceExecutor")
    public void updateArticleViewCount(ArticleMapper articleMapper, Article article) {
        try {
            int viewCounts = article.getViewCounts();
            java.lang.Thread.sleep(5000);
            article.setViewCounts(article.getViewCounts() + 1);
            LambdaUpdateWrapper<Article> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Article::getId, article.getId());
            updateWrapper.eq(Article::getViewCounts, viewCounts);
            articleMapper.update(article,updateWrapper);
            System.out.println("更新完成了...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
