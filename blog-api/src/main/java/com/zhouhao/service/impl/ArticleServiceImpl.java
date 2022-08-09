package com.zhouhao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhouhao.dao.dos.Archives;
import com.zhouhao.dao.mapper.ArticleBodyMapper;
import com.zhouhao.dao.mapper.ArticleMapper;
import com.zhouhao.dao.mapper.ArticleTagMapper;
import com.zhouhao.dao.pojo.Article;
import com.zhouhao.dao.pojo.ArticleBody;
import com.zhouhao.dao.pojo.ArticleTag;
import com.zhouhao.dao.pojo.SysUser;
import com.zhouhao.service.*;
import com.zhouhao.utils.UserThreadLocal;
import com.zhouhao.vo.ArticleBodyVo;
import com.zhouhao.vo.ArticleVo;
import com.zhouhao.vo.Result;
import com.zhouhao.vo.TagVo;
import com.zhouhao.vo.params.ArticleParam;
import com.zhouhao.vo.params.PageParams;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private TagService tagService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Override
    public Result listArchives() {
        List<Archives> archivesList = articleMapper.listArchives();
        return Result.success(archivesList);
    }

    @Override
    public Result articles(PageParams pageParams) {
        Page<Article> page = new Page<>(pageParams.getPage(), pageParams.getPageSize());

        IPage<Article> articleIPage = articleMapper.listArticles(
            page,
            pageParams.getCategoryId(),
            pageParams.getTagId(),
            pageParams.getYear(),
            pageParams.getMonth()
        );

        List<Article> records = articleIPage.getRecords();
        return Result.success(copyList(records, true, true));
    }

    /**
     * 文章列表
     * @param pageParams
     * @return
     */
    /*@Override
    public Result articles(PageParams pageParams) {
        IPage<Article> page = new Page<>((pageParams.getPage()-1)*pageParams.getPageSize(), pageParams.getPageSize());
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        if(pageParams.getCategoryId() != null){
            queryWrapper.eq(Article::getCategoryId, pageParams.getCategoryId());
        }
        if(pageParams.getTagId() != null){
            LambdaQueryWrapper<ArticleTag> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(ArticleTag::getTagId, pageParams.getTagId());
            List<ArticleTag> articleTags = articleTagMapper.selectList(queryWrapper1);
            List<Long> ArticleIds = new ArrayList<>();
            for(ArticleTag articleTag: articleTags){
                ArticleIds.add(articleTag.getArticleId());
            }
            queryWrapper.in(Article::getId, ArticleIds);
        }
        queryWrapper.orderByDesc(Article::getWeight, Article::getCreateDate);
        articleMapper.selectPage(page, queryWrapper);
        List<Article> records = page.getRecords();
        List<ArticleVo> articleVoList = copyList(records, true, true);

        return Result.success(articleVoList);
    }*/

    /**
     * 最热文章
     * @param limit
     * @return
     */
    @Override
    public Result hotArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId, Article::getTitle);
        queryWrapper.last("limit " + limit);
        // select id, title from ms_article order by view_count limit 5
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles, false, false));
    }

    /**
     * 最新文章
     * @param limit
     * @return
     */
    @Override
    public Result newArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getCreateDate);
        queryWrapper.select(Article::getId, Article::getTitle);
        queryWrapper.last("limit " + limit);
        // select id, title from ms_article order by view_count limit 5
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles, false, false));
    }

    @Autowired
    private ThreadService threadService;

    @Override
    public Result findArticleById(Long articleId) {
        Article article = this.articleMapper.selectById(articleId);
        ArticleVo articleVo = copy(article, true, true, true, true);
        threadService.updateArticleViewCount(articleMapper, article);
        return Result.success(articleVo);
    }

    @Override
    public Result publish(ArticleParam articleParam) {
        SysUser sysUser = UserThreadLocal.get();
        Article article = new Article();
        article.setAuthorId(sysUser.getId());
        article.setWeight(Article.Article_Common);
        article.setViewCounts(0);
        article.setTitle(articleParam.getTitle());
        article.setSummary(articleParam.getSummary());
        article.setCommentCounts(0);
        article.setCreateDate(System.currentTimeMillis());
        article.setCategoryId(articleParam.getCategory().getId());
        articleMapper.insert(article);

        List<TagVo> tags = articleParam.getTags();
        if(tags != null){
            for (TagVo tag : tags) {
                Long articleId = article.getId();
                ArticleTag articleTag = new ArticleTag();
                articleTag.setTagId(tag.getId());
                articleTag.setArticleId(articleId);
                articleTagMapper.insert(articleTag);
            }
        }
        ArticleBody articleBody = new ArticleBody();
        articleBody.setArticleId(article.getId());
        articleBody.setContent(articleParam.getBody().getContent());
        articleBody.setContentHtml(articleParam.getBody().getContentHtml());
        articleBodyMapper.insert(articleBody);
        article.setBodyId(articleBody.getId());
        articleMapper.updateById(article);

        Map<String, String> map = new HashMap<>();
        map.put("id", article.getId().toString());
        return Result.success(map);
    }

    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor) {
        List<ArticleVo> articleVos = new ArrayList<>();
        for(int i = 0; i < records.size(); i++){
            ArticleVo articleVo = copy(records.get(i), isTag, isAuthor);
            articleVos.add(articleVo);
        }

        return articleVos;
    }

    private ArticleVo copy(Article article, boolean isTag, boolean isAuthor){
        ArticleVo articleVo = new ArticleVo();
        BeanUtils.copyProperties(article, articleVo);

        articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-MM-dd hh:mm:ss"));
        if(isTag){
            Long articleId = article.getId();
            articleVo.setTags(tagService.findTagsByArticleId(articleId));
        }
        if(isAuthor){
            Long authorId = article.getAuthorId();
            articleVo.setAuthor(sysUserService.findUserById(authorId).getNickname());
        }
        return articleVo;
    }

    @Autowired
    private CategoryService categoryService;

    private ArticleVo copy(Article article, boolean isTag, boolean isAuthor, boolean isBody, boolean isCategory){
        ArticleVo articleVo = new ArticleVo();
        BeanUtils.copyProperties(article, articleVo);

        articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-MM-dd hh:mm:ss"));
        if(isTag){
            Long articleId = article.getId();
            articleVo.setTags(tagService.findTagsByArticleId(articleId));
        }
        if(isAuthor){
            Long authorId = article.getAuthorId();
            articleVo.setAuthor(sysUserService.findUserById(authorId).getNickname());
        }
        if(isBody){
            Long bodyId = article.getBodyId();
            articleVo.setBody(findArticleBodyById(bodyId));
        }
        if(isCategory){
            Long categoryId = article.getCategoryId();
            articleVo.setCategory(categoryService.findCategoryById(categoryId));
        }
        return articleVo;
    }

    @Autowired
    private ArticleBodyMapper articleBodyMapper;

    private ArticleBodyVo findArticleBodyById(Long bodyId) {
        ArticleBody articleBody = articleBodyMapper.selectById(bodyId);
        ArticleBodyVo articleBodyVo = new ArticleBodyVo();
        BeanUtils.copyProperties(articleBody, articleBodyVo);
        return articleBodyVo;
    }
}
