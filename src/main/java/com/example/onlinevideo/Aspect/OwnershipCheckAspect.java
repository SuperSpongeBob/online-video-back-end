package com.example.onlinevideo.Aspect;

import com.example.onlinevideo.Annotation.CheckOwnership;
import com.example.onlinevideo.Security.CurrentUserService;
import com.example.onlinevideo.Service.VideoAlbumService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

@Aspect
@Component
public class OwnershipCheckAspect {
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Autowired
    private CurrentUserService currentUserService; // 用于获取当前用户ID
    @Autowired
    private ApplicationContext applicationContext; // 新增
    @Autowired
    private VideoAlbumService videoAlbumService;

    @Around("@annotation(checkOwnership)")
    public Object checkOwnership(ProceedingJoinPoint joinPoint, CheckOwnership checkOwnership) throws Throwable {
        // 1. 获取目标用户ID（通过SpEL表达式解析）
        Integer targetUserId = parseTargetUserId(joinPoint, checkOwnership.expression());

        //  如果是管理员且允许跳过，直接放行
        if (checkOwnership.allowAdmin() && currentUserService.isAdmin()){
            return joinPoint.proceed();
        }

        // 2. 检查当前用户是否有权限
        if (!currentUserService.isCurrentUser(targetUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权操作他人数据");
        }

        // 3. 权限验证通过，继续执行原方法
        return joinPoint.proceed();
    }

    /**
     * 解析SpEL表达式，获取目标用户ID
     */
    private Integer parseTargetUserId(ProceedingJoinPoint joinPoint, String expressionStr) {
        //  检查表达式是否为空
        if (!StringUtils.hasText(expressionStr)) {
            throw new IllegalArgumentException("@CheckOwnership expression must not be empty");
        }

        try {
            //  获取方法和签名
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Object[] args = joinPoint.getArgs();

            EvaluationContext context = new MethodBasedEvaluationContext(null, method, args, parameterNameDiscoverer);

            //  注册自定义函数
            context.setVariable("videoAlbumService", videoAlbumService);    //  注入Service供SpEl调用
            ((MethodBasedEvaluationContext) context).setBeanResolver(new BeanFactoryResolver(applicationContext));

            // 解析表达式
            Integer targetUserId = expressionParser.parseExpression(expressionStr)
                    .getValue(context, Integer.class);

            if (targetUserId == null) {
                throw new IllegalStateException("解析出的用户ID为null，请检查表达式: " + expressionStr);
            }
            return targetUserId;
        } catch (Exception e) {
            throw new IllegalStateException("解析表达式 '" + expressionStr + "' 失败: " + e.getMessage(), e);
        }
    }
}
