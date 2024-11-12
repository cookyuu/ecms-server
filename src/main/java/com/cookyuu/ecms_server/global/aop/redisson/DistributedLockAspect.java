package com.cookyuu.ecms_server.global.aop.redisson;

import com.cookyuu.ecms_server.global.exception.utils.EcmsRedissonException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {
    private final RedissonClient redissonClient;
    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(distributedLock")
    public Object excuteWithLock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = generateLockKey(joinPoint, distributedLock);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(
                    distributedLock.waitTime(),
                    distributedLock.leaseTime(),
                    distributedLock.timeUnit()
            );

            if (!isLocked){
                log.error("[Lock::Fail] Fail to acquire distributed lock");
                throw new EcmsRedissonException();
            }
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Lock::Fail] Interrupted distributed lock");
            throw new EcmsRedissonException();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private String generateLockKey(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        EvaluationContext context = new StandardEvaluationContext();

        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i=0; i<parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        Expression expression = parser.parseExpression(distributedLock.key());
        return expression.getValue(context, String.class);
    }
}
