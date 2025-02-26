package com.aminnasiri;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.logging.Logging;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Arrays;



public class JeveryParamReplacer implements BurpExtension
{
    private Logging logging;
    private JTextArea payloadTextArea;
    private JCheckBox replaceInRepeater;
    private JCheckBox replaceInProxy;
    private JCheckBox mustRequestBeInScopeCheckBox;
    public List<String> payloads;
    private JprHttpHandler jprHttpHandler;

    @Override
    public void initialize(MontoyaApi montoyaApi) {
        montoyaApi.extension().setName("JeveryParamReplacer");
        this.logging = montoyaApi.logging();

        montoyaApi.userInterface().registerSuiteTab("JEvery Param Replacer Payloads", createUiPayloadPanel());
        jprHttpHandler = new JprHttpHandler(this, montoyaApi.http());
        montoyaApi.http().registerHttpHandler(jprHttpHandler);

    }

    private Component createUiPayloadPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        payloadTextArea = new JTextArea(10, 30);
        JScrollPane scrollPane = new JScrollPane(payloadTextArea);
        JButton saveButton = new JButton("Save Payloads");
        replaceInRepeater = new JCheckBox("Replace params in requests from repeater");
        replaceInProxy = new JCheckBox("Replace params in requests from proxy");
        mustRequestBeInScopeCheckBox = new JCheckBox("Must Request be in Scope ?");
        mustRequestBeInScopeCheckBox.setSelected(true);

        saveButton.addActionListener(e -> savePayloads());
        replaceInRepeater.addActionListener(e -> changeEnableRepeaterReplacerButton());
        replaceInProxy.addActionListener(e -> changeEnableProxyReplacerButton());
        mustRequestBeInScopeCheckBox.addActionListener(e -> changeMustRequestBeInScopeCheckBox());

        JPanel optionsPanel = new JPanel();
        optionsPanel.add(replaceInRepeater);
        optionsPanel.add(replaceInProxy);
        optionsPanel.add(mustRequestBeInScopeCheckBox);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(saveButton, BorderLayout.SOUTH);
        panel.add(optionsPanel, BorderLayout.NORTH);

        return panel;
    }

    private HttpRequest modifyRequest(HttpRequest originalRequest) {
        return originalRequest;
    }

    private void changeEnableProxyReplacerButton(){
        this.jprHttpHandler.setEnableProxyToolType(replaceInProxy.isSelected());
    }

    private void changeEnableRepeaterReplacerButton(){
        this.jprHttpHandler.setEnableRepeaterToolType(replaceInRepeater.isSelected());
    }

    private void changeMustRequestBeInScopeCheckBox(){
        this.jprHttpHandler.setRequestMustBeInScope(mustRequestBeInScopeCheckBox.isSelected());
    }

    private void savePayloads() {
        payloads = Arrays.asList(payloadTextArea.getText().split("\n"));
        print_output("Payloads are Saved:\n" + payloads);
        JOptionPane.showMessageDialog(null, "Payloads saved successfully!");
    }

    public void print_output(String text) {
        if (logging != null) {
            logging.logToOutput(text);
        }
    }

    public void print_error(String text) {
        if (logging != null) {
            logging.logToError(text);
        }
    }
}
