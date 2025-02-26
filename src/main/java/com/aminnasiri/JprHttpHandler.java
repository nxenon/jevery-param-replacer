package com.aminnasiri;

import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.HttpParameterType;


import static burp.api.montoya.http.handler.RequestToBeSentAction.continueWith;
import burp.api.montoya.core.ToolType;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import burp.api.montoya.http.Http;

import static burp.api.montoya.http.message.params.HttpParameter.bodyParameter;
import static burp.api.montoya.http.message.params.HttpParameter.urlParameter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;


public class JprHttpHandler implements HttpHandler {
    private JeveryParamReplacer jeveryParamReplacerObject;
    private ToolType[] toolTypeSources = {};
    private boolean requestMustBeInScope = true;
    private boolean parametersMustBeInScope = true;
    private Http httpObject;


    public JprHttpHandler(JeveryParamReplacer jeveryParamReplacerObject, Http httpObject) {
        this.jeveryParamReplacerObject = jeveryParamReplacerObject;
        this.httpObject = httpObject;
    }

    private void print_error(String text){
        this.jeveryParamReplacerObject.print_error(text);
    }

    private void print_output(String text){
        this.jeveryParamReplacerObject.print_output(text);
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent httpRequestToBeSent) {

        if (requestMustBeInScope){
            if (!httpRequestToBeSent.isInScope()){
                return null;
            }
        }

        ToolType initiatingTool = httpRequestToBeSent.toolSource().toolType();

        if (!Arrays.asList(toolTypeSources).contains(initiatingTool)) {
            return null;
        }


        for (String p: this.jeveryParamReplacerObject.payloads){
            HttpRequest modifiedRequest = httpRequestToBeSent;
            List<ParsedHttpParameter> allParams = httpRequestToBeSent.parameters();
            for (ParsedHttpParameter param : allParams) {
                if (!checkParameterInScopeState(param.name())){
                    continue;
                }
                if (param.type() == HttpParameterType.URL){
                    modifiedRequest = modifiedRequest.withParameter(urlParameter(param.name(), p));
                } else if (param.type() == HttpParameterType.BODY){
                    modifiedRequest = modifiedRequest.withParameter(bodyParameter(param.name(), p));
                } else if (param.type() == HttpParameterType.JSON){
                    String requestBody = modifiedRequest.body().toString();
                    try{

                        JsonObject jsonObject = JsonParser.parseString(requestBody).getAsJsonObject();
                        for (String key : jsonObject.keySet()) {
                            if (!checkParameterInScopeState(key)){
                                continue;
                            }
                            print_output(jsonObject.get(key).getAsString());
                            jsonObject.addProperty(key, p);
                        }

                        modifiedRequest = modifiedRequest.withBody(jsonObject.toString());

                    } catch (JsonSyntaxException e){
                        print_error("Invalid JSON detected: " + e.getMessage());
                    }

                } else {
                    print_error("Invalid Param Type: " + param.type().toString());
                }
            }

            if (modifiedRequest == null) {
                continue;
            }

            HttpRequestResponse httpRequestResponse = httpObject.sendRequest(modifiedRequest);

        }


        return continueWith(httpRequestToBeSent, null);

    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived httpResponseReceived) {
        return null;
    }

    private boolean checkParameterInScopeState(String paramName){
        if (parametersMustBeInScope){
            return this.jeveryParamReplacerObject.selectedParameters.contains(paramName);
        } else {
            return true;
        }
    }

    public void setRequestMustBeInScope(boolean state) {
        requestMustBeInScope = state;
    }

    public void setParametersMustBeInScope(boolean state) {
        parametersMustBeInScope = state;
    }

    private static boolean isPost(HttpRequestToBeSent httpRequestToBeSent) {
        return httpRequestToBeSent.method().equalsIgnoreCase("POST");
    }

    public void setEnableRepeaterToolType(boolean state){
        ArrayList<ToolType> list = new ArrayList<>(Arrays.asList(toolTypeSources));
        if (state){
            if (!list.contains(ToolType.REPEATER)){
                list.add(ToolType.REPEATER);
                toolTypeSources = list.toArray(new ToolType[0]);
            }
        } else {
            if (list.contains(ToolType.REPEATER)){
                list.remove(ToolType.REPEATER);
                toolTypeSources = list.toArray(new ToolType[0]);
            }
        }
    }
    public void setEnableProxyToolType(boolean state){
        ArrayList<ToolType> list = new ArrayList<>(Arrays.asList(toolTypeSources));
        if (state){
            if (!list.contains(ToolType.PROXY)){
                list.add(ToolType.PROXY);
                toolTypeSources = list.toArray(new ToolType[0]);
            }
        } else {
            if (list.contains(ToolType.PROXY)){
                list.remove(ToolType.PROXY);
                toolTypeSources = list.toArray(new ToolType[0]);
            }
        }
    }


}
