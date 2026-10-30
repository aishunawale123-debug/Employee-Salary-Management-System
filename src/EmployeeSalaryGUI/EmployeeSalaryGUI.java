package EmployeeSalaryGUI;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;

public class EmployeeSalaryGUI extends JFrame {

    static class Employee implements Comparable<Employee>, Serializable {
        private static final long serialVersionUID = 1L;

        int id;
        String name, department;
        double salary;

        Employee(int id, String name, String department, double salary) {
            this.id = id;
            this.name = name;
            this.department = department;
            this.salary = salary;
        }

        public int compareTo(Employee other) {
            return Double.compare(this.salary, other.salary);
        }
    }

    ArrayList<Employee> employees = new ArrayList<>();

    JTextField idField = new JTextField();
    JTextField nameField = new JTextField();
    JTextField departmentField = new JTextField();
    JTextField salaryField = new JTextField();

    DefaultTableModel model;
    JTable table;

    JLabel totalLabel = new JLabel();
    JLabel highestLabel = new JLabel();
    JLabel averageLabel = new JLabel();

    final String FILE_NAME = "employees.dat";

    public EmployeeSalaryGUI() {
        setTitle("Employee Salary Management System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel(
            "EMPLOYEE SALARY MANAGEMENT SYSTEM",
            SwingConstants.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 23));
        title.setForeground(Color.WHITE);
        title.setBackground(new Color(30, 65, 120));
        title.setOpaque(true);
        title.setBorder(BorderFactory.createEmptyBorder(18, 5, 18, 5));
        add(title, BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JPanel dashboard = new JPanel(new GridLayout(1, 3, 10, 0));
        styleLabel(totalLabel);
        styleLabel(highestLabel);
        styleLabel(averageLabel);

        dashboard.add(totalLabel);
        dashboard.add(highestLabel);
        dashboard.add(averageLabel);
        main.add(dashboard, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Employee Details"));

        addField(form, "Employee ID:", idField);
        addField(form, "Employee Name:", nameField);
        addField(form, "Department:", departmentField);
        addField(form, "Monthly Salary:", salaryField);

        JPanel buttons = new JPanel(new GridLayout(3, 3, 8, 8));
        buttons.setBorder(BorderFactory.createTitledBorder("Operations"));

        JButton add = new JButton("Add Employee");
        JButton update = new JButton("Update Employee");
        JButton delete = new JButton("Delete Employee");
        JButton clear = new JButton("Cancel / Clear");
        JButton lowHigh = new JButton("Salary Low to High");
        JButton highLow = new JButton("Salary High to Low");
        JButton highest = new JButton("Highest Salary");
        JButton average = new JButton("Average Salary");
        JButton search = new JButton("Search by ID");

        buttons.add(add);
        buttons.add(update);
        buttons.add(delete);
        buttons.add(clear);
        buttons.add(lowHigh);
        buttons.add(highLow);
        buttons.add(highest);
        buttons.add(average);
        buttons.add(search);

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.add(form, BorderLayout.NORTH);
        left.add(buttons, BorderLayout.CENTER);

        model = new DefaultTableModel(
            new String[]{"Employee ID", "Employee Name",
                         "Department", "Salary (₹)"}, 0
        ) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(27);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(
            BorderFactory.createTitledBorder("Employee Records")
        );
        scroll.setPreferredSize(new Dimension(900, 260));

        main.add(left, BorderLayout.CENTER);
        main.add(scroll, BorderLayout.SOUTH);
        add(main, BorderLayout.CENTER);

        loadEmployees();

        add.addActionListener(e -> addEmployee());
        update.addActionListener(e -> updateEmployee());
        delete.addActionListener(e -> deleteEmployee());
        clear.addActionListener(e -> clearFields());

        lowHigh.addActionListener(e -> {
            Collections.sort(employees);
            refreshTable();
        });

        highLow.addActionListener(e -> {
            employees.sort(
                Comparator.comparingDouble((Employee emp) -> emp.salary)
                          .reversed()
            );
            refreshTable();
        });

        highest.addActionListener(e -> showHighestSalary());
        average.addActionListener(e -> showAverageSalary());
        search.addActionListener(e -> searchEmployee());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int row = table.getSelectedRow();
                idField.setText(model.getValueAt(row, 0).toString());
                nameField.setText(model.getValueAt(row, 1).toString());
                departmentField.setText(model.getValueAt(row, 2).toString());
                salaryField.setText(model.getValueAt(row, 3).toString());
            }
        });

        refreshTable();
    }

    void styleLabel(JLabel label) {
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 15));
        label.setForeground(new Color(30, 65, 120));
        label.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    }

    void addField(JPanel panel, String text, JTextField field) {
        panel.add(new JLabel(text));
        panel.add(field);
    }

    Employee readEmployee() {
        int id = Integer.parseInt(idField.getText().trim());
        String name = nameField.getText().trim();
        String dept = departmentField.getText().trim();
        double salary = Double.parseDouble(salaryField.getText().trim());

        if (id <= 0 || name.isEmpty() || dept.isEmpty()
                || !Double.isFinite(salary) || salary < 0) {
            throw new IllegalArgumentException(
                "Enter valid employee details."
            );
        }

        return new Employee(id, name, dept, salary);
    }

    void addEmployee() {
        try {
            Employee emp = readEmployee();

            for (Employee old : employees) {
                if (old.id == emp.id) {
                    message("Employee ID already exists.");
                    return;
                }
            }

            employees.add(emp);
            saveEmployees();
            refreshTable();
            clearFields();
            message("Employee added successfully!");

        } catch (NumberFormatException ex) {
            message("ID must be an integer and salary must be numeric.");
        } catch (IllegalArgumentException ex) {
            message(ex.getMessage());
        }
    }

    void updateEmployee() {
        int row = table.getSelectedRow();

        if (row < 0) {
            message("Select an employee from the table first.");
            return;
        }

        try {
            Employee updated = readEmployee();
            int oldId = Integer.parseInt(
                model.getValueAt(row, 0).toString()
            );

            for (Employee emp : employees) {
                if (emp.id == updated.id && emp.id != oldId) {
                    message("Employee ID already exists.");
                    return;
                }
            }

            for (int i = 0; i < employees.size(); i++) {
                if (employees.get(i).id == oldId) {
                    employees.set(i, updated);
                    break;
                }
            }

            saveEmployees();
            refreshTable();
            clearFields();
            message("Employee updated successfully!");

        } catch (NumberFormatException ex) {
            message("Enter a valid ID and salary.");
        } catch (IllegalArgumentException ex) {
            message(ex.getMessage());
        }
    }

    void deleteEmployee() {
        int row = table.getSelectedRow();

        if (row < 0) {
            message("Select an employee to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this, "Delete selected employee?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            int id = Integer.parseInt(
                model.getValueAt(row, 0).toString()
            );

            employees.removeIf(emp -> emp.id == id);
            saveEmployees();
            refreshTable();
            clearFields();
            message("Employee deleted successfully!");
        }
    }

    void searchEmployee() {
        try {
            int id = Integer.parseInt(idField.getText().trim());

            for (int i = 0; i < model.getRowCount(); i++) {
                int tableId = Integer.parseInt(
                    model.getValueAt(i, 0).toString()
                );

                if (tableId == id) {
                    table.setRowSelectionInterval(i, i);
                    table.scrollRectToVisible(
                        table.getCellRect(i, 0, true)
                    );
                    message("Employee found.");
                    return;
                }
            }

            message("Employee not found.");

        } catch (NumberFormatException ex) {
            message("Enter a valid Employee ID.");
        }
    }

    void showHighestSalary() {
        if (employees.isEmpty()) {
            message("No employee records available.");
            return;
        }

        Employee emp = Collections.max(
            employees,
            Comparator.comparingDouble((Employee e) -> e.salary)
        );

        message("Highest Salary Employee: " + emp.name
            + "\nEmployee ID: " + emp.id
            + "\nSalary: ₹" + String.format("%.2f", emp.salary));
    }

    void showAverageSalary() {
        if (employees.isEmpty()) {
            message("No employee records available.");
            return;
        }

        double total = 0;

        for (Employee emp : employees) {
            total += emp.salary;
        }

        message("Average Salary: ₹"
            + String.format("%.2f", total / employees.size()));
    }

    void refreshTable() {
        model.setRowCount(0);

        for (Employee emp : employees) {
            model.addRow(new Object[]{
                emp.id, emp.name, emp.department,
                String.format("%.2f", emp.salary)
            });
        }

        totalLabel.setText("Total Employees: " + employees.size());

        if (employees.isEmpty()) {
            highestLabel.setText("Highest Salary: ₹0.00");
            averageLabel.setText("Average Salary: ₹0.00");
            return;
        }

        double total = 0;
        double max = employees.get(0).salary;

        for (Employee emp : employees) {
            total += emp.salary;
            max = Math.max(max, emp.salary);
        }

        highestLabel.setText(
            "Highest Salary: ₹" + String.format("%.2f", max)
        );

        averageLabel.setText(
            "Average Salary: ₹"
            + String.format("%.2f", total / employees.size())
        );
    }

    void clearFields() {
        idField.setText("");
        nameField.setText("");
        departmentField.setText("");
        salaryField.setText("");
        table.clearSelection();
    }

    void saveEmployees() {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(FILE_NAME))) {
            out.writeObject(employees);
        } catch (IOException ex) {
            message("Could not save employee records: " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    void loadEmployees() {
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(file))) {
            employees = (ArrayList<Employee>) in.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            message("Could not load saved records.");
        }
    }

    void message(String text) {
        JOptionPane.showMessageDialog(this, text);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EmployeeSalaryGUI gui = new EmployeeSalaryGUI();
            gui.setVisible(true);
        });
    }
}
