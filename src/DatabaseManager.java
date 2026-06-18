import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:quizhub.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        createUsersTable();
        createQuestionsTable();
        createRankingTable();
        createStatsTable();
    }

    private static void createUsersTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id TEXT PRIMARY KEY,
                    password TEXT NOT NULL
                );
                """;

        execute(sql);
    }

    private static void createQuestionsTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS questions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    category TEXT NOT NULL,
                    question TEXT NOT NULL,
                    option1 TEXT NOT NULL,
                    option2 TEXT NOT NULL,
                    option3 TEXT NOT NULL,
                    option4 TEXT NOT NULL,
                    answer_index INTEGER NOT NULL
                );
                """;

        execute(sql);
    }

    private static void createRankingTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS ranking (
                    user_id TEXT NOT NULL,
                    category TEXT NOT NULL,
                    score INTEGER NOT NULL,
                    PRIMARY KEY (user_id, category)
                );
                """;

        execute(sql);
    }

    private static void createStatsTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS stats (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id TEXT NOT NULL,
                    category TEXT NOT NULL,
                    correct_count INTEGER NOT NULL,
                    total_count INTEGER NOT NULL
                );
                """;

        execute(sql);
    }

    private static void execute(String sql) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

        } catch (SQLException e) {
            System.out.println("DB 실행 오류: " + e.getMessage());
        }
    }
}