import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class RankingManager {

    public void saveScore(String userId, int score, String category) {

        String selectSql = """
                SELECT score
                FROM ranking
                WHERE user_id = ? AND category = ?
                """;

        String insertSql = """
                INSERT INTO ranking (user_id, category, score)
                VALUES (?, ?, ?)
                """;

        String updateSql = """
                UPDATE ranking
                SET score = ?
                WHERE user_id = ? AND category = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

            selectStmt.setString(1, userId);
            selectStmt.setString(2, category);

            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                int oldScore = rs.getInt("score");

                if (score > oldScore) {
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, score);
                        updateStmt.setString(2, userId);
                        updateStmt.setString(3, category);
                        updateStmt.executeUpdate();
                    }
                }

            } else {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, userId);
                    insertStmt.setString(2, category);
                    insertStmt.setInt(3, score);
                    insertStmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            System.out.println("랭킹 저장 오류: " + e.getMessage());
        }
    }

    public Map<String, Integer> getCategoryRankingMap(String category) {

        Map<String, Integer> rankingMap = new HashMap<>();

        String sql = """
                SELECT user_id, score
                FROM ranking
                WHERE category = ?
                ORDER BY score DESC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                rankingMap.put(
                        rs.getString("user_id"),
                        rs.getInt("score")
                );
            }

        } catch (SQLException e) {
            System.out.println("랭킹 불러오기 오류: " + e.getMessage());
        }

        return rankingMap;
    }

    public String getRankingText() {

        String[] categories = {
                "초급 단어",
                "초급 문법",
                "중급 단어",
                "중급 문법",
                "고급 단어",
                "고급 문법"
        };

        StringBuilder sb = new StringBuilder();

        for (String category : categories) {
            sb.append("🏆 ")
                    .append(category)
                    .append(" Ranking\n");

            Map<String, Integer> rankingMap = getCategoryRankingMap(category);

            if (rankingMap.isEmpty()) {
                sb.append("기록 없음\n\n");
                continue;
            }

            rankingMap.entrySet()
                    .stream()
                    .sorted((a, b) -> b.getValue() - a.getValue())
                    .forEach(entry -> {
                        sb.append(entry.getKey())
                                .append(" - ")
                                .append(entry.getValue())
                                .append("점\n");
                    });

            sb.append("\n");
        }

        return sb.toString();
    }
}