package com.notfound.jphacks.shareduler;

import com.android.volley.Response;

/**
 * Created by owner on 2017/10/21.
 */

public class respListener<T> implements Response.Listener<T> {
    private T _result = null;

    public T getResp() {
        return _result;
    }

    @Override
    public void onResponse(T response) {
        _result = response;
    }
}
