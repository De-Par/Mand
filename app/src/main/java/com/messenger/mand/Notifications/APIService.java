package com.messenger.mand.Notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAy-e1zV8:APA91bHqqIOymN6FTHHn-_PYRksbq9pc-yf4YmNBTfqoz93gbegJ8M_WTFJHKkucZ5ZbQXIs7kG0IwMbrl5_qaYVftFXffJga1NqIwigPd0havdojHG_Y6-YDhx-Z2-sqry76I7GGab5"
            }
    )

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
