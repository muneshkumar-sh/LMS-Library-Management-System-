import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Stack;

public class LibrarySystem {
    private static final String DB_URL="jdbc:mysql://localhost:3306/libraryDatabase";
    private static final String DB_USER="root";
    private static final String DB_PASS="lifeisgood";

    private Stack<UndoAction> undoStack=new Stack<>();

    private class UndoAction {
        String type; Object data; String description;
        public UndoAction(String type, Object data, String description) {
            this.type = type; this.data = data; this.description=description;
        }
    }

    public static class Book {
        public int id; String title, author, shelf; int year;
        public Book(int id, String title, String author, int year, String shelf) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.year = year;
            this.shelf = shelf;
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    //DATA RETRIEVAL
    public Object[][] getAllBooks() {
        ArrayList<Object[]> list=new ArrayList<>();
        try (Connection con=getConnection();
             Statement st=con.createStatement();
             ResultSet rs=st.executeQuery("SELECT * FROM books")) {
            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("id"), rs.getString("title"),
                        rs.getString("author"), rs.getInt("year"),
                        rs.getString("shelf")});
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return list.toArray(new Object[0][]);
    }

    public String[][] getAllIssueHistory() {
        ArrayList<String[]> list=new ArrayList<>();

        //Select all issues and use a CASE statement to determine the status
        String query="SELECT i.book_id, b.title, m.name, i.issue_date, " +
                "IFNULL(i.return_date, 'Currently Issued') as status " +
                "FROM issues i " +
                "JOIN books b ON i.book_id = b.id " +
                "JOIN members m ON i.member_id = m.member_id " +
                "ORDER BY i.issue_date DESC";

        try (Connection con=getConnection();
             Statement st=con.createStatement();
             ResultSet rs=st.executeQuery(query)) {
            while (rs.next()) {
                list.add(new String[]{
                        rs.getString(1), //Book ID
                        rs.getString(2), //Title
                        rs.getString(3), //Member Name
                        rs.getString(4), //Issue Date
                        rs.getString(5)  //Status (Return Date or 'Currently Issued')
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list.toArray(new String[0][]);
    }

    public Book findBook(int id) {
        try (Connection con = getConnection();
             PreparedStatement ps=con.prepareStatement("SELECT * FROM books WHERE id = ?")) {
            ps.setInt(1, id); ResultSet rs=ps.executeQuery();
            if (rs.next())
                return new Book(rs.getInt("id"),
                    rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("year"),
                        rs.getString("shelf"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //OPERATIONS
    public boolean addBook(int id, String title, String author, int year, String shelf) {
        try (Connection con=getConnection();
             PreparedStatement ps=con.prepareStatement("INSERT INTO books VALUES (?,?,?,?,?)")) {
                ps.setInt(1, id);
                ps.setString(2, title);
                ps.setString(3, author);
                ps.setInt(4, year);
                ps.setString(5, shelf);
            if (ps.executeUpdate() > 0) {
                undoStack.push(new UndoAction("ADD", id, "Added Book ID: " + id));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateBook(int id, String title, String author, int year, String shelf) {
        Book old=findBook(id);
        try (Connection con=getConnection();
             PreparedStatement ps=con.prepareStatement("UPDATE books SET title=?, author=?, year=?, shelf=? WHERE id=?")) {
                ps.setString(1, title);
                ps.setString(2, author);
                ps.setInt(3, year);
                ps.setString(4, shelf);
                ps.setInt(5, id);
            if (ps.executeUpdate() > 0) {
                undoStack.push(new UndoAction("EDIT", old, "Edited Book: " + old.title));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteBook(int id) {
        Book b = findBook(id);
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM books WHERE id=?")) {
                ps.setInt(1, id);
            if (ps.executeUpdate() > 0) {
                undoStack.push(new UndoAction("DELETE", b, "Deleted Book: " + b.title));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean issueBook(int bookId, String name, String date) {
        try (Connection con = getConnection()) {
            PreparedStatement ck = con.prepareStatement("SELECT COUNT(*) FROM issues WHERE book_id=? AND return_date IS NULL");
            ck.setInt(1, bookId);
                ResultSet rs = ck.executeQuery();
            if (rs.next() && rs.getInt(1) > 0)
                return false;

            int mid = getOrCreateMember(con, name);
            PreparedStatement ps = con.prepareStatement("INSERT INTO issues(book_id, member_id, issue_date) VALUES (?,?,?)");
                ps.setInt(1, bookId);
                ps.setInt(2, mid);
                ps.setString(3, date);
            if (ps.executeUpdate() > 0) {
                undoStack.push(new UndoAction("ISSUE", bookId, "Issued Book ID: " + bookId));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int returnBook(int bookId, String date) {
        try (Connection con = getConnection()) {
            PreparedStatement ps=con.prepareStatement("SELECT issue_id, issue_date FROM issues WHERE book_id=? AND return_date IS NULL");
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next())
                return -1;

            int iid = rs.getInt("issue_id");
            int fine = calculateFine(rs.getString("issue_date"), date);
            PreparedStatement up = con.prepareStatement("UPDATE issues SET return_date=? WHERE issue_id=?");
            up.setString(1, date);
            up.setInt(2, iid);

            if (up.executeUpdate() > 0) {
                undoStack.push(new UndoAction("RETURN", iid, "Returned Book ID: " + bookId));
                return fine;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }


    public boolean undoLastAction() {
        if (undoStack.isEmpty())
            return false;

        UndoAction last=undoStack.pop();
        try (Connection con = getConnection()) {
            switch (last.type) {
                case "ADD":
                    con.prepareStatement("DELETE FROM books WHERE id=" + last.data).executeUpdate();
                    break;

                case "DELETE":
                    Book b=(Book) last.data;
                    PreparedStatement in = con.prepareStatement("INSERT INTO books VALUES (?,?,?,?,?)");
                    in.setInt(1, b.id);
                    in.setString(2, b.title);
                    in.setString(3, b.author);
                    in.setInt(4, b.year);
                    in.setString(5, b.shelf);
                    in.executeUpdate();
                    break;
                case "EDIT":
                    Book o=(Book) last.data;
                    PreparedStatement ed=con.prepareStatement("UPDATE books SET title=?, author=?, year=?, shelf=? WHERE id=?");
                    ed.setString(1, o.title);
                    ed.setString(2, o.author);
                    ed.setInt(3, o.year);
                    ed.setString(4, o.shelf);
                    ed.setInt(5, o.id);
                    ed.executeUpdate();
                    break;
                case "ISSUE": con.prepareStatement("DELETE FROM issues WHERE book_id=" + last.data + " AND return_date IS NULL").executeUpdate();
                break;
                case "RETURN": con.prepareStatement("UPDATE issues SET return_date=NULL WHERE issue_id=" + last.data).executeUpdate();
                break;
            }
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //report
    public Object[] getReportData() {
        try (Connection con = getConnection();
             Statement st = con.createStatement()) {
                ResultSet r1 = st.executeQuery("SELECT COUNT(*) FROM books"); r1.next(); int t = r1.getInt(1);
                ResultSet r2 = st.executeQuery("SELECT COUNT(*) FROM issues WHERE return_date IS NULL");
                r2.next();
                int i = r2.getInt(1);
                return new Object[]{t, i, t - i};
        } catch (Exception e) {
            return new Object[]{0, 0, 0};
        }
    }

    public Object[][] getSortedBooks(String criteria) {
        String col = criteria.equalsIgnoreCase("Title") ? "title" : (criteria.equalsIgnoreCase("Year") ? "year" : "id");
        ArrayList<Object[]> list = new ArrayList<>();
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM books ORDER BY " + col)) {
            while (rs.next()) list.add(new Object[]{rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getInt("year"),
                    rs.getString("shelf")});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list.toArray(new Object[0][]);
    }

    public String[][] getIssuedBooksReport() {
        ArrayList<String[]> list = new ArrayList<>();
        try (Connection con = getConnection();
             ResultSet rs = con.createStatement().executeQuery("SELECT i.book_id, b.title, m.name, i.issue_date FROM issues i JOIN books b ON i.book_id=b.id JOIN members m ON i.member_id=m.member_id WHERE i.return_date IS NULL")) {
            while (rs.next()) list.add(new String[]{rs.getString(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4)});
        } catch (Exception e) {}
        return list.toArray(new String[0][]);
    }

    private int getOrCreateMember(Connection con, String name) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT member_id FROM members WHERE name=?");
        ps.setString(1, name);
        ResultSet rs = ps.executeQuery();

        if (rs.next())
            return rs.getInt(1);

        PreparedStatement in = con.prepareStatement("INSERT INTO members(name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        in.setString(1, name);
        in.executeUpdate();
        ResultSet k = in.getGeneratedKeys();
        k.next();
        return k.getInt(1);
    }

    //fine calculation
    private int calculateFine(String start, String end) {
        try {
            long days = ChronoUnit.DAYS.between(LocalDate.parse(start), LocalDate.parse(end));
            return days > 7 ? (int) (days - 7) * 10 : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public void setLastAction(String type, Object data, String description) {
        undoStack.push(new UndoAction(type, data, description));
    }

    public String getLastActionDescription() {
        return (!undoStack.isEmpty()) ? undoStack.peek().description : "No recent actions";
    }
}