import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;
import java.util.regex.Pattern;

public class Main extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;

    private JTextField petNameField, breedField, ageField, ownerField, addressField, searchField;
    private JComboBox<String> genderCombo, petTypeCombo;
    private JButton registerButton, deleteButton;

    private JTextArea displayArea;
    private JTable petsTable;
    private DefaultTableModel petsTableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private CardLayout cardLayout;
    private JPanel contentPanel;

    public Main() {
        setTitle("🐾 Pet Registration System");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());

        // Sidebar
        JPanel sidePanel = new JPanel();
        sidePanel.setBackground(new Color(33, 33, 33));
        sidePanel.setPreferredSize(new Dimension(150, 0));
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));

        JButton dashboardButton = createSideButton("Dashboard");
        JButton petsButton = createSideButton("Pets");

        dashboardButton.addActionListener(e -> cardLayout.show(contentPanel, "dashboard"));
        petsButton.addActionListener(e -> {
            loadPetsFromDatabase();
            cardLayout.show(contentPanel, "pets");
        });

        sidePanel.add(Box.createVerticalStrut(30));
        sidePanel.add(dashboardButton);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(petsButton);
        sidePanel.add(Box.createVerticalGlue());
        getContentPane().add(sidePanel, BorderLayout.WEST);

        // Main Content
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        contentPanel.add(createDashboardPanel(), "dashboard");
        contentPanel.add(createPetsPanel(), "pets");
        cardLayout.show(contentPanel, "dashboard");

        setVisible(true);
    }

    private JButton createSideButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(130, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusPainted(false);
        button.setBackground(new Color(48, 63, 159));
        button.setForeground(Color.WHITE);
        return button;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Pet Registration", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 150, 243));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        formPanel.setBackground(Color.WHITE);

        int row = 0;
        addLabelAndField("Pet Name:", petNameField = new JTextField(), formPanel, row++);
        addLabelAndField("Pet Type:", petTypeCombo = new JComboBox<>(new String[]{"Dog", "Cat", "Bird", "Rabbit", "Others"}), formPanel, row++);
        addLabelAndField("Breed:", breedField = new JTextField(), formPanel, row++);
        addLabelAndField("Age:", ageField = new JTextField(), formPanel, row++);
        addLabelAndField("Gender:", genderCombo = new JComboBox<>(new String[]{"Male", "Female"}), formPanel, row++);
        addLabelAndField("Owner Name:", ownerField = new JTextField(), formPanel, row++);
        addLabelAndField("Owner Address:", addressField = new JTextField(), formPanel, row++);

        registerButton = new JButton("Register Pet");
        registerButton.setBackground(new Color(76, 175, 80));
        registerButton.setForeground(Color.WHITE);
        registerButton.addActionListener(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(registerButton, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setPreferredSize(new Dimension(600, 150));
        panel.add(scrollPane, BorderLayout.SOUTH);
        return panel;
    }

    private void addLabelAndField(String label, JComponent field, JPanel panel, int row) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.insets = new Insets(8, 8, 8, 8);
        gbcLabel.fill = GridBagConstraints.HORIZONTAL;
        gbcLabel.gridx = 0;
        gbcLabel.gridy = row;
        gbcLabel.anchor = GridBagConstraints.LINE_END;

        GridBagConstraints gbcField = new GridBagConstraints();
        gbcField.insets = new Insets(8, 8, 8, 8);
        gbcField.fill = GridBagConstraints.HORIZONTAL;
        gbcField.gridx = 1;
        gbcField.gridy = row;
        gbcField.weightx = 1.0;
        gbcField.anchor = GridBagConstraints.LINE_START;

        panel.add(new JLabel(label), gbcLabel);
        panel.add(field, gbcField);
    }

    private JPanel createPetsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Registered Pets", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(33, 150, 243));
        panel.add(title, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.add(new JLabel("Search Pet Name: "));
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        panel.add(searchPanel, BorderLayout.SOUTH);

        // Table setup
        String[] columns = {"ID", "Pet Name", "Pet Type", "Breed", "Age", "Gender", "Owner Name", "Owner Address"};
        petsTableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return column != 0; // ID not editable
            }
        };
        petsTable = new JTable(petsTableModel);
        petsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rowSorter = new TableRowSorter<>(petsTableModel);
        petsTable.setRowSorter(rowSorter);

        // Listener for table edits (update database)
        petsTableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int id = (int) petsTableModel.getValueAt(row, 0);
                try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pet_db", "root", "");
                     PreparedStatement stmt = con.prepareStatement("UPDATE pets SET pet_name=?, pet_type=?, breed=?, age=?, gender=?, owner_name=?, owner_address=? WHERE id=?")) {
                    stmt.setString(1, petsTableModel.getValueAt(row, 1).toString());
                    stmt.setString(2, petsTableModel.getValueAt(row, 2).toString());
                    stmt.setString(3, petsTableModel.getValueAt(row, 3).toString());
                    stmt.setString(4, petsTableModel.getValueAt(row, 4).toString());
                    stmt.setString(5, petsTableModel.getValueAt(row, 5).toString());
                    stmt.setString(6, petsTableModel.getValueAt(row, 6).toString());
                    stmt.setString(7, petsTableModel.getValueAt(row, 7).toString());
                    stmt.setInt(8, id);
                    stmt.executeUpdate();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Search filter logic
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                searchTable();
            }

            public void removeUpdate(DocumentEvent e) {
                searchTable();
            }

            public void changedUpdate(DocumentEvent e) {
                searchTable();
            }

            private void searchTable() {
                String text = searchField.getText();
                if (text.trim().isEmpty()) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 1)); // 1 = Pet Name
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(petsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        deleteButton = new JButton("Delete Pet");
        deleteButton.setBackground(new Color(244, 67, 54));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.addActionListener(e -> deletePet());

        btnPanel.add(deleteButton);
        panel.add(btnPanel, BorderLayout.NORTH);

        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == registerButton) {
            registerPet();
        }
    }

    private void registerPet() {
        String petName = petNameField.getText().trim();
        String petType = (String) petTypeCombo.getSelectedItem();
        String breed = breedField.getText().trim();
        String age = ageField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String ownerName = ownerField.getText().trim();
        String ownerAddress = addressField.getText().trim();

        if (petName.isEmpty() || breed.isEmpty() || age.isEmpty() || ownerName.isEmpty() || ownerAddress.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int ageInt = Integer.parseInt(age);
            if (ageInt < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Age must be a non-negative integer.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pet_db", "root", "");
             PreparedStatement stmt = con.prepareStatement("INSERT INTO pets (pet_name, pet_type, breed, age, gender, owner_name, owner_address) VALUES (?, ?, ?, ?, ?, ?, ?)")) {

            stmt.setString(1, petName);
            stmt.setString(2, petType);
            stmt.setString(3, breed);
            stmt.setString(4, age);
            stmt.setString(5, gender);
            stmt.setString(6, ownerName);
            stmt.setString(7, ownerAddress);
            stmt.executeUpdate();

            displayArea.setText("Pet Registered Successfully!\n\n" + getPetDetailsString(petName, petType, breed, age, gender, ownerName, ownerAddress));
            clearForm();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePet() {
        int selectedRow = petsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a pet to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = petsTable.convertRowIndexToModel(selectedRow);
        int petId = (int) petsTableModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this pet?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pet_db", "root", "");
             PreparedStatement stmt = con.prepareStatement("DELETE FROM pets WHERE id=?")) {
            stmt.setInt(1, petId);
            stmt.executeUpdate();
            loadPetsFromDatabase();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting pet: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPetsFromDatabase() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pet_db", "root", "");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM pets")) {

            petsTableModel.setRowCount(0);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("pet_name"));
                row.add(rs.getString("pet_type"));
                row.add(rs.getString("breed"));
                row.add(rs.getString("age"));
                row.add(rs.getString("gender"));
                row.add(rs.getString("owner_name"));
                row.add(rs.getString("owner_address"));
                petsTableModel.addRow(row);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading pets: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        petNameField.setText("");
        petTypeCombo.setSelectedIndex(0);
        breedField.setText("");
        ageField.setText("");
        genderCombo.setSelectedIndex(0);
        ownerField.setText("");
        addressField.setText("");
    }

    private String getPetDetailsString(String petName, String petType, String breed, String age, String gender, String ownerName, String ownerAddress) {
        return String.format("Pet Name: %s%nPet Type: %s%nBreed: %s%nAge: %s%nGender: %s%nOwner Name: %s%nOwner Address: %s",
                petName, petType, breed, age, gender, ownerName, ownerAddress);
    }

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "MySQL JDBC Driver not found. Please add it to your project.", "Driver Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(Main::new);
    }
}
