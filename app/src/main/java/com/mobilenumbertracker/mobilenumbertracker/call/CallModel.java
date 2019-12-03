package com.mobilenumbertracker.mobilenumbertracker.call;

import java.util.Date;

/**
 * Created by Ahmed on 11/01/2017.
 */

public class CallModel {
    private String number;
    private String callType;
    private Date date;
    private String callDuration;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCallDuration() {
        return callDuration;
    }

    public void setCallDuration(String callDuration) {
        this.callDuration = callDuration;
    }

    public CallModel(String number, String callType, Date date, String callDuration) {
        this.number = number;
        this.callType = callType;
        this.date = date;
        this.callDuration = callDuration;
    }
}
