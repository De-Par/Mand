package com.messenger.mand.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA9pK5LpM:APA91bF60_iPmBlHi1IysRsZIyp9b0PuPqSb7jU3zFklW1sO" +
                            "QBIKi0xYKB-_9hIaVKWSXwJTI8VMLp-QidcTBR3BsK5DvRPFWScP2paoMuUifVFxMaZv8v" +
                            "QOXF7mo3aRQwCM9QhR3zCo"
            }
    )

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
