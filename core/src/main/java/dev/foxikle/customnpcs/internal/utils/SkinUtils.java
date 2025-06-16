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

import org.jetbrains.annotations.Nullable;
import org.mineskin.ClientBuilder;
import org.mineskin.JsoupRequestHandler;
import org.mineskin.MineSkinClient;
import org.mineskin.data.JobInfo;
import org.mineskin.data.SkinInfo;
import org.mineskin.request.GenerateRequest;

import java.util.concurrent.CompletableFuture;

public class SkinUtils {
    public static MineSkinClient MINESKIN_CLIENT;

    public static void setup(@Nullable String key, @Nullable String url) {

        ClientBuilder builder = MineSkinClient.builder().requestHandler(JsoupRequestHandler::new);
        if ((key == null || key.isEmpty()) && (url == null || url.isEmpty())) {
            builder.userAgent("Default-CustomNPCS/v1.7.4").baseUrl("https://mineskin.foxikle.dev");
        } else if (url == null || url.isEmpty()) {
            builder.userAgent("UserKey-CustomNPCS/v1.7.4").apiKey(key);
        } else { // we don't care if a key is used, if they supply their own proxy
            if (url.endsWith("/")) url = url.substring(0, url.length() - 1); // trim trailing slash off
            builder.userAgent("UserProxy-CustomNPCs/v1.7.4").baseUrl(url);
        }
        MINESKIN_CLIENT = builder.build();
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
