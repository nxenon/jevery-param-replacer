package com.aminnasiri;

import burp.api.montoya.http.Http;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class JprMenuTab implements ContextMenuItemsProvider {
    private JeveryParamReplacer jeveryParamReplacerObject;
    private Http httpObject;
    private JprHttpHandler jprHttpHandler;

    public JprMenuTab(JeveryParamReplacer jeveryParamReplacerObject, Http httpObject, JprHttpHandler jprHttpHandler) {
        this.jeveryParamReplacerObject = jeveryParamReplacerObject;
        this.httpObject = httpObject;
        this.jprHttpHandler = jprHttpHandler;
    }


    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        HttpRequest request = null;
        if (event.selectedRequestResponses().isEmpty()) {
            if (event.messageEditorRequestResponse().isEmpty()){
                return List.of();
            } else {
                request = event.messageEditorRequestResponse().get().requestResponse().request();
            }
        } else {
            request = event.selectedRequestResponses().get(0).request();
        }

        JMenuItem sendToJprMenuItem = new JMenuItem("Send to JPR");
        HttpRequest finalRequest = request;
        sendToJprMenuItem.addActionListener(
                e -> {
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() {
                            jprHttpHandler.doReplacementTask(finalRequest);
                            return null;
                        }
                    }.execute();
                }
        );

        return List.of(sendToJprMenuItem);
    }

}
