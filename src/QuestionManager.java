import java.sql.*;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.FileReader;

public class QuestionManager {

    public ArrayList<Question> loadQuestions() {

        ArrayList<Question> questions = new ArrayList<>();

        String sql = "SELECT * FROM questions";

        try (
                Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()
        ) {

            while (rs.next()) {

                String category = rs.getString("category");
                String questionText = rs.getString("question");

                String[] options = {
                        rs.getString("option1"),
                        rs.getString("option2"),
                        rs.getString("option3"),
                        rs.getString("option4")
                };

                int answerIndex = rs.getInt("answer_index");

                questions.add(new Question(
                        questionText,
                        options,
                        answerIndex,
                        category
                ));
            }

        } catch (SQLException e) {
            System.out.println("문제 불러오기 오류: " + e.getMessage());
        }

        return questions;
    }

    public void addQuestion(Question q) {

        String sql = """
                INSERT INTO questions
                (category, question, option1, option2, option3, option4, answer_index)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setString(1, q.getCategory());
            pstmt.setString(2, q.getQuestion());
            pstmt.setString(3, q.getOptions()[0]);
            pstmt.setString(4, q.getOptions()[1]);
            pstmt.setString(5, q.getOptions()[2]);
            pstmt.setString(6, q.getOptions()[3]);
            pstmt.setInt(7, q.getAnswerIndex());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("문제 저장 오류: " + e.getMessage());
        }
    }

    public void importQuestionsFromCSV(String filePath) {

        if (hasQuestions()) {
            System.out.println("이미 DB에 문제가 있어서 CSV import를 건너뜁니다.");
            return;
        }

        String sql = """
                INSERT INTO questions
                (category, question, option1, option2, option3, option4, answer_index)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                BufferedReader br = new BufferedReader(new FileReader(filePath));
                Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            String header = br.readLine();

            if (header == null) {
                System.out.println("CSV 파일이 비어 있습니다.");
                return;
            }

            String line;
            int count = 0;

            while ((line = br.readLine()) != null) {

                String[] data = line.split(",", -1);

                if (data.length != 7) {
                    System.out.println("CSV 형식 오류: " + line);
                    continue;
                }

                int answerIndex;

                try {
                    answerIndex = Integer.parseInt(data[6].trim());
                } catch (NumberFormatException e) {
                    System.out.println("정답 번호 오류: " + line);
                    continue;
                }

                if (answerIndex < 0 || answerIndex > 3) {
                    System.out.println("정답 번호 범위 오류: " + line);
                    continue;
                }

                pstmt.setString(1, data[0].trim());
                pstmt.setString(2, data[1].trim());
                pstmt.setString(3, data[2].trim());
                pstmt.setString(4, data[3].trim());
                pstmt.setString(5, data[4].trim());
                pstmt.setString(6, data[5].trim());
                pstmt.setInt(7, answerIndex);

                pstmt.executeUpdate();
                count++;
            }

            System.out.println("CSV 문제 추가 완료: " + count + "개");

        } catch (Exception e) {
            System.out.println("CSV Import 오류: " + e.getMessage());
        }
    }

    private boolean hasQuestions() {

        String sql = "SELECT COUNT(*) FROM questions";

        try (
                Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()
        ) {

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.out.println("문제 개수 확인 오류: " + e.getMessage());
        }

        return false;
    }
}