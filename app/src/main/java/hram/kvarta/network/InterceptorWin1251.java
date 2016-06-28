package hram.kvarta.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @author Evgeny Hramov
 */
public class InterceptorWin1251 implements Interceptor {

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        return originalResponse.newBuilder().header("Content-Type", "text/html;charset=windows-1251").build();
    }
}
