import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantReviewSystem extends JFrame {
    private ReviewManager reviewManager;
    private JTable reviewTable;
    private DefaultTableModel tableModel;

    public RestaurantReviewSystem() {
        reviewManager = new ReviewManager();

        setTitle("Restaurant Review System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table for displaying reviews
        tableModel = new DefaultTableModel(new String[]{"ID", "Customer Name", "Restaurant Name", "Rating", "Review"}, 0);
        reviewTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(reviewTable);
        add(scrollPane, BorderLayout.CENTER);

        // Panel for review input
        JPanel inputPanel = new JPanel(new GridLayout(6, 2));
        JTextField customerNameField = new JTextField();
        JTextField restaurantNameField = new JTextField();
        JTextField ratingField = new JTextField();
        JTextField reviewField = new JTextField();
        JButton addButton = new JButton("Add Review");
        JButton updateButton = new JButton("Update Review");
        JButton deleteButton = new JButton("Delete Review");

        inputPanel.add(new JLabel("Customer Name:"));
        inputPanel.add(customerNameField);
        inputPanel.add(new JLabel("Restaurant Name:"));
        inputPanel.add(restaurantNameField);
        inputPanel.add(new JLabel("Rating:"));
        inputPanel.add(ratingField);
        inputPanel.add(new JLabel("Review:"));
        inputPanel.add(reviewField);
        inputPanel.add(addButton);
        inputPanel.add(updateButton);
        inputPanel.add(deleteButton);

        add(inputPanel, BorderLayout.SOUTH);

        // Load reviews into the table
        loadReviews();

        // Image in the middle of the GUI
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            ImageIcon imageIcon = new ImageIcon("burgir.jpg");
            Image image = imageIcon.getImage();
            Image scaledImage = image.getScaledInstance(400, 300, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            imageLabel.setText("Image not found");
        }
        add(imageLabel, BorderLayout.NORTH);

        // Add review button action
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reviewManager.addReview(customerNameField.getText(), restaurantNameField.getText(), Float.parseFloat(ratingField.getText()), reviewField.getText());
                loadReviews();
                clearFields(customerNameField, restaurantNameField, ratingField, reviewField);
            }
        });

        // Update review button action
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = reviewTable.getSelectedRow();
                if (selectedRow != -1) {
                    int id = (int) tableModel.getValueAt(selectedRow, 0);
                    reviewManager.updateReview(id, customerNameField.getText(), restaurantNameField.getText(), Float.parseFloat(ratingField.getText()), reviewField.getText());
                    loadReviews();
                    clearFields(customerNameField, restaurantNameField, ratingField, reviewField);
                } else {
                    JOptionPane.showMessageDialog(null, "Please select a review to update.");
                }
            }
        });

        // Delete review button action
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = reviewTable.getSelectedRow();
                if (selectedRow != -1) {
                    int id = (int) tableModel.getValueAt(selectedRow, 0);
                    reviewManager.deleteReview(id);
                    loadReviews();
                } else {
                    JOptionPane.showMessageDialog(null, "Please select a review to delete.");
                }
            }
        });

        // Save reviews on exit
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                reviewManager.saveReviews();
                System.exit(0);
            }
        });
    }

    private void loadReviews() {
        tableModel.setRowCount(0);
        List<Review> reviews = reviewManager.getReviews();
        for (Review review : reviews) {
            tableModel.addRow(new Object[]{review.getId(), review.getCustomerName(), review.getRestaurantName(), review.getRating(), review.getReview()});
        }
    }

    private void clearFields(JTextField customerNameField, JTextField restaurantNameField, JTextField ratingField, JTextField reviewField) {
        customerNameField.setText("");
        restaurantNameField.setText("");
        ratingField.setText("");
        reviewField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new RestaurantReviewSystem().setVisible(true);
            }
        });
    }
}

class Review {
    private int id;
    private String customerName;
    private String restaurantName;
    private float rating;
    private String review;

    public Review(int id, String customerName, String restaurantName, float rating, String review) {
        this.id = id;
        this.customerName = customerName;
        this.restaurantName = restaurantName;
        this.rating = rating;
        this.review = review;
    }

    public int getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public float getRating() {
        return rating;
    }

    public String getReview() {
        return review;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setReview(String review) {
        this.review = review;
    }

    @Override
    public String toString() {
        return id + ";" + customerName + ";" + restaurantName + ";" + rating + ";" + review;
    }

    public static Review fromString(String str) {
        String[] parts = str.split(";");
        return new Review(Integer.parseInt(parts[0]), parts[1], parts[2], Float.parseFloat(parts[3]), parts[4]);
    }
}

class ReviewManager {
    private List<Review> reviews;
    private int nextId;
    private final String filePath = "reviews.txt";

    public ReviewManager() {
        reviews = new ArrayList<>();
        loadReviews();
        nextId = reviews.size() > 0 ? reviews.get(reviews.size() - 1).getId() + 1 : 1;
    }

    public void addReview(String customerName, String restaurantName, float rating, String review) {
        reviews.add(new Review(nextId++, customerName, restaurantName, rating, review));
        saveReviews();
    }

    public void updateReview(int id, String customerName, String restaurantName, float rating, String review) {
        for (Review r : reviews) {
            if (r.getId() == id) {
                r.setCustomerName(customerName);
                r.setRestaurantName(restaurantName);
                r.setRating(rating);
                r.setReview(review);
                break;
            }
        }
        saveReviews();
    }

    public void deleteReview(int id) {
        reviews.removeIf(r -> r.getId() == id);
        saveReviews();
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void loadReviews() {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                reviews.add(Review.fromString(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveReviews() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (Review review : reviews) {
                bw.write(review.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
