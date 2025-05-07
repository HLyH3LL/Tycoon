
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util*;
import java.text.DecimalFormat;

public class TycoonGame {

    public static void main(String[] args) {
        // Run GUI creation on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                new LandingPage();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error initializing game:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }); 
    }

    // Landing page where user enters name and starts game with randomized goal
    static class LandingPage extends JFrame {
        private JTextField nameField;
        private JButton startButton;
        private JLabel infoLabel;

        public LandingPage() {
            setTitle("Tycoon Game - Landing Page");
            setSize(400, 200);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            nameField = new JTextField(20);
            startButton = new JButton("Start Game");
            infoLabel = new JLabel("Enter your name and start the game.");
            infoLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.insets = new Insets(10, 10, 5, 10);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            panel.add(infoLabel, gbc);

            gbc.insets = new Insets(5, 10, 5, 10);
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            panel.add(new JLabel("Your Name:"), gbc);

            gbc.gridx = 1;
            panel.add(nameField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            panel.add(startButton, gbc);

            add(panel);

            startButton.addActionListener(e -> startGame());

            setVisible(true);
        }

        private void startGame() {
            try {
                String playerName = nameField.getText().trim();
                if (playerName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter your name.", "Missing Name", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Game game = new Game(playerName);
                new GameGUI(game);
                this.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error starting game:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    static class Game {
        private String playerName;
        private List<Product> products;
        private List<Employee> employees;
        private double revenue;
        private int weeks;
        private boolean isBankrupt;
        private Goal goal;
        private Random random;
        private double promotionBudget;
        private double innovationBudget;
        private double startupBudget;

        public Game(String playerName) {
            this.playerName = playerName;
            products = new ArrayList<>();
            employees = new ArrayList<>();
            revenue = 0;
            weeks = 0;
            isBankrupt = false;
            random = new Random();
            promotionBudget = 500;
            innovationBudget = 500;
            startupBudget = 10000 + random.nextInt(50000); // Random startup budget between $10,000 and $60,000
            revenue += startupBudget; // Add startup budget to initial revenue
            generateRandomGoal();
        }

        private void generateRandomGoal() {
            int goalType = random.nextInt(3);
            switch (goalType) {
                case 0: {
                    int revenueGoal = 50000 + random.nextInt(50000);
                    int weeksLimit = 10 + random.nextInt(10);
                    goal = new Goal("Achieve revenue of $" + revenueGoal + " within " + weeksLimit + " weeks",
                            () -> revenue >= revenueGoal && weeks <= weeksLimit);
                    break;
                }
                case 1: {[]
                    double qualityGoal = 70 + random.nextInt(31);
                    int weeksLimit2 = 15 + random.nextInt(10);
                    goal = new Goal(String.format("Achieve average product quality of %.0f within %d weeks", qualityGoal, weeksLimit2),
                            () -> averageProductQuality() >= qualityGoal && weeks <= weeksLimit2);
                    break;
                }
                case 2: {
                    int userGoal = 1000 + random.nextInt(4000);
                    int weeksLimit3 = 8 + random.nextInt(10);
                    goal = new Goal("Achieve total users of " + userGoal + " within " + weeksLimit3 + " weeks",
                            () -> totalUsers() >= userGoal && weeks <= weeksLimit3);
                    break;
                }
                default:
                    goal = new Goal("No goal set", () -> false);
                    break;
            }
        }

        public void progressWeek() {
            weeks++;
            if (isBankrupt) return;

            double totalRevenueThisWeek = 0;

            for (Product p : products) {
                int userGrowth = (int) (p.getQuality() * 1.5 + p.getPromotionEffect() * 10 + employeesHappinessAverage() / 10);
                p.increaseUsers(userGrowth);

                double revenueFromProduct = p.getUsers() * p.getPrice();
                totalRevenueThisWeek += revenueFromProduct;

                if (employeesHappinessAverage() < 40) {
                    p.degradeQuality(1);
                }
            }

            revenue += totalRevenueThisWeek;

            double payroll = 0;
            for (Employee e : employees) {
                payroll += e.getSalary();
                if (e.getSalary() < 300) e.decreaseHappiness(5);
            }
            revenue -= payroll;

            revenue -= (promotionBudget + innovationBudget);

            if (revenue < 0) isBankrupt = true;

            // Trigger a random event at the end of the week
            triggerRandomEvent();
        }

        private void triggerRandomEvent() {
            if (isBankrupt) return; // No events if bankrupt

            String[] events = {
                    "A new investor has joined your company! You receive $5000.",
                    "Market trends have shifted, causing a decline in product sales. Lose $2000.",
                    "A competitor has launched a similar product. Lose $3000 in revenue.",
                    "Your product has gone viral! Gain $7000 in revenue.",
                    "A new technology has emerged, increasing your innovation budget by $2000.",
                    "Employee morale is low, resulting in a $1000 loss in revenue."
            };

            int eventIndex = random.nextInt(events.length);
            String eventMessage = events[eventIndex];

            switch (eventIndex) {
                case 0:
                    revenue += 5000;
                    break;
                case 1:
                    revenue -= 2000;
                    break;
                case 2:
                    revenue -= 3000;
                    break;
                case 3:
                    revenue += 7000;
                    break;
                case 4:
                    innovationBudget += 2000;
                    break;
                case 5:
                    revenue -= 1000;
                    break;
            }

            JOptionPane.showMessageDialog(null, eventMessage, "Random Event", JOptionPane.INFORMATION_MESSAGE);
        }

        private double averageProductQuality() {
            if (products.isEmpty()) return 0;
            double totalQuality = 0;
            for (Product p : products) totalQuality += p.getQuality();
            return totalQuality / products.size();
        }

        private int totalUsers() {
            int total = 0;
            for (Product p : products) total += p.getUsers();
            return total;
        }

        public double employeesHappinessAverage() {
            if (employees.isEmpty()) return 100;
            double total = 0;
            for (Employee e : employees) total += e.getHappiness();
            return total / employees.size();
        }

        public void developProduct(String name, double price, String field) {
            products.add(new Product(name, price, field));
        }

        public void hireEmployee(String name, double salary) {
            employees.add(new Employee(name, salary));
        }

        public void fireEmployee(int index) {
            if (index >= 0 && index < employees.size()) employees.remove(index);
        }

        public void increaseEmployeeSalary(int index, double amount) {
            if (index >= 0 && index < employees.size()) {
                employees.get(index).increaseSalary(amount);
                employees.get(index).increaseHappiness(amount * 0.1);
            }
        }

        public void promoteProduct(int index, double budget) {
            if (index >= 0 && index < products.size()) {
                if (budget <= promotionBudget) {
                    products.get(index).promote(budget);
                    promotionBudget -= budget;
                }
            }
        }

        public void improveProductQuality(int index, double budget) {
            if (index >= 0 && index < products.size()) {
                if (budget <= innovationBudget) {
                    products.get(index).improveQuality(budget);
                    innovationBudget -= budget;
                }
            }
        }

        public void addPromotionBudget(double amount) {
            promotionBudget += amount;
        }

        public void addInnovationBudget(double amount) {
            innovationBudget += amount;
        }

        public boolean checkWin() {
            if (isBankrupt) return false;
            return goal.isAchieved();
        }

        public String getPlayerName() { return playerName; }
        public int getWeeks() { return weeks; }
        public double getRevenue() { return revenue; }
        public boolean isBankrupt() { return isBankrupt; }
        public Goal getGoal() { return goal; }
        public List<Product> getProducts() { return products; }
        public List<Employee> getEmployees() { return employees; }
        public double getPromotionBudget() { return promotionBudget; }
        public double getInnovationBudget() { return innovationBudget; }
        public double getStartupBudget() { return startupBudget; }
    }

    static class Goal {
        private String description;
        private GoalCondition condition;

        public Goal(String description, GoalCondition condition) {
            this.description = description;
            this.condition = condition;
        }

        public String getDescription() { return description; }
        public boolean isAchieved() { return condition.isReached(); }

        interface GoalCondition {
            boolean isReached();
        }
    }

    static class Product {
        private String name;
        private double price;
        private String field;
        private double quality;
        private double promotionEffect;
        private int users;

        public Product(String name, double price, String field) {
            this.name = name;
            this.price = price;
            this.field = field;
            this.quality = 50 + Math.random() * 30;
            this.promotionEffect = 0;
            this.users = 10;
        }

        public void promote(double budget) {
            promotionEffect += budget * 0.05;
            if (promotionEffect > 100) promotionEffect = 100;
        }

        public void improveQuality(double innovationBudget) {
            quality += innovationBudget * 0.1;
            if (quality > 100) quality = 100;
        }

        public void degradeQuality(double amount) {
            quality -= amount;
            if (quality < 0) quality = 0;
        }

        public void increaseUsers(int amount) {
            users += amount;
            if (users < 0) users = 0;
        }

        // Getters
        public String getName() { return name; }
        public double getPrice() { return price; }
        public String getField() { return field; }
        public double getQuality() { return quality; }
        public double getPromotionEffect() { return promotionEffect; }
        public int getUsers() { return users; }
    }

    static class Employee {
        private String name;
        private double salary;
        private double happiness;

        public Employee(String name, double salary) {
            this.name = name;
            this.salary = salary;
            this.happiness = 50 + Math.random() * 30;
        }

        public void increaseSalary(double amount) {
            salary += amount;
        }

        public void increaseHappiness(double amount) {
            happiness += amount;
            if (happiness > 100) happiness = 100;
        }

        public void decreaseHappiness(double amount) {
            happiness -= amount;
            if (happiness < 0) happiness = 0;
        }

        // Getters
        public String getName() { return name; }
        public double getSalary() { return salary; }
        public double getHappiness() { return happiness; }
    }

    static class GameGUI extends JFrame {
        private Game game;

        private JLabel playerNameLabel, weekLabel, revenueLabel, goalLabel, bankruptLabel;
        private JLabel promoBudgetLabel, innovationBudgetLabel, empHappinessLabel, startupBudgetLabel;
        private DefaultListModel<String> productListModel, employeeListModel;
        private JList<String> productList, employeeList;

        private JButton nextWeekButton, developProductButton, hireEmployeeButton, fireEmployeeButton;
        private JButton increaseSalaryButton, promoteProductButton, improveQualityButton;
        private JButton addPromotionBudgetButton, addInnovationBudgetButton, saveGameButton, surrenderButton;

        private DecimalFormat df = new DecimalFormat("#,##0.00");

        public GameGUI(Game game) {
            this.game = game;
            setTitle("Tycoon Game - Player: " + game.getPlayerName());
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(1000, 700);
            setLocationRelativeTo(null);

            initializeComponents();

            setVisible(true);
        }

        private void initializeComponents() {
            setLayout(new BorderLayout());

            JPanel topPanel = new JPanel(new GridLayout(3, 1));
            JPanel playerInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            playerNameLabel = new JLabel("Player: " + game.getPlayerName());
            weekLabel = new JLabel("Week: 0");
            revenueLabel = new JLabel("Revenue: $0.00");
            startupBudgetLabel = new JLabel("Startup Budget: $" + df.format(game.getStartupBudget()));
            bankruptLabel = new JLabel("");
            bankruptLabel.setForeground(Color.RED);
            goalLabel = new JLabel("Goal: " + game.getGoal().getDescription());
            playerInfoPanel.add(playerNameLabel);
            playerInfoPanel.add(Box.createHorizontalStrut(20));
            playerInfoPanel.add(weekLabel);
            playerInfoPanel.add(Box.createHorizontalStrut(20));
            playerInfoPanel.add(revenueLabel);
            playerInfoPanel.add(Box.createHorizontalStrut(20));
            playerInfoPanel.add(startupBudgetLabel);
            playerInfoPanel.add(Box.createHorizontalStrut(20));
            playerInfoPanel.add(bankruptLabel);

            JPanel goalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            goalPanel.add(goalLabel);

            topPanel.add(playerInfoPanel);
            topPanel.add(goalPanel);
            add(topPanel, BorderLayout.NORTH);

            JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));

            JPanel productsPanel = new JPanel(new BorderLayout());
            productsPanel.setBorder(new TitledBorder("Products"));
            productListModel = new DefaultListModel<>();
            productList = new JList<>(productListModel);
            productsPanel.add(new JScrollPane(productList), BorderLayout.CENTER);

            JPanel productButtonPanel = new JPanel(new GridLayout(5, 1, 5, 5));
            developProductButton = new JButton("Develop Product");
            promoteProductButton = new JButton("Promote Product");
            improveQualityButton = new JButton("Improve Quality");
            productButtonPanel.add(developProductButton);
            productButtonPanel.add(promoteProductButton);
            productButtonPanel.add(improveQualityButton);

            productsPanel.add(productButtonPanel, BorderLayout.SOUTH);

            centerPanel.add(productsPanel);

            JPanel employeesPanel = new JPanel(new BorderLayout());
            employeesPanel.setBorder(new TitledBorder("Employees"));
            employeeListModel = new DefaultListModel<>();
            employeeList = new JList<>(employeeListModel);
            employeesPanel.add(new JScrollPane(employeeList), BorderLayout.CENTER);

            JPanel employeeButtonPanel = new JPanel(new GridLayout(5, 1, 5, 5));
            hireEmployeeButton = new JButton("Hire Employee");
            fireEmployeeButton = new JButton("Fire Employee");
            increaseSalaryButton = new JButton("Increase Salary");
            employeeButtonPanel.add(hireEmployeeButton);
            employeeButtonPanel.add(fireEmployeeButton);
            employeeButtonPanel.add(increaseSalaryButton);

            employeesPanel.add(employeeButtonPanel, BorderLayout.SOUTH);

            centerPanel.add(employeesPanel);

            add(centerPanel, BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            nextWeekButton = new JButton("Next Week");
            addPromotionBudgetButton = new JButton("Add Promotion $500");
            addInnovationBudgetButton = new JButton("Add Innovation $500");
            saveGameButton = new JButton("Save Game");
            surrenderButton = new JButton("Surrender");

            promoBudgetLabel = new JLabel("Promotion Budget: $" + df.format(game.getPromotionBudget()));
            innovationBudgetLabel = new JLabel("Innovation Budget: $" + df.format(game.getInnovationBudget()));
            empHappinessLabel = new JLabel(String.format("Avg Employee Happiness: %.1f", game.employeesHappinessAverage()));

            bottomPanel.add(nextWeekButton);
            bottomPanel.add(addPromotionBudgetButton);
            bottomPanel.add(addInnovationBudgetButton);
            bottomPanel.add(saveGameButton);
            bottomPanel.add(surrenderButton);
            bottomPanel.add(Box.createHorizontalStrut(20));
            bottomPanel.add(promoBudgetLabel);
            bottomPanel.add(Box.createHorizontalStrut(15));
            bottomPanel.add(innovationBudgetLabel);
            bottomPanel.add(Box.createHorizontalStrut(15));
            bottomPanel.add(empHappinessLabel);

            add(bottomPanel, BorderLayout.SOUTH);

            // Listeners
            nextWeekButton.addActionListener(e -> nextWeek());
            developProductButton.addActionListener(e -> developProductDialog());
            hireEmployeeButton.addActionListener(e -> hireEmployeeDialog());
            fireEmployeeButton.addActionListener(e -> fireEmployee());
            increaseSalaryButton.addActionListener(e -> increaseSalary());
            promoteProductButton.addActionListener(e -> promoteProductDialog());
            improveQualityButton.addActionListener(e -> improveQualityDialog());
            addPromotionBudgetButton.addActionListener(e -> addPromotionBudget());
            addInnovationBudgetButton.addActionListener(e -> addInnovationBudget());
            surrenderButton.addActionListener(e -> surrender());
            saveGameButton.addActionListener(e -> saveGame());

            refreshUI();
        }

        private void refreshUI() {
            weekLabel.setText("Week: " + game.getWeeks());
            revenueLabel.setText("Revenue: $" + df.format(game.getRevenue()));
            startupBudgetLabel.setText("Startup Budget: $" + df.format(game.getStartupBudget()));
            promoBudgetLabel.setText("Promotion Budget: $" + df.format(game.getPromotionBudget()));
            innovationBudgetLabel.setText("Innovation Budget: $" + df.format(game.getInnovationBudget()));
            empHappinessLabel.setText(String.format("Avg Employee Happiness: %.1f", game.employeesHappinessAverage()));
            bankruptLabel.setText(game.isBankrupt() ? "You are bankrupt! Game Over." : "");

            productListModel.clear();
            for (Product p : game.getProducts()) {
                productListModel.addElement(String.format("%s (Field: %s) - $%.2f, Quality: %.1f, Users: %d, Promotion: %.1f",
                        p.getName(), p.getField(), p.getPrice(), p.getQuality(), p.getUsers(), p.getPromotionEffect()));
            }

            employeeListModel.clear();
            for (Employee e : game.getEmployees()) {
                employeeListModel.addElement(String.format("%s - Salary: $%.2f, Happiness: %.1f", e.getName(), e.getSalary(), e.getHappiness()));
            }
        }

        private void nextWeek() {
            if (game.isBankrupt()) {
                JOptionPane.showMessageDialog(this, "You are bankrupt! Game over. Please restart.", "Game Over", JOptionPane.ERROR_MESSAGE);
                return;
            }
            game.progressWeek();
            refreshUI();

            if (game.checkWin()) {
                JOptionPane.showMessageDialog(this, "Congratulations! You achieved the goal:\n" + game.getGoal().getDescription(), "You Win!", JOptionPane.INFORMATION_MESSAGE);
                nextWeekButton.setEnabled(false);
            } else if (game.isBankrupt()) {
                JOptionPane.showMessageDialog(this, "You went bankrupt! Game Over.", "Game Over", JOptionPane.ERROR_MESSAGE);
                nextWeekButton.setEnabled(false);
            }
        }

        private void developProductDialog() {
            JTextField nameField = new JTextField();
            JTextField priceField = new JTextField();
            JTextField fieldField = new JTextField();

            Object[] message = {
                    "Product Name:", nameField,
                    "Price per Unit (e.g. 9.99):", priceField,
                    "Field (e.g. Technology, Food):", fieldField
            };

            int option = JOptionPane.showConfirmDialog(this, message, "Develop New Product", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    String name = nameField.getText().trim();
                    double price = Double.parseDouble(priceField.getText().trim());
                    String field = fieldField.getText().trim();

                    if (name.isEmpty() || price < 0 || field.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Invalid input. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    game.developProduct(name, price, field);
                    refreshUI();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid price format.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void hireEmployeeDialog() {
            JTextField nameField = new JTextField();
            JTextField salaryField = new JTextField();

            Object[] message = {
                    "Employee Name:", nameField,
                    "Salary (e.g. 500):", salaryField
            };

            int option = JOptionPane.showConfirmDialog(this, message, "Hire New Employee", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    String name = nameField.getText().trim();
                    double salary = Double.parseDouble(salaryField.getText().trim());

                    if (name.isEmpty() || salary < 0) {
                        JOptionPane.showMessageDialog(this, "Invalid input. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    game.hireEmployee(name, salary);
                    refreshUI();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid salary format.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void fireEmployee() {
            int index = employeeList.getSelectedIndex();
            if (index < 0) {
                JOptionPane.showMessageDialog(this, "Select an employee to fire.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to fire " + game.getEmployees().get(index).getName() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                game.fireEmployee(index);
                refreshUI();
            }
        }

        private void increaseSalary() {
            int index = employeeList.getSelectedIndex();
            if (index < 0) {
                JOptionPane.showMessageDialog(this, "Select an employee to increase salary.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String input = JOptionPane.showInputDialog(this, "Enter amount to increase salary by ($):");
            if (input != null) {
                try {
                    double amount = Double.parseDouble(input.trim());
                    if (amount <= 0) {
                        JOptionPane.showMessageDialog(this, "Amount must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    game.increaseEmployeeSalary(index, amount);
                    refreshUI();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid number format.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void promoteProductDialog() {
            int index = productList.getSelectedIndex();
            if (index < 0) {
                JOptionPane.showMessageDialog(this, "Select a product to promote.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String input = JOptionPane.showInputDialog(this, "Enter promotion budget to spend ($): Max $" + df.format(game.getPromotionBudget()));
            if (input != null) {
                try {
                    double budget = Double.parseDouble(input.trim());
                    if (budget <= 0 || budget > game.getPromotionBudget()) {
                        JOptionPane.showMessageDialog(this, "Invalid budget amount.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    game.promoteProduct(index, budget);
                    refreshUI();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid number format.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void improveQualityDialog() {
            int index = productList.getSelectedIndex();
            if (index < 0) {
                JOptionPane.showMessageDialog(this, "Select a product to improve quality.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String input = JOptionPane.showInputDialog(this, "Enter innovation budget to spend ($): Max $" + df.format(game.getInnovationBudget()));
            if (input != null) {
                try {
                    double budget = Double.parseDouble(input.trim());
                    if (budget <= 0 || budget > game.getInnovationBudget()) {
                        JOptionPane.showMessageDialog(this, "Invalid budget amount.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    game.improveProductQuality(index, budget);
                    refreshUI();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid number format.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void addPromotionBudget() {
            game.addPromotionBudget(500);
            refreshUI();
        }

        private void addInnovationBudget() {
            game.addInnovationBudget(500);
            refreshUI();
        }

        private void surrender() {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to surrender?", "Confirm Surrender", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, "You surrendered. Game Over.", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
        }

        private void saveGame() {
            JOptionPane.showMessageDialog(this, "Game saved successfully! (Not really, demo only)", "Save Game", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}