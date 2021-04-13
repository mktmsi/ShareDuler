package com.notfound.jphacks.shareduler;

/**
 * Created by owner on 2017/10/21.
 */
//自分で作ったRunnableのジェネリック対応版
//Runnableと同じだが互換性はない
public interface MyRunnable<T> {
    public abstract void run(T t);
}
