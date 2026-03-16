//package com.chwww924.chwwwBackend.controller;
//
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.chwww924.chwwwBackend.common.BaseResponse;
//import com.chwww924.chwwwBackend.common.ErrorCode;
//import com.chwww924.chwwwBackend.common.ResultUtils;
//import com.chwww924.chwwwBackend.model.dto.post.PostQueryRequest;
//import com.chwww924.chwwwBackend.model.dto.postfavour.QuestionSubmitAddRequest;
//import com.chwww924.chwwwBackend.model.dto.postfavour.QuestionSubmitQueryRequest;
//import com.chwww924.chwwwBackend.model.entity.Post;
//import com.chwww924.chwwwBackend.model.entity.User;
//import com.chwww924.chwwwBackend.model.vo.PostVO;
//import com.chwww924.chwwwBackend.exception.BusinessException;
//import com.chwww924.chwwwBackend.exception.ThrowUtils;
//import com.chwww924.chwwwBackend.service.QuestionSubmitService;
//import com.chwww924.chwwwBackend.service.PostService;
//import com.chwww924.chwwwBackend.service.UserService;
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * 题目收藏接口
// *
// * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
// * @from <a href="https://yupi.icu">编程导航知识星球</a>
// */
//@RestController
//@RequestMapping("/ques_submit")
//@Slf4j
//public class QuestionSubmitController {
//
//    @Resource
//    private QuestionSubmitService questionSubmitService;
//
//    @Resource
//    private PostService postService;
//
//    @Resource
//    private UserService userService;
//
//    /**
//     * 收藏 / 取消收藏
//     *
//     * @param questionSubmitAddRequest
//     * @param request
//     * @return resultNum 收藏变化数
//     */
//    @PostMapping("/")
//    public BaseResponse<Integer> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
//                                              HttpServletRequest request) {
//        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getPostId() <= 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        // 登录才能操作
//        final User loginUser = userService.getLoginUser(request);
//        long postId = questionSubmitAddRequest.getPostId();
//        int result = questionSubmitService.doQuestionSubmit(postId, loginUser);
//        return ResultUtils.success(result);
//    }
//
//    /**
//     * 获取我收藏的题目列表
//     *
//     * @param postQueryRequest
//     * @param request
//     */
//    @PostMapping("/my/list/page")
//    public BaseResponse<Page<PostVO>> listMyFavourPostByPage(@RequestBody PostQueryRequest postQueryRequest,
//                                                             HttpServletRequest request) {
//        if (postQueryRequest == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        User loginUser = userService.getLoginUser(request);
//        long current = postQueryRequest.getCurrent();
//        long size = postQueryRequest.getPageSize();
//        // 限制爬虫
//        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
//        Page<Post> postPage = questionSubmitService.listFavourPostByPage(new Page<>(current, size),
//                postService.getQueryWrapper(postQueryRequest), loginUser.getId());
//        return ResultUtils.success(postService.getPostVOPage(postPage, request));
//    }
//
//    /**
//     * 获取用户收藏的题目列表
//     *
//     * @param questionSubmitQueryRequest
//     * @param request
//     */
//    @PostMapping("/list/page")
//    public BaseResponse<Page<PostVO>> listFavourPostByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
//            HttpServletRequest request) {
//        if (questionSubmitQueryRequest == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        long current = questionSubmitQueryRequest.getCurrent();
//        long size = questionSubmitQueryRequest.getPageSize();
//        Long userId = questionSubmitQueryRequest.getUserId();
//        // 限制爬虫
//        ThrowUtils.throwIf(size > 20 || userId == null, ErrorCode.PARAMS_ERROR);
//        Page<Post> postPage = questionSubmitService.listFavourPostByPage(new Page<>(current, size),
//                postService.getQueryWrapper(questionSubmitQueryRequest.getPostQueryRequest()), userId);
//        return ResultUtils.success(postService.getPostVOPage(postPage, request));
//    }
//}
