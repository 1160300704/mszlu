package com.zhouhao.service;

import com.zhouhao.vo.Result;
import com.zhouhao.vo.params.CommentParam;

public interface CommentsService {
    Result commentsByArticleId(Long id);

    Result comment(CommentParam commentParam);
}
