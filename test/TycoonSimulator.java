/*
 * Tycoon Simulator - Full Java Project
 * Technologies: Java SE, Java Swing, OOP, File I/O, Event Handling
 * Features: Startup Management, Turn-based Gameplay, Hiring System,
 * Progress Tracking, Random Events, Save/Load System
 */

 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.*;
 
 public class TycoonSimulator extends JFrame {
     private int week = 1;
     private int revenue = 12000;
     private int users = 1650;
     private int quality = 75;
     private int growth = 50;
 
     private ArrayList<Employee> employees = new ArrayList<>();
     private JTextArea employeeDisplay = new JTextArea();
     private JTextArea eventDisplay = new JTextArea();
     private JLabel statsLabel = new JLabel();
     private JLabel weekLabel = new JLabel();
 
     private Random random = new Random();
 
     public TycoonSimulator() {
         setTitle("Tycoon Simulator");
         setSize(800, 600);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setLayout(new BorderLayout());
 
         weekLabel.setText("Week: " + week);
         weekLabel.setFont(new Font("Arial", Font.BOLD, 24));
         weekLabel.setHorizontalAlignment(SwingConstants.CENTER);
         add(weekLabel, BorderLayout.NORTH);
 
         // Stats Panel
         statsLabel.setHorizontalAlignment(SwingConstants.CENTER);
         statsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
         statsLabel.setText(updateStats());
         add(statsLabel, BorderLayout.CENTER);
 
         // Employee display
         employeeDisplay.setEditable(false);
         updateEmployeeDisplay();
         add(new JScrollPane(employeeDisplay), BorderLayout.WEST);
 
         // Event display
         eventDisplay.setEditable(false);
         add(new JScrollPane(eventDisplay), BorderLayout.EAST);
 
         // Buttons
         JPanel buttonPanel = new JPanel(new GridLayout(2, 4));
         String[] buttons = {"Add Employee", "Next Week", "Save Game", "Load Game",
                             "Develop Product", "Remove Employee", "Promote Product", "Improve Quality", "Surrender"};
 
         for (String btnText : buttons) {
             JButton btn = new JButton(btnText);
             btn.addActionListener(this::handleButton);
             buttonPanel.add(btn);
         }
 
         add(buttonPanel, BorderLayout.SOUTH);
     }
 
     private String updateStats() {
         return String.format("<html><center>Revenue: %d PHP<br>Users: %d<br>Quality: %d%%<br>Growth: %d%%</center></html>",
                 revenue, users, quality, growth);
     }
 
     private void updateEmployeeDisplay() {
         StringBuilder sb = new StringBuilder("Employees:\n");
         for (Employee e : employees) {
             sb.append(String.format("%s (%s) - Happy: %d%% - Salary: %,d\n", e.name, e.getRole(), e.happiness, e.salary));
         }
         employeeDisplay.setText(sb.toString());
     }
 
     private void handleButton(ActionEvent e) {
         String cmd = e.getActionCommand();
 
         switch (cmd) {
             case "Add Employee":
                 String[] roles = {"Developer", "Marketer", "Sales"};
                 String role = (String) JOptionPane.showInputDialog(this, "Select Role:", "Hiring",
                         JOptionPane.PLAIN_MESSAGE, null, roles, roles[0]);
                 if (role != null) {
                     String name = JOptionPane.showInputDialog("Enter name:");
                     int salary = random.nextInt(10000) + 15000;
                     Employee newEmp;
                     switch (role) {
                         case "Developer": newEmp = new Developer(name, 100, salary); break;
                         case "Marketer": newEmp = new Marketer(name, 100, salary); break;
                         case "Sales": newEmp = new Salesperson(name, 100, salary); break;
                         default: newEmp = new Employee(name, role, 100, salary);
                     }
                     employees.add(newEmp);
                 }
                 break;
             case "Next Week":
                 week++;
                 revenue += growth * 1000;
                 users += growth * 100;
 
                 for (Employee emp : employees) {
                     revenue -= emp.salary;
                     emp.happiness -= random.nextInt(6);
                 }
 
                 triggerRandomEvents();
 
                 if (revenue <= 0) {
                     JOptionPane.showMessageDialog(this, "Your company went bankrupt on Week " + week + "! Game Over.");
                     System.exit(0);
                 }
                 break;
             case "Save Game":
                 try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("savegame.dat"))) {
                     oos.writeObject(week);
                     oos.writeObject(revenue);
                     oos.writeObject(users);
                     oos.writeObject(quality);
                     oos.writeObject(growth);
                     oos.writeObject(employees);
                 } catch (IOException ex) {
                     ex.printStackTrace();
                 }
                 break;
             case "Load Game":
                 try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("savegame.dat"))) {
                     week = (int) ois.readObject();
                     revenue = (int) ois.readObject();
                     users = (int) ois.readObject();
                     quality = (int) ois.readObject();
                     growth = (int) ois.readObject();
                     employees = (ArrayList<Employee>) ois.readObject();
                 } catch (IOException | ClassNotFoundException ex) {
                     ex.printStackTrace();
                 }
                 break;
             case "Develop Product":
                 quality += 5;
                 revenue += 5000;
                 break;
             case "Remove Employee":
                 if (!employees.isEmpty()) employees.remove(employees.size() - 1);
                 break;
             case "Promote Product":
                 growth += 10;
                 revenue += 2000;
                 break;
             case "Improve Quality":
                 quality += 10;
                 break;
             case "Surrender":
                 JOptionPane.showMessageDialog(this, "You surrendered on Week " + week);
                 System.exit(0);
         }
 
         weekLabel.setText("Week: " + week);
         statsLabel.setText(updateStats());
         updateEmployeeDisplay();
     }
 
     private void triggerRandomEvents() {
         eventDisplay.setText("");
         int eventChance = random.nextInt(100);
 
         if (eventChance < 30) {
             int funding = random.nextInt(500000) + 500000;
             revenue += funding;
             eventDisplay.append("Investor funding received: " + funding + " PHP\n");
         }
         if (eventChance > 70 && !employees.isEmpty()) {
             quality -= 10;
             eventDisplay.append("Employee burnout! Quality dropped.\n");
         }
     }
 
     public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> new TycoonSimulator().setVisible(true));
     }
 }
 
 class Employee implements Serializable {
     String name;
     String role;
     int happiness;
     int salary;
 
     public Employee(String name, String role, int happiness, int salary) {
         this.name = name;
         this.role = role;
         this.happiness = happiness;
         this.salary = salary;
     }
 
     public String getRole() {
         return role;
     }
 }
 
 class Developer extends Employee {
     public Developer(String name, int happiness, int salary) {
         super(name, "Developer", happiness, salary);
     }
 }
 
 class Marketer extends Employee {
     public Marketer(String name, int happiness, int salary) {
         super(name, "Marketer", happiness, salary);
     }
 }
 
 class Salesperson extends Employee {
     public Salesperson(String name, int happiness, int salary) {
         super(name, "Sales", happiness, salary);
     }
 }
 