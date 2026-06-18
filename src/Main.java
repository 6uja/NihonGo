import javafx.application.Application;
import java.io.File;

public class Main {

    public static void main(String[] args) {

        DatabaseManager.initializeDatabase();

        QuestionManager questionManager = new QuestionManager();

        File csvFile = new File("japanese_quiz_questions.csv");

        if (csvFile.exists()) {
            questionManager.importQuestionsFromCSV("japanese_quiz_questions.csv");
        } else {
            System.out.println("CSV 파일이 없습니다.");
        }

        Application.launch(QuizApp.class, args);
    }
}