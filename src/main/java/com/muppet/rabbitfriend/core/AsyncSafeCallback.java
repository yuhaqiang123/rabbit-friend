package com.muppet.rabbitfriend.core;

import java.util.LinkedList;
import java.util.List;

/**
 * 异步的安全Callback
 * 该类型必须输入备份对象
 * 在异步处理流程中，当发送完异步请求后，当前线程就去做其他事情了，而执行回调逻辑的线程
 * 是另一个线程，这时，如果在该回调线程中出现异常，也需要统一的捕获，捕获之后，可能需要作一些善后兜底工作
 * 例如这个异步流程必须终止，可能需要调用其他对象方法终止该流程，或者释放该流程的锁。或者继续回调到上一流程
 * 如果没有兜底，那么一旦回调流程出现异常，代码路径就会改变，无法保证该流程一定终止，虽然流程会有超时时间，但是不如
 * 通过捕获该异常处理及时。
 * <p>
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public abstract class AsyncSafeCallback<T> implements Callback<T> {

    public List<Object> backups = new LinkedList<>();

    AsyncSafeCallback(Object object) {
        this.backups.add(object);
    }

    public AsyncSafeCallback(Object object, Object... backups) {
        this.backups.add(object);
        for (Object backup : backups) {
            this.backups.add(backup);
        }
    }
}

