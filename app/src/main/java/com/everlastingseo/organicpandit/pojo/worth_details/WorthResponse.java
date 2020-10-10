package com.everlastingseo.organicpandit.pojo.worth_details;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WorthResponse {
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("data")
    @Expose
    private WorthData data;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public WorthData getData() {
        return data;
    }

    public void setData(WorthData data) {
        this.data = data;
    }
}
