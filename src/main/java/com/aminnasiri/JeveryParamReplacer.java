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
    private JTextArea selectedHeadersTextArea;
    private JCheckBox replaceInRepeater;
    private JCheckBox replaceInProxy;
    private JCheckBox urlParamsCheckBox;
    private JCheckBox bodyParamsCheckBox;
    private JCheckBox jsonParamsCheckBox;
    private JCheckBox mustRequestBeInScopeCheckBox;
    private JCheckBox mustParameterNameBeInSelectedParameterBoxCheckBox;
    private JCheckBox mustHeadersBeReplacedCheckBox;
    private JCheckBox mustHeadersBeInInScopeHeadersCheckBox;
    public List<String> payloads;
    public List<String> selectedParameters;
    public List<String> selectedHeaders;
    public JprHttpHandler jprHttpHandler;
    public JprLoggerTab jprLoggerTab;

    @Override
    public void initialize(MontoyaApi montoyaApi) {
        montoyaApi.extension().setName("JeveryParamReplacer");
        this.logging = montoyaApi.logging();
        jprLoggerTab = new JprLoggerTab(montoyaApi.http(), montoyaApi.userInterface());

        montoyaApi.userInterface().registerSuiteTab("JEvery Param Replacer Payloads", createUiPayloadPanel());
        montoyaApi.userInterface().registerSuiteTab("JPR Request Logger", jprLoggerTab);

        jprHttpHandler = new JprHttpHandler(this, montoyaApi.http());
        montoyaApi.http().registerHttpHandler(jprHttpHandler);
        montoyaApi.userInterface().registerContextMenuItemsProvider(new JprMenuTab(this, montoyaApi.http(), jprHttpHandler));

    }

    private Component createUiPayloadPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Labels
        JLabel payloadLabel = new JLabel("Payloads:");
        JLabel selectedParametersLabel = new JLabel("In Scope Parameters [case-sensitive]:");
        JLabel inScopeHeadersLabel = new JLabel("In Scope Headers [case-sensitive]:");

        // Payload Text Area
        payloadTextArea = new JTextArea(6, 20);
        JScrollPane payloadScrollPane = new JScrollPane(payloadTextArea);

        // Selected Header Text Area
        selectedParametersTextArea = new JTextArea(6, 20);
        JScrollPane selectedParametersScrollPane = new JScrollPane(selectedParametersTextArea);

        // Selected Header Text Area
        selectedHeadersTextArea = new JTextArea(6, 20);
        JScrollPane selectedHeadersScrollPane = new JScrollPane(selectedHeadersTextArea);

        // Panels for text areas
        JPanel payloadPanel = new JPanel(new BorderLayout());
        payloadPanel.add(payloadLabel, BorderLayout.NORTH);
        payloadPanel.add(payloadScrollPane, BorderLayout.CENTER);

        JPanel selectedParamsPanel = new JPanel(new BorderLayout());
        selectedParamsPanel.add(selectedParametersLabel, BorderLayout.NORTH);
        selectedParamsPanel.add(selectedParametersScrollPane, BorderLayout.CENTER);

        JPanel selectedHeadersPanel = new JPanel(new BorderLayout());
        selectedHeadersPanel.add(inScopeHeadersLabel, BorderLayout.NORTH);
        selectedHeadersPanel.add(selectedHeadersScrollPane, BorderLayout.CENTER);

        // Make them sit side by side
        JPanel middlePanel = new JPanel(new GridLayout(1, 2, 10, 0)); // 1 row, 2 columns, 10px gap
        middlePanel.add(payloadPanel);
        middlePanel.add(selectedParamsPanel);
        middlePanel.add(selectedHeadersPanel);

        // Checkboxes
        replaceInRepeater = new JCheckBox("Replace Requests from Repeater");
        replaceInRepeater.addActionListener(e -> changeCheckBoxState());

        replaceInProxy = new JCheckBox("Replace Requests from Proxy");
        replaceInProxy.addActionListener(e -> changeCheckBoxState());

        urlParamsCheckBox = new JCheckBox("URL Params");
        urlParamsCheckBox.addActionListener(e -> changeCheckBoxState());

        bodyParamsCheckBox = new JCheckBox("Body Params");
        bodyParamsCheckBox.addActionListener(e -> changeCheckBoxState());

        jsonParamsCheckBox = new JCheckBox("JSON Body Params");
        jsonParamsCheckBox.addActionListener(e -> changeCheckBoxState());

        mustRequestBeInScopeCheckBox = new JCheckBox("Must Request be in Scope ?");
        mustRequestBeInScopeCheckBox.setSelected(true);
        mustRequestBeInScopeCheckBox.addActionListener(e -> changeCheckBoxState());

        mustParameterNameBeInSelectedParameterBoxCheckBox = new JCheckBox("Must Parameter Name Be in In Scope Parameters? [If you disable this, all parameters will be replaced!]");
        mustParameterNameBeInSelectedParameterBoxCheckBox.setSelected(true);
        mustParameterNameBeInSelectedParameterBoxCheckBox.addActionListener(e -> changeCheckBoxState());

        mustHeadersBeReplacedCheckBox = new JCheckBox("Must Headers Be Replaced ?");
        mustHeadersBeReplacedCheckBox.addActionListener(e -> changeCheckBoxState());

        mustHeadersBeInInScopeHeadersCheckBox = new JCheckBox("Must Headers Be In Scope Headers? [If you disable this, all headers' value will be replaced!]");
        mustHeadersBeInInScopeHeadersCheckBox.setSelected(true);
        mustHeadersBeInInScopeHeadersCheckBox.addActionListener(e -> changeCheckBoxState());

        JButton saveButton = new JButton("Save Settings");
        saveButton.addActionListener(e -> saveSettings());

        // Top panel for checkboxes
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(replaceInRepeater);
        topPanel.add(replaceInProxy);
        topPanel.add(urlParamsCheckBox);
        topPanel.add(bodyParamsCheckBox);
        topPanel.add(jsonParamsCheckBox);
        topPanel.add(mustRequestBeInScopeCheckBox);
        topPanel.add(mustParameterNameBeInSelectedParameterBoxCheckBox);
        topPanel.add(mustHeadersBeReplacedCheckBox);
        topPanel.add(mustHeadersBeInInScopeHeadersCheckBox);

        // Add everything to the main panel
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(middlePanel, BorderLayout.CENTER);
        panel.add(saveButton, BorderLayout.SOUTH);

        return panel;
    }

    private void changeCheckBoxState(){
        this.jprHttpHandler.setMustUrlParameterBeReplaced(urlParamsCheckBox.isSelected());
        this.jprHttpHandler.setMustBodyParameterBeReplaced(bodyParamsCheckBox.isSelected());
        this.jprHttpHandler.setMustJsonParameterBeReplaced(jsonParamsCheckBox.isSelected());

        this.jprHttpHandler.setMustHeadersBeReplaced(mustHeadersBeReplacedCheckBox.isSelected());
        this.jprHttpHandler.setMustHeadersBeInScope(mustHeadersBeInInScopeHeadersCheckBox.isSelected());

        this.jprHttpHandler.setEnableProxyToolType(replaceInProxy.isSelected());
        this.jprHttpHandler.setEnableRepeaterToolType(replaceInRepeater.isSelected());

        this.jprHttpHandler.setRequestMustBeInScope(mustRequestBeInScopeCheckBox.isSelected());

        this.jprHttpHandler.setParametersMustBeInScope(mustParameterNameBeInSelectedParameterBoxCheckBox.isSelected());

    }

    private void saveSettings() {
        payloads = Arrays.asList(payloadTextArea.getText().split("\n"));
        print_output("Payloads are Saved:\n" + payloads);

        selectedParameters = Arrays.asList(selectedParametersTextArea.getText().split("\n"));
        print_output("In-Scope parameters are Saved:\n" + selectedParameters);

        selectedHeaders = Arrays.asList(selectedHeadersTextArea.getText().split("\n"));
        print_output("In-Scope headers are Saved:\n" + selectedHeaders);

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
