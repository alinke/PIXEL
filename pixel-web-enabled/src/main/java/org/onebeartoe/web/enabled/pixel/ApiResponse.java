/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.onebeartoe.web.enabled.pixel;

import com.google.gson.Gson;

/**
 *
 * @author nobull-laptop
 */
public class ApiResponse {
    private int httpCode;
    private String httpResponse;
    
    public int getHttpCode ()
    {
        return httpCode;
    }
    public void setHttpCode (int Code)
    {
        httpCode = Code;
    }
    
    public String getHttpResponse ()
    {
        return httpResponse;
    }

    public void setHttpResponse (String response)
    {
        httpResponse = response;
    }
    public void setHttpResponse (String response, String convertTo)
    {
        switch (convertTo)
        {
            case "text": default:
                httpResponse = response;
                break;
            case "json":
                Gson gson = new Gson();
                httpResponse = gson.toJson(response);
        }

    }
}
