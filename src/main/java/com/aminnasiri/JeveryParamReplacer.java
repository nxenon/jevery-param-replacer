package com.aminnasiri;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.logging.Logging;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Arrays;



public class JeveryParamReplacer implements BurpExtension
{
    private Logging logging;
    private JTextArea payloadTextArea;
    private JTextArea selectedParametersTextArea;
    private JCheckBox replaceInRepeater;
    private JCheckBox replaceInProxy;
    private JCheckBox mustRequestBeInScopeCheckBox;
    private JCheckBox mustParameterNameBeInSelectedParameterBoxCheckBox;
    public List<String> payloads;
    public List<String> selectedParameters;
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

        // Labels
        JLabel payloadLabel = new JLabel("Payloads:");
        JLabel selectedParametersLabel = new JLabel("In Scope Parameters:");

        // Payload Text Area
        payloadTextArea = new JTextArea(6, 20);
        JScrollPane payloadScrollPane = new JScrollPane(payloadTextArea);

        // Selected Header Text Area
        selectedParametersTextArea = new JTextArea(6, 20);
        JScrollPane selectedParametersScrollPane = new JScrollPane(selectedParametersTextArea);

        // Panels for text areas
        JPanel payloadPanel = new JPanel(new BorderLayout());
        payloadPanel.add(payloadLabel, BorderLayout.NORTH);
        payloadPanel.add(payloadScrollPane, BorderLayout.CENTER);

        JPanel selectedParamsPanel = new JPanel(new BorderLayout());
        selectedParamsPanel.add(selectedParametersLabel, BorderLayout.NORTH);
        selectedParamsPanel.add(selectedParametersScrollPane, BorderLayout.CENTER);

        // Make them sit side by side
        JPanel middlePanel = new JPanel(new GridLayout(1, 2, 10, 0)); // 1 row, 2 columns, 10px gap
        middlePanel.add(payloadPanel);
        middlePanel.add(selectedParamsPanel);

        // Checkboxes
        replaceInRepeater = new JCheckBox("Replace params in requests from repeater");
        replaceInRepeater.addActionListener(e -> changeEnableRepeaterReplacerButton());

        replaceInProxy = new JCheckBox("Replace params in requests from proxy");
        replaceInProxy.addActionListener(e -> changeEnableProxyReplacerButton());

        mustRequestBeInScopeCheckBox = new JCheckBox("Must Request be in Scope ?");
        mustRequestBeInScopeCheckBox.setSelected(true);
        mustRequestBeInScopeCheckBox.addActionListener(e -> changeMustRequestBeInScopeCheckBox());

        mustParameterNameBeInSelectedParameterBoxCheckBox = new JCheckBox("Must Parameter Name Be in \"In Scope Parameter Box?\" [If you disable this, all parameters will be replaced!]");
        mustParameterNameBeInSelectedParameterBoxCheckBox.setSelected(true);
        mustParameterNameBeInSelectedParameterBoxCheckBox.addActionListener(e -> changeMustParameterNameBeInSelectedParameterCheckBox());

        JButton saveButton = new JButton("Save Settings");
        saveButton.addActionListener(e -> saveSettings());

        // Top panel for checkboxes
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(replaceInRepeater);
        topPanel.add(replaceInProxy);
        topPanel.add(mustRequestBeInScopeCheckBox);
        topPanel.add(mustParameterNameBeInSelectedParameterBoxCheckBox);

        // Add everything to the main panel
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(middlePanel, BorderLayout.CENTER);
        panel.add(saveButton, BorderLayout.SOUTH);

        return panel;
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

    private void changeMustParameterNameBeInSelectedParameterCheckBox(){
        this.jprHttpHandler.setParametersMustBeInScope(mustParameterNameBeInSelectedParameterBoxCheckBox.isSelected());
    }

    private void saveSettings() {
        payloads = Arrays.asList(payloadTextArea.getText().split("\n"));
        print_output("Payloads are Saved:\n" + payloads);

        selectedParameters = Arrays.asList(selectedParametersTextArea.getText().split("\n"));
        print_output("In-Scope parameters are Saved:\n" + selectedParameters);

        JOptionPane.showMessageDialog(null, "Settings saved successfully!");
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
