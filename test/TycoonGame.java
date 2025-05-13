import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*; 
import javax.swing.border.TitledBorder; 

public class TycoonGame {

    public static void main(String[] args) {
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
        private String currentEvent;
        private boolean eventTriggeredThisWeek;
        JLabel revenueLabel = new JLabel("Revenue: " + revenue);

        public Game(String playerName) {
            this.playerName = playerName;
            this.products = new ArrayList<>();
            this.employees = new ArrayList<>();
            this.revenue = 0;
            this.weeks = 0;
            this.isBankrupt = false;
            this.random = new Random();
            this.promotionBudget = 500;
            this.innovationBudget = 500;
            this.startupBudget = 10000 + random.nextInt(50000);
            this.revenue += startupBudget;
            this.currentEvent = "No events yet";
            this.eventTriggeredThisWeek = false;
            generateRandomGoal();
        }

        private void generateRandomGoal() {
            String[] goalTypes = {
                "Achieve revenue of $%,.0f within %d weeks",
                "Achieve average product quality of %.0f within %d weeks",
                "Achieve total users of %d within %d weeks",
                "Have at least %d products with quality above 80 within %d weeks",
                "Maintain employee happiness above %.0f for %d consecutive weeks"
            };
            
            String goalFormat = goalTypes[random.nextInt(goalTypes.length)];
            int weeksLimit = 10 + random.nextInt(20); // 10-30 weeks
            
            switch (goalFormat) {
                case "Achieve revenue of $%,.0f within %d weeks":
                    double revenueGoal = startupBudget * (2 + random.nextDouble() * 3); // 2x-5x startup
                    goal = new Goal(String.format(goalFormat, revenueGoal, weeksLimit),
                            () -> revenue >= revenueGoal && weeks <= weeksLimit);
                    break;
                case "Achieve average product quality of %.0f within %d weeks":
                    double qualityGoal = 70 + random.nextInt(31); // 70-100
                    goal = new Goal(String.format(goalFormat, qualityGoal, weeksLimit),
                            () -> averageProductQuality() >= qualityGoal && weeks <= weeksLimit);
                    break;
                case "Achieve total users of %d within %d weeks":
                    int userGoal = 1000 + random.nextInt(4000); // 1000-5000
                    goal = new Goal(String.format(goalFormat, userGoal, weeksLimit),
                            () -> totalUsers() >= userGoal && weeks <= weeksLimit);
                    break;
                case "Have at least %d products with quality above 80 within %d weeks":
                    int productCountGoal = 1 + random.nextInt(3); // 1-3 products
                    goal = new Goal(String.format(goalFormat, productCountGoal, weeksLimit),
                            () -> countHighQualityProducts() >= productCountGoal && weeks <= weeksLimit);
                    break;
                case "Maintain employee happiness above %.0f for %d consecutive weeks":
                    double happinessThreshold = 60 + random.nextInt(31); // 60-90
                    int consecutiveWeeks = 3 + random.nextInt(3); // 3-5 weeks
                    goal = new Goal(String.format(goalFormat, happinessThreshold, consecutiveWeeks),
                            () -> checkConsecutiveHappiness(happinessThreshold, consecutiveWeeks));
                    break;
                default:
                    goal = new Goal("No goal set", () -> false);
                    break;
            }
        }

        private int countHighQualityProducts() {
            int count = 0;
            for (Product p : products) {
                if (p.getQuality() > 80) count++;
            }
            return count;
        }

        private boolean checkConsecutiveHappiness(double threshold, int requiredWeeks) {
            // Simplified - would need to track happiness history for full implementation
            return employeesHappinessAverage() >= threshold && weeks >= requiredWeeks;
        }

       public void progressWeek() {
    weeks++;
    if (isBankrupt) return;

    eventTriggeredThisWeek = false;
    currentEvent = "No events this week";

    // 50% chance of an event happening
    if (random.nextDouble() < 0.5) {
        triggerRandomEvent();
        eventTriggeredThisWeek = true;
    }

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

    // Update the revenue label on the GUI
    revenueLabel.setText("Revenue: " + String.format("%.2f", revenue)); // Make sure 'revenueLabel' is the JLabel showing the revenue

    // Display the revenue added this week
    JOptionPane.showMessageDialog(null, 
        String.format("This week's revenue added: $%.2f", totalRevenueThisWeek), 
        "Weekly Revenue Update", JOptionPane.INFORMATION_MESSAGE);

    // Check for bankruptcy
    if (revenue < 0) {
        isBankrupt = true;
        // Call the method to show bankruptcy message
        showBankruptcyMessage();
    }
}


        private void showBankruptcyMessage() {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, 
                  "You have gone bankrupt! Your revenue has reached $0.\nGame Over.", 
                  "Bankruptcy", JOptionPane.ERROR_MESSAGE);
    });
}

        private void triggerRandomEvent() {
            if (isBankrupt) return;

            String[] positiveEvents = {
                "Economic Boom: Your products are in high demand! All product users increased by 25%.",
                "Tech Breakthrough: Your innovation team made a discovery! All product qualities increased by 15 points.",
                "Viral Marketing: Your marketing campaign went viral! All product promotion effects increased by 20 points.",
                "Tax Windfall: You received a tax refund! Revenue increased by $10,000.",
                "Employee Initiative: Your employees are highly motivated! All employee happiness increased by 20 points.",
                "Industry Recognition: Your products received awards! All product prices can be increased by 10% next week.",
                "Investor Interest: New investors are interested! Your promotion and innovation budgets increased by $2,000 each.",
                "Efficiency Gains: Your team found ways to reduce costs! Employee salaries reduced by 10% with no happiness loss.",
                "New Market: You've expanded to a new region! All product users increased by 1,000.",
                "Supplier Discount: Your suppliers offered better rates! Product development costs reduced by 20% next week."
            };

            String[] negativeEvents = {
                "Economic Recession: Customers are spending less. All product users decreased by 20%.",
                "Tech Glitch: A system failure hurt your products. All product qualities decreased by 10 points.",
                "PR Disaster: Negative publicity hurt your brand. All product promotion effects decreased by 15 points.",
                "Regulatory Fine: You were fined for violations! Revenue decreased by $5,000.",
                "Employee Strike: Your employees are unhappy! All employee happiness decreased by 25 points.",
                "Competitor Launch: A strong competitor entered your market. All product users decreased by 15%.",
                "Supply Chain Issues: Production delays hurt quality. All product qualities decreased by 5 points.",
                "Hacking Incident: Your systems were compromised. Revenue decreased by $3,000 and users by 10%.",
                "Key Employee Resignation: A star employee left. All employee happiness decreased by 10 points.",
                "Natural Disaster: A disaster disrupted operations. All product users decreased by 30% for this week."
            };

            boolean isPositive = random.nextBoolean();
            String[] events = isPositive ? positiveEvents : negativeEvents;
            int eventIndex = random.nextInt(events.length);
            currentEvent = "Week " + weeks + " Event: " + events[eventIndex];

            switch ((isPositive ? 0 : 10) + eventIndex) {
                case 0: // Economic Boom
                    for (Product p : products) {
                        p.increaseUsers((int)(p.getUsers() * 0.25));
                    }
                    break;
                case 1: // Tech Breakthrough
                    for (Product p : products) {
                        p.improveQuality(15);
                    }
                    break;
                case 2: // Viral Marketing
                    for (Product p : products) {
                        p.promote(400); // Equivalent to 20 points
                    }
                    break;
                case 3: // Tax Windfall
                    revenue += 10000;
                    break;
                case 4: // Employee Initiative
                    for (Employee e : employees) {
                        e.increaseHappiness(20);
                    }
                    break;
                case 5: // Industry Recognition
                    // Effect applied in product pricing next week
                    break;
                case 6: // Investor Interest
                    promotionBudget += 2000;
                    innovationBudget += 2000;
                    break;
                case 7: // Efficiency Gains
                    for (Employee e : employees) {
                        e.increaseSalary(-e.getSalary() * 0.1);
                    }
                    break;
                case 8: // New Market
                    for (Product p : products) {
                        p.increaseUsers(1000);
                    }
                    break;
                case 9: // Supplier Discount
                    // Effect applied in product development next week
                    break;
                case 10: // Economic Recession
                    for (Product p : products) {
                        p.increaseUsers(-(int)(p.getUsers() * 0.2));
                    }
                    break;
                case 11: // Tech Glitch
                    for (Product p : products) {
                        p.degradeQuality(10);
                    }
                    break;
                case 12: // PR Disaster
                    for (Product p : products) {
                        p.promote(-300); // Equivalent to -15 points
                    }
                    break;
                case 13: // Regulatory Fine
                    revenue -= 5000;
                    break;
                case 14: // Employee Strike
                    for (Employee e : employees) {
                        e.decreaseHappiness(25);
                    }
                    break;
                case 15: // Competitor Launch
                    for (Product p : products) {
                        p.increaseUsers(-(int)(p.getUsers() * 0.15));
                    }
                    break;
                case 16: // Supply Chain Issues
                    for (Product p : products) {
                        p.degradeQuality(5);
                    }
                    break;
                case 17: // Hacking Incident
                    revenue -= 3000;
                    for (Product p : products) {
                        p.increaseUsers(-(int)(p.getUsers() * 0.1));
                    }
                    break;
                case 18: // Key Employee Resignation
                    for (Employee e : employees) {
                        e.decreaseHappiness(10);
                    }
                    break;
                case 19: // Natural Disaster
                    for (Product p : products) {
                        p.increaseUsers(-(int)(p.getUsers() * 0.3));
                    }
                    break;
            }
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
        public String getCurrentEvent() { return currentEvent; }
        public boolean wasEventTriggeredThisWeek() { return eventTriggeredThisWeek; }
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
            if (promotionEffect < 0) promotionEffect = 0;
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
      
        public String getName() { return name; }
        public double getSalary() { return salary; }
        public double getHappiness() { return happiness; }
    }

    static class GameGUI extends JFrame {
        private Game game;
        private JLabel playerNameLabel, weekLabel, revenueLabel, goalLabel, bankruptLabel;
        private JLabel promoBudgetLabel, innovationBudgetLabel, empHappinessLabel, startupBudgetLabel, eventLabel;
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
            setSize(1000, 750); // Increased height to accommodate event label
            setLocationRelativeTo(null);

            initializeComponents();
            setVisible(true);
        }

        

        private void initializeComponents() {
            setLayout(new BorderLayout());

            JPanel topPanel = new JPanel(new GridLayout(4, 1)); // Changed to 4 rows for event label
            JPanel playerInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            playerNameLabel = new JLabel("Player: " + game.getPlayerName());
            weekLabel = new JLabel("Week: 0");
            revenueLabel = new JLabel("Revenue: $0.00");
            startupBudgetLabel = new JLabel("Startup Budget: $" + df.format(game.getStartupBudget()));
            bankruptLabel = new JLabel("");
            bankruptLabel.setForeground(Color.RED);
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
            goalLabel = new JLabel("Goal: " + game.getGoal().getDescription());
            goalPanel.add(goalLabel);

            // Add event panel
            JPanel eventPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            eventLabel = new JLabel("Event: " + game.getCurrentEvent());
            eventLabel.setForeground(new Color(0, 100, 0)); // Dark green for events
            eventPanel.add(eventLabel);

            topPanel.add(playerInfoPanel);
            topPanel.add(goalPanel);
            topPanel.add(eventPanel);
            add(topPanel, BorderLayout.NORTH);

            // Rest of the UI remains the same...
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
            
            // Update event label with color coding
            if (game.wasEventTriggeredThisWeek()) {
                eventLabel.setText("Event: " + game.getCurrentEvent());
                // Color positive events green and negative events red
                if (game.getCurrentEvent().contains("increased") || game.getCurrentEvent().contains("received") || 
                    game.getCurrentEvent().contains("viral") || game.getCurrentEvent().contains("recognition")) {
                    eventLabel.setForeground(new Color(0, 128, 0)); // Dark green
                } else if (game.getCurrentEvent().contains("decreased") || game.getCurrentEvent().contains("disaster") || 
                          game.getCurrentEvent().contains("strike") || game.getCurrentEvent().contains("fine")) {
                    eventLabel.setForeground(Color.RED);
                } else {
                    eventLabel.setForeground(Color.BLUE);
                }
            } else {
                eventLabel.setText("Event: No events this week");
                eventLabel.setForeground(Color.BLACK);
            }

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
                return;
            }
            game.progressWeek();
            refreshUI();

            if (game.checkWin()) {
                JOptionPane.showMessageDialog(this, 
                    String.format("Congratulations! You achieved the goal in %d weeks:\n%s", 
                        game.getWeeks(), game.getGoal().getDescription()), 
                    "You Win!", JOptionPane.INFORMATION_MESSAGE);
                nextWeekButton.setEnabled(false);
            } else if (game.isBankrupt()) {
                nextWeekButton.setEnabled(false);
            } else if (game.wasEventTriggeredThisWeek()) {
                // Event details are already shown in the event label
            }
        }

        // Rest of the methods remain the same...
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
                        JOptionPane.showMessageDialog(this, "Invalid input. Please try again.", "Error", JOptionPane.WARNING_MESSAGE);
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
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to fire " + game.getEmployees().get(index).getName() + "?", 
                "Confirm", JOptionPane.YES_NO_OPTION);
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
            String input = JOptionPane.showInputDialog(this, 
                "Enter promotion budget to spend ($): Max $" + df.format(game.getPromotionBudget()));
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
            String input = JOptionPane.showInputDialog(this, 
                "Enter innovation budget to spend ($): Max $" + df.format(game.getInnovationBudget()));
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
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to surrender?", 
                "Confirm Surrender", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, 
                    "You surrendered after " + game.getWeeks() + " weeks. Game Over.", 
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
        }

        private void saveGame() {
            JOptionPane.showMessageDialog(this, 
                "Game saved successfully! (Not really, demo only)", 
                "Save Game", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}