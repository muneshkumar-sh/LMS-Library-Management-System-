import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.awt.print.*;

public class LibraryGUI {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel model;
    private LibrarySystem system;
    private Color darkBlue = new Color(41, 128, 185);
    private String currentRole = "GUEST";

    //ENUM FOR MESSAGE TYPES
    private enum MessageType {
        SUCCESS, ERROR, INFO
    }

    public LibraryGUI(LibrarySystem system) {
        this.system = system;
    }

    //ROLE SELECTION
    public void openLoginDialog() {
        JDialog d = new JDialog((JFrame)null, "System Access", true);
        d.setLayout(new BorderLayout());

        //stop the program
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        d.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });

        JPanel header=new JPanel(new BorderLayout());
        header.setBackground(darkBlue);
        header.setPreferredSize(new Dimension(450, 60));
        JLabel lblTitle=new JLabel("Select Access Level", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle);

        JPanel body=new JPanel(new GridLayout(1, 2, 20, 0));
        body.setBorder(new EmptyBorder(30, 30, 30, 30));

        JButton btnLib=new JButton("Librarian");
        JButton btnStd=new JButton("Student");
        styleSoftButton(btnLib, darkBlue);
        styleSoftButton(btnStd, new Color(46, 204, 113));

        btnLib.addActionListener(e -> {

            JDialog loginD=new JDialog(d, "Administrative Access", true);
            loginD.setLayout(new BorderLayout());

            //HEADER
            JPanel headerP=new JPanel(new BorderLayout());
            headerP.setBackground(darkBlue);
            headerP.setPreferredSize(new Dimension(400, 50));
            JLabel lblL = new JLabel("  Librarian Verification", JLabel.LEFT);
            lblL.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblL.setForeground(Color.WHITE);
            headerP.add(lblL, BorderLayout.CENTER);

            //INPUT
            JPanel bodyP = new JPanel(new GridBagLayout());
            bodyP.setBackground(new Color(245, 245, 245));
            bodyP.setBorder(new EmptyBorder(20, 30, 20, 30));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel lblP = new JLabel("Enter Admin Password:");
            lblP.setFont(new Font("Segoe UI", Font.BOLD, 14));
            JPasswordField passF = new JPasswordField();
            passF.setPreferredSize(new Dimension(200, 30));

            gbc.gridy = 0; bodyP.add(lblP, gbc);
            gbc.gridy = 1; bodyP.add(passF, gbc);

            //FOOTER BUTTONS
            JPanel footerP = new JPanel(new GridLayout(1, 2));
            JButton btnIn = new JButton("Login");
            JButton btnOut = new JButton("Back");
            styleSoftButton(btnIn, darkBlue);
            styleSoftButton(btnOut, Color.GRAY);

            btnIn.addActionListener(ev -> {
                String inputPass = new String(passF.getPassword());
                if (inputPass.equals("admin123")) {
                    currentRole = "LIBRARIAN";
                    loginD.dispose();
                    d.dispose(); // Close role selection
                    createGUI(); // Open Admin Dashboard
                } else {
                    showGreenCard("Error", "Incorrect Admin Password", MessageType.ERROR);
                }
            });

            btnOut.addActionListener(ev -> loginD.dispose());

            footerP.add(btnIn); footerP.add(btnOut);
            loginD.add(headerP, BorderLayout.NORTH);
            loginD.add(bodyP, BorderLayout.CENTER);
            loginD.add(footerP, BorderLayout.SOUTH);

            loginD.pack();
            loginD.setLocationRelativeTo(d);
            loginD.setVisible(true);
        });

        btnStd.addActionListener(e -> {
            currentRole = "STUDENT";
            d.dispose();
            createGUI();
        });

        body.add(btnLib); body.add(btnStd);
        d.add(header, BorderLayout.NORTH);
        d.add(body, BorderLayout.CENTER);
        d.pack(); d.setLocationRelativeTo(null); d.setVisible(true);
    }


    //MAIN DASHBOARD
    private void createGUI() {
        frame = new JFrame("Library System - Role: " + currentRole);
        frame.setSize(1100, 750);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.WHITE);

        // --- TOP AREA: HEADER & LOGOUT ---
        JPanel topArea = new JPanel();
        topArea.setLayout(new BoxLayout(topArea, BoxLayout.Y_AXIS));
        topArea.setBackground(Color.WHITE);

        JLabel lblHeader = new JLabel("Welcome to Library Management System", JLabel.CENTER);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblHeader.setForeground(darkBlue);
        lblHeader.setBorder(new EmptyBorder(20, 0, 10, 0));
        lblHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        topArea.add(lblHeader);

        //Logout
        JPanel navRow = new JPanel(new BorderLayout());
        navRow.setBackground(Color.WHITE);
        navRow.setBorder(new EmptyBorder(0, 20, 10, 20));

        JButton btnExt = new JButton("Logout");
        styleSoftButton(btnExt, new Color(231, 76, 60)); // Red button
        btnExt.addActionListener(e -> {
            if (showGreenConfirm("System Message", "Logout and return to Login page?")) {
                frame.dispose();
                openLoginDialog();
            }
        });

        navRow.add(btnExt, BorderLayout.EAST);
        topArea.add(navRow);
        frame.add(topArea, BorderLayout.NORTH);

        // --- CENTER AREA: DATA TABLE ---
        model = new DefaultTableModel(system.getAllBooks(), new String[]{"ID", "Title", "Author", "Year", "Shelf"}) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnUndo = new JButton("Undo Action");
        JButton btnSrt = new JButton("Sort Books");
        styleSoftButton(btnUndo, darkBlue);
        styleSoftButton(btnSrt, darkBlue);

        btnUndo.addActionListener(e -> openUndoDialog()); // Uses LIFO stack
        btnSrt.addActionListener(e -> openSortDialog());

        if (currentRole.equals("LIBRARIAN")) {
            // Librarian Bottom Alignment
            JButton btnAdd = new JButton("Add Book");
            JButton btnEdit = new JButton("Edit Book");
            JButton btnDel = new JButton("Delete Book");
            JButton btnSrc = new JButton("Search ID");
            JButton btnHis = new JButton("Issue History");
            JButton btnRep = new JButton("System Report");

            JButton[] libActions = {btnAdd, btnEdit, btnDel, btnSrc, btnUndo, btnSrt, btnHis, btnRep};
            for (JButton b : libActions) {
                styleSoftButton(b, darkBlue);
                bottomPanel.add(b);
            }

            btnAdd.addActionListener(e -> openAddDialog());
            btnEdit.addActionListener(e -> openEditDialog());
            btnDel.addActionListener(e -> openDeleteDialog());
            btnSrc.addActionListener(e -> openSearchDialog());
            btnHis.addActionListener(e -> openHistoryDialog());
            btnRep.addActionListener(e -> openReportDialog());
        } else {

            JButton btnIss = new JButton("Issue Book");
            JButton btnRet = new JButton("Return Book");
            styleSoftButton(btnIss, darkBlue);
            styleSoftButton(btnRet, darkBlue);

            bottomPanel.add(btnUndo);
            bottomPanel.add(btnSrt);
            bottomPanel.add(btnIss);
            bottomPanel.add(btnRet);

            btnIss.addActionListener(e -> openIssueDialog());
            btnRet.addActionListener(e -> openReturnDialog());
        }

        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    //REPORT
    private void openReportDialog() {
        Object[] stats = system.getReportData();
        JDialog d = new JDialog(frame, "Library Report", true);
        d.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkBlue);
        header.setPreferredSize(new Dimension(600, 60));
        JLabel lblTitle = new JLabel("System Statistics Dashboard", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle);

        JPanel body = new JPanel(new GridLayout(1, 3, 15, 0));
        body.setBorder(new EmptyBorder(30, 30, 30, 30));
        body.add(createStatCard("TOTAL", stats[0].toString(), new Color(52, 152, 219)));
        body.add(createStatCard("ISSUED", stats[1].toString(), new Color(231, 76, 60)));
        body.add(createStatCard("SHELF", stats[2].toString(), new Color(46, 204, 113)));

        JPanel footer = new JPanel(new GridLayout(1, 2));
        JButton btnPdf = new JButton("Save as PDF");
        JButton btnClose = new JButton("Close");
        styleSoftButton(btnPdf, new Color(192, 57, 43));
        styleSoftButton(btnClose, darkBlue);

        btnPdf.addActionListener(e -> saveAsPDF(stats));
        btnClose.addActionListener(e -> d.dispose());

        footer.add(btnPdf); footer.add(btnClose);
        d.add(header, BorderLayout.NORTH);
        d.add(body, BorderLayout.CENTER);
        d.add(footer, BorderLayout.SOUTH);

        d.pack(); d.setLocationRelativeTo(frame); d.setVisible(true);
    }

    //REPORT IN PDF
    private void saveAsPDF(Object[] stats) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Library Summary Report");

        job.setPrintable((g, pf, pi) -> {
            if (pi > 0) return Printable.NO_SUCH_PAGE;

            Graphics2D g2 = (Graphics2D) g;
            g2.translate(pf.getImageableX(), pf.getImageableY());

            Color brandBlue = new Color(41, 128, 185);
            g2.setColor(brandBlue);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
            g2.drawString("LIBRARY SUMMARY REPORT", 50, 60);

            g2.setStroke(new BasicStroke(2));
            g2.drawLine(50, 75, 500, 75);

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            g2.drawString("Generated on: " + new java.util.Date(), 50, 95);

            int startY = 130;
            int rowHeight = 30;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            //Background for table header
            g2.setColor(new Color(240, 240, 240));
            g2.fillRect(50, startY, 450, rowHeight);

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString("Category", 60, startY + 20);
            g2.drawString("Count", 400, startY + 20);

            //Table Rows
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            String[][] data = {
                    {"Total Inventory", stats[0].toString()},
                    {"Currently Issued", stats[1].toString()},
                    {"Available on Shelf", stats[2].toString()}
            };

            for (int i = 0; i < data.length; i++) {
                int yPos = startY + (i + 1) * rowHeight;
                g2.drawString(data[i][0], 60, yPos + 20);
                g2.drawString(data[i][1], 400, yPos + 20);
                g2.setColor(new Color(220, 220, 220));
                g2.drawLine(50, yPos + rowHeight, 500, yPos + rowHeight);
                g2.setColor(Color.BLACK);
            }

            //FOOTER
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.drawString("End of Administrative Report", 50, startY + 180);

            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
                showGreenCard("PDF Export", "Report successfully formatted and saved.", MessageType.SUCCESS);
            } catch (PrinterException ex) {
                ex.printStackTrace();
                showGreenCard("Error", "Failed to print report.", MessageType.ERROR);
            }
        }
    }

    //undo option
    private void openUndoDialog() {
        // 1. Peek at the top of the stack to show the user what will be undone
        String lastActionDesc = system.getLastActionDescription();

        if (lastActionDesc.equals("No recent actions")) {
            showGreenCard("Undo", "There are no recent actions to revert.", MessageType.INFO);
            return;
        }

        JDialog d = new JDialog(frame, "Undo Last Action", true);
        d.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(41, 128, 185));
        header.setPreferredSize(new Dimension(450, 55));
        JLabel lblTitle = new JLabel("  Revert Changes", JLabel.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.CENTER);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(new Color(245, 245, 245));
        body.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 5, 5, 5);

        JLabel lblMsg = new JLabel("Action to be Reverted:");
        lblMsg.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel lblActionText = new JLabel("<html><i>" + lastActionDesc + "</i></html>");
        lblActionText.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblActionText.setForeground(new Color(192, 57, 43)); // Red for visibility

        g.gridx = 0; g.gridy = 0; body.add(lblMsg, g);
        g.gridx = 0; g.gridy = 1; body.add(lblActionText, g);
        g.gridx = 0; g.gridy = 2; body.add(new JLabel("Do you want to undo this action?"), g);


        JPanel footer = new JPanel(new GridLayout(1, 2));
        JButton btnConfirm = new JButton("Undo Action");
        JButton btnCancel = new JButton("Cancel");

        styleSoftButton(btnConfirm, new Color(41, 128, 185));
        styleSoftButton(btnCancel, Color.GRAY);

        btnConfirm.addActionListener(e -> {
            //Pops from the stack and reverts the database entry
            if (system.undoLastAction()) {
                showGreenCard("Success", "Successfully undone: " + lastActionDesc, MessageType.SUCCESS);
                refresh(); // Refresh the main dashboard table
                d.dispose();
            } else {
                showGreenCard("Error", "Could not complete the undo operation.", MessageType.ERROR);
            }
        });

        btnCancel.addActionListener(e -> d.dispose());

        footer.add(btnConfirm);
        footer.add(btnCancel);

        d.add(header, BorderLayout.NORTH);
        d.add(body, BorderLayout.CENTER);
        d.add(footer, BorderLayout.SOUTH);

        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }

    //HELPER UI METHODS
    private void refresh() {
        model.setDataVector(system.getAllBooks(), new String[]{"ID", "Title", "Author", "Year", "Shelf"});
    }

    private void styleSoftButton(JButton b, Color bg) {
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false); b.setOpaque(true); b.setBorderPainted(false);
        b.setBorder(new RoundedBorder(10, new Color(0,0,0,30)));
    }

    private JPanel createStatCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, accent),
                new EmptyBorder(10, 15, 10, 15)));
        JLabel l1 = new JLabel(title); l1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel l2 = new JLabel(value); l2.setFont(new Font("Segoe UI", Font.BOLD, 28));
        card.add(l1); card.add(l2);
        return card;
    }

    private void showGreenCard(String title, String message, MessageType type) {
        JDialog d = new JDialog(frame, title, true);
        d.setLayout(new BorderLayout());
        JPanel h = new JPanel(new FlowLayout(FlowLayout.LEFT));
        h.setBackground(darkBlue);
        JLabel l = new JLabel("  " + title); l.setForeground(Color.WHITE); l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        h.add(l); d.add(h, BorderLayout.NORTH);
        d.add(new JLabel("<html><div style='padding:20px;'>" + message + "</div></html>"), BorderLayout.CENTER);
        JButton ok = new JButton("OK"); ok.setBackground(darkBlue); ok.setForeground(Color.WHITE);
        ok.addActionListener(e -> d.dispose());
        d.add(ok, BorderLayout.SOUTH);
        d.pack(); d.setLocationRelativeTo(frame); d.setVisible(true);
    }

    // Inside LibraryGUI.java
    private boolean showGreenConfirm(String title, String message) {
        // Increased size to 500x250 for better readability
        JDialog d = new JDialog(frame, title, true);
        d.setLayout(new BorderLayout());

        // --- LARGE BLUE HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkBlue);
        header.setPreferredSize(new Dimension(400, 50));

        JLabel lblTitle = new JLabel("  " + title, JLabel.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.CENTER);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(new Color(245, 245, 245));
        body.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel lblMsg = new JLabel("<html><div style='text-align: center; width:350px;'>" + message + "</div></html>");
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 18)); // Increased font size
        body.add(lblMsg);

        // --- FOOTER BUTTONS ---
        JPanel footer = new JPanel(new GridLayout(1, 2));
        JButton btnYes = new JButton("Confirm");
        JButton btnNo = new JButton("Cancel");

        // Re-applying your horizontal styling
        styleSoftButton(btnYes, darkBlue);
        styleSoftButton(btnNo, Color.GRAY);
        btnYes.setPreferredSize(new Dimension(200, 50)); // Large buttons
        btnNo.setPreferredSize(new Dimension(200, 50));

        final boolean[] result = {false};
        btnYes.addActionListener(e -> { result[0] = true; d.dispose(); });
        btnNo.addActionListener(e -> { result[0] = false; d.dispose(); });

        footer.add(btnYes); footer.add(btnNo);
        d.add(header, BorderLayout.NORTH);
        d.add(body, BorderLayout.CENTER);
        d.add(footer, BorderLayout.SOUTH);

        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
        return result[0];
    }

    private void openAddDialog() {
        JDialog d = new JDialog(frame, "Add New Book", true);
        d.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkBlue);
        header.setPreferredSize(new Dimension(500, 60));
        JLabel lblTitle = new JLabel("Add New Book", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.CENTER);
        d.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(new Color(245, 245, 245));
        body.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 15);
        String[] labels = {"Book ID:", "Title:", "Author:", "Year:", "Shelf:"};
        JTextField[] fields = new JTextField[5];

        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0.3;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(labelFont);
            body.add(lbl, g);

            g.gridx = 1; g.weightx = 0.7;
            fields[i] = new JTextField();
            fields[i].setPreferredSize(new Dimension(250, 28));
            body.add(fields[i], g);
        }
        d.add(body, BorderLayout.CENTER);

        JButton btnSave = new JButton("Save");
        btnSave.setPreferredSize(new Dimension(500, 50));
        btnSave.setBackground(darkBlue);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.setOpaque(true);

        btnSave.addActionListener(e -> {
            try {
                int id = Integer.parseInt(fields[0].getText().trim());
                String title = fields[1].getText().trim();
                String author = fields[2].getText().trim();
                int year = Integer.parseInt(fields[3].getText().trim());
                String shelf = fields[4].getText().trim();

                if (system.addBook(id, title, author, year, shelf)) {
                    showGreenCard("Success", "Book added successfully!", MessageType.SUCCESS);
                    refresh();
                    d.dispose();
                } else {
                    showGreenCard("Error", "Duplicate ID or Database error.", MessageType.ERROR);
                }
            } catch (Exception ex) {
                showGreenCard("Error", "Please enter valid data.", MessageType.ERROR);
            }
        });

        d.add(btnSave, BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }

    private void openReturnDialog() {
        // Step 1: Input ID using a styled horizontal search bar
        JDialog d = new JDialog(frame, "Return Book", true);
        d.setLayout(new BorderLayout());

        // --- BLUE HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(41, 128, 185)); // darkBlue
        header.setPreferredSize(new Dimension(450, 55));
        JLabel lblTitle = new JLabel("Book Return Processing", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.CENTER);

        // --- BODY ---
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(new Color(245, 245, 245));
        body.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblId = new JLabel("Enter Book ID:");
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JTextField fSearchId = new JTextField();
        fSearchId.setPreferredSize(new Dimension(200, 30));

        g.gridx = 0; g.gridy = 0; body.add(lblId, g);
        g.gridx = 1; g.gridy = 0; g.weightx = 1.0; body.add(fSearchId, g);

        // --- ACTION BUTTON ---
        JButton btnProcess = new JButton("Verify & Return");
        styleSoftButton(btnProcess, new Color(41, 128, 185));

        btnProcess.addActionListener(e -> {
            String idStr = fSearchId.getText().trim();
            if (idStr.isEmpty()) {
                showGreenCard("Error", "Please enter a Book ID.", MessageType.ERROR);
                return;
            }

            if (showGreenConfirm("Confirm Return", "Are you sure you want to return Book ID: " + idStr + "?")) {
                try {
                    int id = Integer.parseInt(idStr);
                    String returnDate = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());

                    int fine = system.returnBook(id, returnDate);

                    if (fine >= 0) {
                        showGreenCard("Success", "Book returned successfully. \nCalculated Fine: Rs " + fine, MessageType.SUCCESS);
                        refresh();
                        d.dispose();
                    } else {
                        showGreenCard("Error", "This book was never issued or is already returned.", MessageType.ERROR);
                    }
                } catch (Exception ex) {
                    showGreenCard("Error", "Please enter a valid numeric ID.", MessageType.ERROR);
                }
            }
        });

        d.add(header, BorderLayout.NORTH);
        d.add(body, BorderLayout.CENTER);
        d.add(btnProcess, BorderLayout.SOUTH);

        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }

    private void openIssueDialog() {
        JDialog d = new JDialog(frame, "Issue Book", true);
        d.setLayout(new BorderLayout());

        // --- INTERACTIVE BLUE HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(41, 128, 185)); // darkBlue
        header.setPreferredSize(new Dimension(500, 60));

        JLabel lblTitle = new JLabel("Issue Book to Student", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.CENTER);
        d.add(header, BorderLayout.NORTH);

        // --- FORM BODY (Horizontal GridBagLayout) ---
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(new Color(245, 245, 245));
        body.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 15);
        Dimension fieldSize = new Dimension(250, 30);

        // Input Fields
        JTextField fId = new JTextField();
        fId.setPreferredSize(fieldSize);

        JTextField fStudent = new JTextField();
        fStudent.setPreferredSize(fieldSize);

        // Date Spinner for Issue Date
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateSpinner = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setPreferredSize(fieldSize);

        // Adding Rows Horizontally
        addFormRow(body, "Book ID:", fId, g, labelFont, 0);
        addFormRow(body, "Student Name:", fStudent, g, labelFont, 1);

        // Adding Date Row
        g.gridx = 0; g.gridy = 2; g.weightx = 0.3;
        JLabel lblDate = new JLabel("Issue Date:");
        lblDate.setFont(labelFont);
        body.add(lblDate, g);

        g.gridx = 1; g.weightx = 0.7;
        body.add(dateSpinner, g);

        d.add(body, BorderLayout.CENTER);

        // --- BLUE ISSUE BUTTON (Footer) ---
        JButton btnIssue = new JButton("Issue Book");
        btnIssue.setPreferredSize(new Dimension(500, 55));
        btnIssue.setBackground(new Color(41, 128, 185));
        btnIssue.setForeground(Color.WHITE);
        btnIssue.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnIssue.setFocusPainted(false);
        btnIssue.setBorderPainted(false);
        btnIssue.setOpaque(true);

        btnIssue.addActionListener(e -> {
            String bookIdText = fId.getText().trim();
            String studentName = fStudent.getText().trim();

            // Check if all fields are filled
            if (bookIdText.isEmpty() || studentName.isEmpty()) {
                showGreenCard("Validation Error", "Please fill in all fields (Book ID and Student Name).", MessageType.ERROR);
                return;
            }

            try {
                int id = Integer.parseInt(bookIdText);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String dateStr = sdf.format(dateSpinner.getValue());

                // Attempt to issue the book
                boolean success = system.issueBook(id, studentName, dateStr);

                if (success) {
                    showGreenCard("Success", "Book issued successfully to " + studentName, MessageType.SUCCESS);
                    refresh();
                    d.dispose();
                } else {
                    // This now covers cases where the book is already issued
                    showGreenCard("Error", "Book is already issued and not yet returned, or does not exist.", MessageType.ERROR);
                }
            } catch (NumberFormatException ex) {
                showGreenCard("Error", "Please enter a valid numeric Book ID.", MessageType.ERROR);
            } catch (Exception ex) {
                showGreenCard("Error", "An unexpected error occurred.", MessageType.ERROR);
            }
        });

        d.add(btnIssue, BorderLayout.SOUTH);

        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }

    // Helper to keep GridBagLayout consistent
    private void addFormRow(JPanel p, String label, JTextField tf, GridBagConstraints g, Font font, int row) {
        g.gridx = 0; g.gridy = row; g.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(font);
        p.add(lbl, g);

        g.gridx = 1; g.weightx = 0.7;
        p.add(tf, g);
    }

    private void openHistoryDialog() {
        // Fetch all records (Current + Previous)
        String[][] data = system.getAllIssueHistory();

        JDialog d = new JDialog(frame, "Full Issue History", true);
        d.setLayout(new BorderLayout());

        // --- BLUE HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(41, 128, 185));
        header.setPreferredSize(new Dimension(700, 55));
        JLabel lblTitle = new JLabel("Complete Issue & Return History", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.CENTER);

        // --- TABLE ---
        String[] columnNames = {"Book ID", "Title", "Member", "Issue Date", "Return Date / Status"};
        DefaultTableModel historyModel = new DefaultTableModel(data, columnNames) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable historyTable = new JTable(historyModel);
        historyTable.setRowHeight(25);

        // Optional: Auto-scroll to the top
        JScrollPane scrollPane = new JScrollPane(historyTable);
        d.add(scrollPane, BorderLayout.CENTER);

        // --- FOOTER CLOSE BUTTON ---
        JButton btnClose = new JButton("Close History");
        styleSoftButton(btnClose, new Color(41, 128, 185));
        btnClose.addActionListener(e -> d.dispose());

        d.add(header, BorderLayout.NORTH);
        d.add(btnClose, BorderLayout.SOUTH);

        d.setSize(800, 500);
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }

    private void openSearchDialog() {
        // Custom Horizontal Dialog for Search
        JDialog d = new JDialog(frame, "Search Book", true);
        d.setLayout(new BorderLayout());

        // --- BLUE HEADER (Matches your branding) ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(41, 128, 185)); // Using your darkBlue color
        header.setPreferredSize(new Dimension(450, 55));

        JLabel lblTitle = new JLabel("Search Book by ID", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.CENTER);

        // --- FORM BODY ---
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(new Color(245, 245, 245));
        body.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblId = new JLabel("Enter Book ID:");
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JTextField fSearchId = new JTextField();
        fSearchId.setPreferredSize(new Dimension(200, 30));

        g.gridx = 0; g.gridy = 0;
        body.add(lblId, g);

        g.gridx = 1; g.gridy = 0; g.weightx = 1.0;
        body.add(fSearchId, g);

        // --- INTERACTIVE FOOTER BUTTON ---
        JButton btnFind = new JButton("Search Now");
        styleSoftButton(btnFind, new Color(41, 128, 185)); // Using your styling helper
        btnFind.setPreferredSize(new Dimension(450, 45));

        btnFind.addActionListener(e -> {
            String idStr = fSearchId.getText().trim();
            if (idStr.isEmpty()) return;

            try {
                int id = Integer.parseInt(idStr);
                LibrarySystem.Book b = system.findBook(id); // Calling logic from LibrarySystem

                if (b != null) {
                    d.dispose(); // Close search prompt
                    showBookDetailsCard(b); // Display the horizontal detail result
                } else {
                    showGreenCard("Not Found", "No book found with ID: " + id, MessageType.ERROR);
                }
            } catch (NumberFormatException ex) {
                showGreenCard("Error", "Please enter a valid numeric ID.", MessageType.ERROR);
            }
        });

        d.add(header, BorderLayout.NORTH);
        d.add(body, BorderLayout.CENTER);
        d.add(btnFind, BorderLayout.SOUTH);

        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }

    private void showBookDetailsCard(LibrarySystem.Book b) {
        JDialog d = new JDialog(frame, "Book Information", true);
        d.setLayout(new BorderLayout());

        // Blue Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkBlue);
        header.setPreferredSize(new Dimension(500, 60));
        JLabel lblTitle = new JLabel("Book Details", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.CENTER);

        // Horizontal Body
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"Book ID:", "Title:", "Author:", "Year:", "Shelf Location:"};
        String[] values = {String.valueOf(b.id), b.title, b.author, String.valueOf(b.year), b.shelf};

        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0.3;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
            body.add(lbl, g);

            g.gridx = 1; g.weightx = 0.7;
            JLabel val = new JLabel(values[i]);
            val.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            body.add(val, g);
        }

        JButton btnClose = new JButton("Close");
        styleSoftButton(btnClose, darkBlue);
        btnClose.addActionListener(e -> d.dispose());

        d.add(header, BorderLayout.NORTH);
        d.add(body, BorderLayout.CENTER);
        d.add(btnClose, BorderLayout.SOUTH);

        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }

    //sorting books
    private void openSortDialog() {
        JDialog d = new JDialog(frame, "Sort Books", true);
        d.setLayout(new BorderLayout());

        // --- BLUE HEADER (Matches your design) ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(41, 128, 185));
        header.setPreferredSize(new Dimension(450, 55));
        JLabel lblTitle = new JLabel("Sort Available Books", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.CENTER);

        // --- BODY ---
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(new Color(245, 245, 245));
        body.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblSort = new JLabel("Sort by:");
        lblSort.setFont(new Font("Segoe UI", Font.BOLD, 15));

        String[] options = {"Book ID", "Title", "Year"};
        JComboBox<String> combo = new JComboBox<>(options);
        combo.setPreferredSize(new Dimension(200, 30));

        g.gridx = 0; g.gridy = 0; body.add(lblSort, g);
        g.gridx = 1; g.gridy = 0; g.weightx = 1.0; body.add(combo, g);

        // --- ACTION BUTTON (Matches 'Save' button style) ---
        JButton btnApply = new JButton("Apply Sort");
        styleSoftButton(btnApply, new Color(41, 128, 185));
        btnApply.setPreferredSize(new Dimension(450, 45));

        btnApply.addActionListener(e -> {
            String criteria = (String) combo.getSelectedItem();
            // Fetch sorted data and update the main table
            model.setDataVector(system.getSortedBooks(criteria),
                    new String[]{"Book ID", "Title", "Author", "Year", "Shelf"});
            d.dispose();
            showGreenCard("Sorted", "Books sorted by " + criteria, MessageType.SUCCESS);
        });

        d.add(header, BorderLayout.NORTH);
        d.add(body, BorderLayout.CENTER);
        d.add(btnApply, BorderLayout.SOUTH);

        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }

    private void openEditDialog() {
        JDialog d = new JDialog(frame, "Edit Book", true);
        d.setLayout(new BorderLayout());

        // Blue Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkBlue);
        header.setPreferredSize(new Dimension(450, 55));
        JLabel lblTitle = new JLabel("Search Book by ID", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.CENTER);

        // Body
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(new Color(245, 245, 245));
        body.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblId = new JLabel("Enter Book ID:");
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JTextField fSearchId = new JTextField();
        fSearchId.setPreferredSize(new Dimension(200, 30));

        g.gridx = 0; g.gridy = 0; body.add(lblId, g);
        g.gridx = 1; g.gridy = 0; g.weightx = 1.0; body.add(fSearchId, g);

        // Find Button
        JButton btnFind = new JButton("Find Book");
        styleSoftButton(btnFind, darkBlue);
        btnFind.addActionListener(e -> {
            try {
                int id = Integer.parseInt(fSearchId.getText().trim());
                LibrarySystem.Book b = system.findBook(id);
                if (b == null) {
                    showGreenCard("Error", "No book found with ID: " + id, MessageType.ERROR);
                } else {
                    d.dispose(); // Close search dialog
                    showHorizontalEditForm(b); // Open edit dialog
                }
            } catch (Exception ex) {
                showGreenCard("Error", "Please enter a valid numeric ID.", MessageType.ERROR);
            }
        });

        d.add(header, BorderLayout.NORTH);
        d.add(body, BorderLayout.CENTER);
        d.add(btnFind, BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }

    private void showHorizontalEditForm(LibrarySystem.Book b) {
        JDialog d = new JDialog(frame, "Update Book Details", true);
        d.setLayout(new BorderLayout());

        // Blue Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkBlue);
        header.setPreferredSize(new Dimension(500, 60));
        JLabel lblTitle = new JLabel("Update Book Information", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.CENTER);

        // Form Body
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(new Color(245, 245, 245));
        body.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField fT = new JTextField(b.title);
        JTextField fA = new JTextField(b.author);
        JTextField fY = new JTextField(String.valueOf(b.year));
        JTextField fS = new JTextField(b.shelf);

        String[] labels = {"Title:", "Author:", "Year:", "Shelf:"};
        JTextField[] fields = {fT, fA, fY, fS};

        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
            body.add(lbl, g);

            g.gridx = 1; g.gridy = i; g.weightx = 1.0;
            fields[i].setPreferredSize(new Dimension(250, 28));
            body.add(fields[i], g);
        }

        // Footer Buttons
        JPanel footer = new JPanel(new GridLayout(1, 2));
        JButton btnUpdate = new JButton("Update");
        JButton btnCancel = new JButton("Cancel");
        styleSoftButton(btnUpdate, darkBlue);
        styleSoftButton(btnCancel, Color.GRAY);

        btnUpdate.addActionListener(e -> {
            if (showGreenConfirm("Confirm Update", "Save changes to Book ID " + b.id + "?")) {
                try {
                    boolean ok = system.updateBook(b.id, fT.getText().trim(), fA.getText().trim(),
                            Integer.parseInt(fY.getText().trim()), fS.getText().trim());
                    if (ok) {
                        showGreenCard("Success", "Updated successfully!", MessageType.SUCCESS);
                        refresh(); d.dispose();
                    }
                } catch (Exception ex) {
                    showGreenCard("Error", "Invalid data format.", MessageType.ERROR);
                }
            }
        });
        btnCancel.addActionListener(e -> d.dispose());

        footer.add(btnUpdate);
        footer.add(btnCancel);
        d.add(header, BorderLayout.NORTH);
        d.add(body, BorderLayout.CENTER);
        d.add(footer, BorderLayout.SOUTH);

        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }

//    // Layout helper for horizontal fields
//    private void addFormRow(JPanel p, String label, JTextField tf, GridBagConstraints g, Font font, int row) {
//        g.gridx = 0; g.gridy = row; g.weightx = 0.3;
//        JLabel lbl = new JLabel(label);
//        lbl.setFont(font);
//        p.add(lbl, g);
//
//        g.gridx = 1; g.weightx = 0.7;
//        tf.setPreferredSize(new Dimension(250, 28));
//        p.add(tf, g);
//    }

    private void openDeleteDialog() {
        // Create custom horizontal search dialog
        JDialog d = new JDialog(frame, "Delete Book", true);
        d.setLayout(new BorderLayout());

        // Blue Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkBlue);
        header.setPreferredSize(new Dimension(450, 55));
        JLabel lblTitle = new JLabel("Delete Book by ID", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.CENTER);

        // Body
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(new Color(245, 245, 245));
        body.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblId = new JLabel("Enter Book ID:");
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JTextField fSearchId = new JTextField();
        fSearchId.setPreferredSize(new Dimension(200, 30));

        g.gridx = 0; g.gridy = 0; body.add(lblId, g);
        g.gridx = 1; g.gridy = 0; g.weightx = 1.0; body.add(fSearchId, g);

        // Footer Button (Delete Action)
        JButton btnFind = new JButton("Find to Delete");
        styleSoftButton(btnFind, darkBlue); // Red color for delete action

        btnFind.addActionListener(e -> {
            try {
                int id = Integer.parseInt(fSearchId.getText().trim());
                LibrarySystem.Book b = system.findBook(id);
                if (b == null) {
                    showGreenCard("Error", "No book found with ID: " + id, MessageType.ERROR);
                } else {
                    d.dispose(); // Close search dialog
                    showDeleteConfirmationForm(b); // Move to Step 2
                }
            } catch (Exception ex) {
                showGreenCard("Error", "Please enter a valid numeric ID.", MessageType.ERROR);
            }
        });

        d.add(header, BorderLayout.NORTH);
        d.add(body, BorderLayout.CENTER);
        d.add(btnFind, BorderLayout.SOUTH);

        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }

    private void showDeleteConfirmationForm(LibrarySystem.Book b) {
        JDialog d = new JDialog(frame, "Confirm Deletion", true);
        d.setLayout(new BorderLayout());

        // Red Header to indicate danger/deletion
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkBlue);
        header.setPreferredSize(new Dimension(500, 60));
        JLabel lblTitle = new JLabel("Are you sure you want to delete?", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.CENTER);

        // Info Body (Horizontal Layout)
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(new Color(245, 245, 245));
        body.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        // Display non-editable labels for verification
        String[] labels = {"Book ID:", "Title:", "Author:", "Location:"};
        String[] values = {String.valueOf(b.id), b.title, b.author, b.shelf};

        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0.3;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
            body.add(lbl, g);

            g.gridx = 1; g.weightx = 0.7;
            JLabel val = new JLabel(values[i]);
            val.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            body.add(val, g);
        }

        // Footer Buttons
        JPanel footer = new JPanel(new GridLayout(1, 2));
        JButton btnConfirm = new JButton("Delete Permanently");
        JButton btnCancel = new JButton("Cancel");

        styleSoftButton(btnConfirm, new Color(231, 76, 60)); // Red
        styleSoftButton(btnCancel, Color.GRAY);

        btnConfirm.addActionListener(e -> {
            if (system.deleteBook(b.id)) {
                showGreenCard("Deleted", "Book removed from database.", MessageType.SUCCESS);
                refresh();
                d.dispose();
            } else {
                showGreenCard("Error", "Delete failed.", MessageType.ERROR);
            }
        });

        btnCancel.addActionListener(e -> d.dispose());

        footer.add(btnConfirm);
        footer.add(btnCancel);

        d.add(header, BorderLayout.NORTH);
        d.add(body, BorderLayout.CENTER);
        d.add(footer, BorderLayout.SOUTH);

        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }


    private static class RoundedBorder implements Border {
        private int r; private Color c;
        public RoundedBorder(int r, Color c) {
            this.r = r; this.c = c;
        }
        public Insets getBorderInsets(Component comp) {
            return new Insets(r, r, r, r);

        }
        public boolean isBorderOpaque() {
            return false;
        }
        public void paintBorder(Component comp, Graphics g, int x, int y, int w, int h) {
            g.setColor(c); g.drawRoundRect(x, y, w-1, h-1, r, r);
        }
    }
}