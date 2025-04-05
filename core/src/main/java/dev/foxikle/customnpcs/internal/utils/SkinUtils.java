/*
 * Copyright (c) 2025. Foxikle
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.foxikle.customnpcs.internal.utils;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import org.jetbrains.annotations.Nullable;
import org.mineskin.JsoupRequestHandler;
import org.mineskin.MineSkinClient;
import org.mineskin.MineSkinClientImpl;
import org.mineskin.data.JobInfo;
import org.mineskin.data.SkinInfo;
import org.mineskin.request.GenerateRequest;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

public class SkinUtils {
    public static MineSkinClient MINESKIN_CLIENT;
    private static String URL = "https://mineskin.foxikle.dev/"; // free proxied instance

    public static void setup(@Nullable String key, @Nullable String url) {
        if (key == null && url == null) {
            // Use the defaults
            MINESKIN_CLIENT = MineSkinClient.builder()
                    .userAgent("Default-CustomNPCS/v1.7.4")
                    .requestHandler(JsoupRequestHandler::new)
                    .build();
        } else if (url == null) {
            MINESKIN_CLIENT = MineSkinClient.builder()
                    .userAgent("UserKey-CustomNPCS/v1.7.4")
                    .requestHandler(JsoupRequestHandler::new)
                    .apiKey(key)
                    .build();
            // user has a key, so use official mineskin api
            URL = "https://api.mineskin.org/";
        } else {
            // user specified a URL to use
            URL = url;
            MINESKIN_CLIENT = MineSkinClient.builder()
                    .userAgent("UserProxy-CustomNPCs/v1.7.4")
                    .requestHandler(JsoupRequestHandler::new)
                    .build();
        }

        // use reflection to change client URL
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);

            Field field = MineSkinClientImpl.class.getDeclaredField("API_BASE");
            field.setAccessible(true);

            Object base = unsafe.staticFieldBase(field);
            long offset = unsafe.staticFieldOffset(field);
            unsafe.putObject(base, offset, URL); // URL must be a String
            // test it
            CustomNPCs.getInstance().getLogger().info("Using Skin API URL: " + URL);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static CompletableFuture<SkinInfo> fetch(GenerateRequest request) {
        return SkinUtils.MINESKIN_CLIENT.queue().submit(request)
                .thenCompose(queueResponse -> {
                    JobInfo job = queueResponse.getJob();
                    // wait for job completion
                    return job.waitForCompletion(SkinUtils.MINESKIN_CLIENT);
                })
                .thenCompose(jobReference -> {
                    // get skin from job or load it from the API
                    return jobReference.getOrLoadSkin(SkinUtils.MINESKIN_CLIENT);
                });
    }
}
