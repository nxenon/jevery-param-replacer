package com.aminnasiri;

import burp.api.montoya.http.Http;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.UserInterface;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JprLoggerTab extends JPanel {
    private final HttpRequestEditor requestEditor;
    private final HttpResponseEditor responseEditor;
    private final DefaultTableModel tableModel;
    private final List<HttpRequestResponse> requestList;
    private final JTable requestTable;

    private JTextField methodFilterField;
    private JTextField urlFilterField;
    private JTextField statusFilterField;
    private JCheckBox sortByLengthCheckBox;

    public JprLoggerTab(Http httpObject, UserInterface userInterface) {
        setLayout(new BorderLayout());

        // ✅ Detect Burp's UI colors dynamically
        Color tableBackground = UIManager.getColor("Table.background");
        Color tableForeground = UIManager.getColor("Table.foreground");
        Color tableHeaderBackground = UIManager.getColor("TableHeader.background");
        Color tableHeaderForeground = UIManager.getColor("TableHeader.foreground");
        Color gridColor = UIManager.getColor("Table.gridColor");

        // 🎯 Fallback colors
        if (tableBackground == null) tableBackground = Color.WHITE;
        if (tableForeground == null) tableForeground = Color.BLACK;
        if (tableHeaderBackground == null) tableHeaderBackground = Color.LIGHT_GRAY;
        if (tableHeaderForeground == null) tableHeaderForeground = Color.BLACK;
        if (gridColor == null) gridColor = Color.GRAY;

        // 🎨 Define alternate row color
        Color alternateRowColor = tableBackground.darker();

        // 🛠 Create request and response editors
        this.requestEditor = userInterface.createHttpRequestEditor();
        this.responseEditor = userInterface.createHttpResponseEditor();

        // 📌 Define table columns
        String[] columnNames = {"#", "Method", "URL", "Status", "Length"};

        // 📌 Create a non-editable table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // 🖥 Create the JTable
        requestTable = new JTable(tableModel);
        requestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestTable.setRowHeight(22);
        requestTable.setFillsViewportHeight(true);
        requestTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        requestTable.getTableHeader().setReorderingAllowed(false);

        // ✅ Apply Burp-like colors
        requestTable.setBackground(tableBackground);
        requestTable.setForeground(tableForeground);
        requestTable.getTableHeader().setBackground(tableHeaderBackground);
        requestTable.getTableHeader().setForeground(tableHeaderForeground);
        requestTable.setGridColor(gridColor);

        // 🎯 Adjust column widths
        TableColumnModel columnModel = requestTable.getColumnModel();
        columnModel.getColumn(0).setMaxWidth(50);  // #
        columnModel.getColumn(1).setMaxWidth(75);  // Method
        columnModel.getColumn(3).setMaxWidth(60);  // Status
        columnModel.getColumn(4).setMaxWidth(80);  // Length

        // 📌 Right-align status and length columns
        DefaultTableCellRenderer rightAlignRenderer = new DefaultTableCellRenderer();
        rightAlignRenderer.setHorizontalAlignment(JLabel.RIGHT);
        requestTable.getColumnModel().getColumn(3).setCellRenderer(rightAlignRenderer);
        requestTable.getColumnModel().getColumn(4).setCellRenderer(rightAlignRenderer);

        // ✅ Fix alternating row colors
        Color finalTableBackground = tableBackground;
        Color finalTableForeground = tableForeground;
        requestTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? alternateRowColor : finalTableBackground);
                    c.setForeground(finalTableForeground);
                }
                return c;
            }
        });

        // 🖥 Scroll pane for the table
        JScrollPane tableScrollPane = new JScrollPane(requestTable);

        // 🔗 Create a split pane for request & response editors
        JSplitPane editorSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, requestEditor.uiComponent(), responseEditor.uiComponent());
        editorSplitPane.setResizeWeight(0.5);

        // 🔗 Main split pane (table + editors)
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, editorSplitPane);
        mainSplitPane.setResizeWeight(0.3);

        // 🔗 Add to the panel
        add(mainSplitPane, BorderLayout.CENTER);

        // 📌 Store requests
        requestList = new ArrayList<>();

        // 🖱 Handle row selection to display request/response
        requestTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = requestTable.getSelectedRow();
                if (selectedRow >= 0) {
                    HttpRequestResponse requestResponse = requestList.get(selectedRow);
                    requestEditor.setRequest(requestResponse.request());
                    if (requestResponse.response() != null) {
                        responseEditor.setResponse(requestResponse.response());
                    }
                }
            }
        });

        // 🔥 Create the filter panel
        createFilterPanel();
    }

    public void logRequestResponse(HttpRequestResponse requestResponse) {
        // Store the request-response pair
        requestList.add(requestResponse);

        // Extract request details
        String method = requestResponse.request().method();
        String url = requestResponse.request().url();
        String status = (requestResponse.response() != null) ? String.valueOf(requestResponse.response().statusCode()) : "-";
        int length = (requestResponse.response() != null) ? requestResponse.response().body().length() : 0;

        // Add to the table
        tableModel.insertRow(0, new Object[]{requestList.size(), method, url, status, length});

    }


    // ✅ Creates the filter panel without the "Custom" field
    private void createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        methodFilterField = new JTextField(6);
        urlFilterField = new JTextField(15);
        statusFilterField = new JTextField(4);
        sortByLengthCheckBox = new JCheckBox("Sort by Length");

        // 🔥 Apply filters on text input change
        methodFilterField.addActionListener(this::applyFilters);
        urlFilterField.addActionListener(this::applyFilters);
        statusFilterField.addActionListener(this::applyFilters);
        sortByLengthCheckBox.addActionListener(e -> applyFilters(null));

        filterPanel.add(new JLabel("Method:"));
        filterPanel.add(methodFilterField);
        filterPanel.add(new JLabel("URL:"));
        filterPanel.add(urlFilterField);
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(statusFilterField);
        filterPanel.add(sortByLengthCheckBox);

        add(filterPanel, BorderLayout.NORTH);
    }

    // ✅ Apply filters and sorting
    private void applyFilters(ActionEvent e) {
        String methodFilter = methodFilterField.getText().trim();
        String urlFilter = urlFilterField.getText().trim();
        String statusFilter = statusFilterField.getText().trim();

        List<HttpRequestResponse> filteredList = requestList.stream()
                .filter(req -> req.request().method().contains(methodFilter))
                .filter(req -> req.request().url().contains(urlFilter))
                .filter(req -> req.response() == null || String.valueOf(req.response().statusCode()).contains(statusFilter))
                .collect(Collectors.toList());

        // ✅ Sort logic
        if (sortByLengthCheckBox.isSelected()) {
            filteredList.sort((a, b) -> Integer.compare(b.response().body().length(), a.response().body().length()));
        }

        refreshTable(filteredList);
    }

    // ✅ Refreshes the table with the filtered list
    private void refreshTable(List<HttpRequestResponse> filteredList) {
        tableModel.setRowCount(0); // Clear existing rows

        int rowIndex = 1;
        for (HttpRequestResponse req : filteredList) {
            String method = req.request().method();
            String url = req.request().url();
            String status = (req.response() != null) ? String.valueOf(req.response().statusCode()) : "-";
            int length = (req.response() != null) ? req.response().body().length() : 0;

            tableModel.addRow(new Object[]{rowIndex++, method, url, status, length});
        }
    }
}
