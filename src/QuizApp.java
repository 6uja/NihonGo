import javafx.application.Application;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class QuizApp extends Application {

  private Stage stage;

  private UserManager userManager = new UserManager();
  private RankingManager rankingManager = new RankingManager();
  private QuestionManager questionManager = new QuestionManager();
  private StatsManager statsManager = new StatsManager();

  private String currentUser;
  private String selectedCategory;

  private ArrayList<Question> questions = new ArrayList<>();
  private ArrayList<Question> filteredQuestions = new ArrayList<>();
  private ArrayList<Question> wrongQuestions = new ArrayList<>();

  private Timeline timeline;
  private int timeLeft;

  private int currentIndex = 0;
  private int score = 0;
  private int correctCount = 0;

  private final String[] CATEGORIES = {
          "초급 단어",
          "초급 문법",
          "중급 단어",
          "중급 문법",
          "고급 단어",
          "고급 문법"
  };

  @Override
  public void start(Stage primaryStage) {
    stage = primaryStage;
    stage.setTitle("QuizHub");

    questions = questionManager.loadQuestions();

    showLoginScreen();
    stage.show();
  }

  private void showLoginScreen() {
    VBox root = createRoot();

    Label title = createTitle("QuizHub");
    Label subtitle = createSubTitle("일본어 학습 퀴즈 플랫폼");

    TextField idField = createTextField("ID");
    PasswordField pwField = createPasswordField("Password");

    Button loginButton = new Button("로그인");
    Button registerButton = new Button("회원가입");

    styleButton(loginButton);
    styleButton(registerButton);

    loginButton.setOnAction(e -> {
      String id = idField.getText();
      String pw = pwField.getText();

      if (id.equals("admin") && pw.equals("1234")) {
        showAdminMenu();
        return;
      }

      if (userManager.login(id, pw)) {
        currentUser = id;
        showMenu();
      } else {
        showAlert("로그인 실패");
      }
    });

    registerButton.setOnAction(e -> {
      String id = idField.getText();
      String pw = pwField.getText();

      if (id.isEmpty() || pw.isEmpty()) {
        showAlert("ID와 비밀번호를 입력해주세요.");
        return;
      }

      if (userManager.register(id, pw)) {
        showAlert("회원가입 성공");
      } else {
        showAlert("이미 존재하는 ID");
      }
    });

    root.getChildren().addAll(title, subtitle, idField, pwField, loginButton, registerButton);
    stage.setScene(new Scene(root, 620, 460));
  }

  private void showMenu() {
    VBox root = createRoot();

    Label label = createTitle(currentUser + "님 환영합니다!");
    Label subtitle = createSubTitle("일본어 학습을 시작해보세요.");

    Button startButton = new Button("퀴즈 시작");
    Button rankingButton = new Button("랭킹 보기");
    Button statsButton = new Button("학습 통계 보기");
    Button logoutButton = new Button("로그아웃");

    styleButton(startButton);
    styleButton(rankingButton);
    styleButton(statsButton);
    styleBackButton(logoutButton);

    startButton.setOnAction(e -> showCategoryMenu());
    rankingButton.setOnAction(e -> showRankingDashboard());
    statsButton.setOnAction(e -> showStatsGraph());
    logoutButton.setOnAction(e -> {
      currentUser = null;
      showLoginScreen();
    });

    root.getChildren().addAll(label, subtitle, startButton, rankingButton, statsButton, logoutButton);
    stage.setScene(new Scene(root, 650, 520));
  }

  private void showCategoryMenu() {
    VBox root = createRoot();

    Label title = createTitle("카테고리 선택");
    Label subtitle = createSubTitle("난이도와 학습 유형을 선택하세요.");

    GridPane categoryGrid = new GridPane();
    categoryGrid.setHgap(16);
    categoryGrid.setVgap(16);
    categoryGrid.setAlignment(Pos.CENTER);

    Button basicWordButton = new Button("초급 단어");
    Button basicGrammarButton = new Button("초급 문법");
    Button middleWordButton = new Button("중급 단어");
    Button middleGrammarButton = new Button("중급 문법");
    Button advancedWordButton = new Button("고급 단어");
    Button advancedGrammarButton = new Button("고급 문법");

    styleButton(basicWordButton);
    styleButton(basicGrammarButton);
    styleButton(middleWordButton);
    styleButton(middleGrammarButton);
    styleButton(advancedWordButton);
    styleButton(advancedGrammarButton);

    basicWordButton.setOnAction(e -> startCategoryQuiz("초급 단어"));
    basicGrammarButton.setOnAction(e -> startCategoryQuiz("초급 문법"));
    middleWordButton.setOnAction(e -> startCategoryQuiz("중급 단어"));
    middleGrammarButton.setOnAction(e -> startCategoryQuiz("중급 문법"));
    advancedWordButton.setOnAction(e -> startCategoryQuiz("고급 단어"));
    advancedGrammarButton.setOnAction(e -> startCategoryQuiz("고급 문법"));

    categoryGrid.add(basicWordButton, 0, 0);
    categoryGrid.add(basicGrammarButton, 1, 0);
    categoryGrid.add(middleWordButton, 0, 1);
    categoryGrid.add(middleGrammarButton, 1, 1);
    categoryGrid.add(advancedWordButton, 0, 2);
    categoryGrid.add(advancedGrammarButton, 1, 2);

    Button backButton = new Button("메인으로");
    styleBackButton(backButton);
    backButton.setOnAction(e -> showMenu());

    root.getChildren().addAll(title, subtitle, categoryGrid, backButton);
    stage.setScene(new Scene(root, 720, 560));
  }

  private void startCategoryQuiz(String category) {
    selectedCategory = category;

    filteredQuestions.clear();
    wrongQuestions.clear();

    ArrayList<Question> categoryQuestions = new ArrayList<>();

    for (Question q : questions) {
      if (q.getCategory().equals(category)) {
        categoryQuestions.add(q);
      }
    }

    if (categoryQuestions.isEmpty()) {
      showAlert("해당 카테고리에 문제가 없습니다.");
      showCategoryMenu();
      return;
    }

    Collections.shuffle(categoryQuestions);

    int limit = Math.min(10, categoryQuestions.size());

    for (int i = 0; i < limit; i++) {
      filteredQuestions.add(categoryQuestions.get(i));
    }

    currentIndex = 0;
    score = 0;
    correctCount = 0;

    showQuestion();
  }

  private void showQuestion() {
    VBox root = createRoot();

    Question q = filteredQuestions.get(currentIndex);

    Label categoryLabel = createSubTitle("카테고리: " + selectedCategory);
    Label progressLabel = createSubTitle("문제 " + (currentIndex + 1) + " / " + filteredQuestions.size());

    Label timerLabel = new Label("남은 시간: 20초");
    timerLabel.setFont(Font.font("Arial", 18));
    timerLabel.setTextFill(Color.web("#4F46E5"));

    Label questionLabel = new Label(q.getQuestion());
    questionLabel.setFont(Font.font("Arial", 28));
    questionLabel.setWrapText(true);
    questionLabel.setMaxWidth(660);
    questionLabel.setAlignment(Pos.CENTER);
    questionLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
    questionLabel.setStyle(
            "-fx-padding: 12;" +
                    "-fx-text-fill: #1E293B;"
    );

    VBox optionBox = new VBox(14);
    optionBox.setAlignment(Pos.CENTER);

    String[] options = q.getOptions();

    for (int i = 0; i < options.length; i++) {
      int selected = i;

      Button button = new Button((i + 1) + ". " + options[i]);
      styleOptionButton(button);

      button.setOnAction(e -> {
        if (timeline != null) {
          timeline.stop();
        }

        if (q.isCorrect(selected)) {
          score += 100 / filteredQuestions.size();
          correctCount++;
          showAlert("정답!");
        } else {
          wrongQuestions.add(q);
          showAlert("오답!");
        }

        goNextQuestion();
      });

      optionBox.getChildren().add(button);
    }

    root.getChildren().addAll(categoryLabel, progressLabel, timerLabel, questionLabel, optionBox);
    stage.setScene(new Scene(root, 760, 580));

    startTimer(timerLabel, q);
  }

  private void startTimer(Label timerLabel, Question q) {
    timeLeft = 20;

    timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
              timeLeft--;
              timerLabel.setText("남은 시간: " + timeLeft + "초");

              if (timeLeft <= 0) {
                timeline.stop();
                wrongQuestions.add(q);
                showAlert("시간 초과!");
                goNextQuestion();
              }
            })
    );

    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();
  }

  private void goNextQuestion() {
    currentIndex++;

    if (currentIndex < filteredQuestions.size()) {
      showQuestion();
    } else {
      showResult();
    }
  }

  private void showResult() {
    if (timeline != null) {
      timeline.stop();
    }

    rankingManager.saveScore(currentUser, score, selectedCategory);
    statsManager.saveResult(currentUser, selectedCategory, correctCount, filteredQuestions.size());

    VBox root = createRoot();

    Label result = createTitle("최종 점수: " + score + "점");
    Label correctLabel = createSubTitle("정답 수: " + correctCount + " / " + filteredQuestions.size());

    Button wrongButton = new Button("오답노트 보기");
    Button rankingButton = new Button("랭킹 보기");
    Button statsButton = new Button("학습 통계 보기");
    Button homeButton = new Button("메인으로");

    styleButton(wrongButton);
    styleButton(rankingButton);
    styleButton(statsButton);
    styleBackButton(homeButton);

    wrongButton.setOnAction(e -> showWrongNotes());
    rankingButton.setOnAction(e -> showRankingDashboard());
    statsButton.setOnAction(e -> showStatsGraph());
    homeButton.setOnAction(e -> showMenu());

    root.getChildren().addAll(result, correctLabel, wrongButton, rankingButton, statsButton, homeButton);
    stage.setScene(new Scene(root, 650, 540));
  }

  private void showWrongNotes() {
    if (wrongQuestions.isEmpty()) {
      showAlert("틀린 문제가 없습니다.");
      return;
    }

    StringBuilder sb = new StringBuilder();

    for (Question q : wrongQuestions) {
      sb.append("문제: ").append(q.getQuestion()).append("\n");
      sb.append("정답: ").append(q.getOptions()[q.getAnswerIndex()]).append("\n\n");
    }

    showTextDialog("오답노트", sb.toString());
  }

  private void showStatsGraph() {
    Map<String, int[]> statsMap = statsManager.getUserStatsMap(currentUser);

    if (statsMap.isEmpty()) {
      showAlert("아직 학습 통계가 없습니다.");
      return;
    }

    VBox root = createRoot();

    Label title = createTitle("학습 통계 그래프");
    Label subtitle = createSubTitle("카테고리별 정답률을 시각화했습니다.");

    VBox graphBox = new VBox(16);
    graphBox.setAlignment(Pos.CENTER);

    for (String category : statsMap.keySet()) {
      int correct = statsMap.get(category)[0];
      int total = statsMap.get(category)[1];
      int rate = (int) ((double) correct / total * 100);

      Label categoryLabel = new Label(category + "  " + correct + "/" + total + "  (" + rate + "%)");
      categoryLabel.setFont(Font.font("Arial", 15));

      Rectangle bgBar = new Rectangle(340, 22);
      bgBar.setArcWidth(18);
      bgBar.setArcHeight(18);
      bgBar.setFill(Color.web("#E5E7EB"));

      Rectangle valueBar = new Rectangle(340 * rate / 100.0, 22);
      valueBar.setArcWidth(18);
      valueBar.setArcHeight(18);
      valueBar.setFill(Color.web("#7C83FD"));

      StackPane bar = new StackPane(bgBar, valueBar);
      bar.setAlignment(Pos.CENTER_LEFT);

      VBox itemBox = new VBox(6);
      itemBox.setAlignment(Pos.CENTER_LEFT);
      itemBox.setMaxWidth(380);
      itemBox.setStyle(
              "-fx-background-color: white;" +
                      "-fx-background-radius: 16;" +
                      "-fx-padding: 14;" +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 10, 0, 0, 4);"
      );

      itemBox.getChildren().addAll(categoryLabel, bar);
      graphBox.getChildren().add(itemBox);
    }

    Button backButton = new Button("메인으로");
    styleBackButton(backButton);
    backButton.setOnAction(e -> showMenu());

    root.getChildren().addAll(title, subtitle, graphBox, backButton);
    stage.setScene(new Scene(root, 700, 560));
  }

  private void showRankingDashboard() {
    VBox root = createRoot();

    Label title = createTitle("카테고리별 랭킹");
    Label subtitle = createSubTitle("카테고리를 선택하면 점수 순위를 그래프로 확인할 수 있습니다.");

    ComboBox<String> categoryBox = new ComboBox<>();
    categoryBox.getItems().addAll(CATEGORIES);
    categoryBox.setValue("초급 단어");
    categoryBox.setMaxWidth(260);

    VBox rankingBox = new VBox(14);
    rankingBox.setAlignment(Pos.CENTER);

    Button backButton = new Button("메인으로");
    styleBackButton(backButton);
    backButton.setOnAction(e -> showMenu());

    categoryBox.setOnAction(e -> updateRankingGraph(categoryBox.getValue(), rankingBox));

    updateRankingGraph("초급 단어", rankingBox);

    root.getChildren().addAll(title, subtitle, categoryBox, rankingBox, backButton);
    stage.setScene(new Scene(root, 760, 640));
  }

  private void updateRankingGraph(String category, VBox rankingBox) {
    rankingBox.getChildren().clear();

    Map<String, Integer> rankingMap = rankingManager.getCategoryRankingMap(category);

    if (rankingMap.isEmpty()) {
      Label emptyLabel = createSubTitle("아직 랭킹 기록이 없습니다.");
      rankingBox.getChildren().add(emptyLabel);
      return;
    }

    Label subTitle = new Label("🏆 " + category + " Ranking");
    subTitle.setFont(Font.font("Arial", 22));

    rankingBox.getChildren().add(subTitle);

    final int[] rank = {1};

    rankingMap.entrySet()
            .stream()
            .sorted((a, b) -> b.getValue() - a.getValue())
            .forEach(entry -> {

              HBox row = new HBox(12);
              row.setAlignment(Pos.CENTER_LEFT);
              row.setMaxWidth(560);
              row.setStyle(
                      "-fx-background-color: white;" +
                              "-fx-background-radius: 18;" +
                              "-fx-padding: 13;" +
                              "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 10, 0, 0, 4);"
              );

              Label rankLabel = new Label(rank[0] + "위");
              rankLabel.setFont(Font.font("Arial", 16));
              rankLabel.setPrefWidth(55);

              Label nameLabel = new Label(entry.getKey());
              nameLabel.setFont(Font.font("Arial", 16));
              nameLabel.setPrefWidth(110);

              Rectangle bgBar = new Rectangle(240, 18);
              bgBar.setArcWidth(18);
              bgBar.setArcHeight(18);
              bgBar.setFill(Color.web("#E5E7EB"));

              Rectangle valueBar = new Rectangle(240 * entry.getValue() / 100.0, 18);
              valueBar.setArcWidth(18);
              valueBar.setArcHeight(18);
              valueBar.setFill(Color.web("#7C83FD"));

              StackPane bar = new StackPane(bgBar, valueBar);
              bar.setAlignment(Pos.CENTER_LEFT);

              Label scoreLabel = new Label(entry.getValue() + "점");
              scoreLabel.setFont(Font.font("Arial", 16));
              scoreLabel.setPrefWidth(70);

              row.getChildren().addAll(rankLabel, nameLabel, bar, scoreLabel);
              rankingBox.getChildren().add(row);

              rank[0]++;
            });
  }

  private void showAdminMenu() {
    VBox root = createRoot();

    Label title = createTitle("관리자 모드");
    Label subtitle = createSubTitle("문제를 추가하거나 검색할 수 있습니다.");

    Button addButton = new Button("문제 추가");
    Button searchButton = new Button("문제 검색 / 필터");
    Button logoutButton = new Button("로그아웃");

    styleButton(addButton);
    styleButton(searchButton);
    styleBackButton(logoutButton);

    addButton.setOnAction(e -> showAddQuestionScreen());
    searchButton.setOnAction(e -> showQuestionSearchScreen());
    logoutButton.setOnAction(e -> showLoginScreen());

    root.getChildren().addAll(title, subtitle, addButton, searchButton, logoutButton);
    stage.setScene(new Scene(root, 650, 460));
  }

  private void showAddQuestionScreen() {
    VBox root = createRoot();

    Label title = createTitle("문제 추가");
    Label subtitle = createSubTitle("관리자는 새로운 문제를 저장할 수 있습니다.");

    ComboBox<String> categoryBox = new ComboBox<>();
    categoryBox.getItems().addAll(CATEGORIES);
    categoryBox.setValue("초급 단어");
    categoryBox.setMaxWidth(300);

    TextField questionField = createTextField("문제 입력");
    TextField option1 = createTextField("보기 1");
    TextField option2 = createTextField("보기 2");
    TextField option3 = createTextField("보기 3");
    TextField option4 = createTextField("보기 4");

    ComboBox<Integer> answerBox = new ComboBox<>();
    answerBox.getItems().addAll(0, 1, 2, 3);
    answerBox.setValue(0);
    answerBox.setMaxWidth(300);

    Label guide = createSubTitle("정답 번호: 0=보기1, 1=보기2, 2=보기3, 3=보기4");

    Button saveButton = new Button("저장");
    Button backButton = new Button("뒤로");

    styleButton(saveButton);
    styleBackButton(backButton);

    saveButton.setOnAction(e -> {
      if (questionField.getText().isEmpty()
              || option1.getText().isEmpty()
              || option2.getText().isEmpty()
              || option3.getText().isEmpty()
              || option4.getText().isEmpty()) {
        showAlert("모든 내용을 입력해주세요.");
        return;
      }

      String[] options = {
              option1.getText(),
              option2.getText(),
              option3.getText(),
              option4.getText()
      };

      Question q = new Question(
              questionField.getText(),
              options,
              answerBox.getValue(),
              categoryBox.getValue()
      );

      questionManager.addQuestion(q);
      questions = questionManager.loadQuestions();

      showAlert("문제가 추가되었습니다.");
      showAdminMenu();
    });

    backButton.setOnAction(e -> showAdminMenu());

    root.getChildren().addAll(
            title, subtitle, categoryBox, questionField,
            option1, option2, option3, option4,
            guide, answerBox, saveButton, backButton
    );

    stage.setScene(new Scene(root, 760, 720));
  }

  private void showQuestionSearchScreen() {
    VBox root = createRoot();

    Label title = createTitle("문제 검색 / 필터");
    Label subtitle = createSubTitle("카테고리와 검색어로 문제를 확인할 수 있습니다.");

    ComboBox<String> categoryBox = new ComboBox<>();
    categoryBox.getItems().add("전체");
    categoryBox.getItems().addAll(CATEGORIES);
    categoryBox.setValue("전체");
    categoryBox.setMaxWidth(300);

    TextField keywordField = createTextField("검색어 입력");

    TextArea resultArea = new TextArea();
    resultArea.setEditable(false);
    resultArea.setPrefSize(540, 300);
    resultArea.setStyle(
            "-fx-background-radius: 16;" +
                    "-fx-border-radius: 16;" +
                    "-fx-font-size: 14px;"
    );

    Button searchButton = new Button("검색");
    Button backButton = new Button("뒤로");

    styleButton(searchButton);
    styleBackButton(backButton);

    searchButton.setOnAction(e -> {
      String category = categoryBox.getValue();
      String keyword = keywordField.getText();

      StringBuilder sb = new StringBuilder();

      for (Question q : questions) {
        boolean categoryMatch = category.equals("전체") || q.getCategory().equals(category);
        boolean keywordMatch = keyword.isEmpty() || q.getQuestion().contains(keyword);

        if (categoryMatch && keywordMatch) {
          sb.append("[")
                  .append(q.getCategory())
                  .append("] ")
                  .append(q.getQuestion())
                  .append("\n정답: ")
                  .append(q.getOptions()[q.getAnswerIndex()])
                  .append("\n\n");
        }
      }

      if (sb.length() == 0) {
        sb.append("검색 결과가 없습니다.");
      }

      resultArea.setText(sb.toString());
    });

    backButton.setOnAction(e -> showAdminMenu());

    root.getChildren().addAll(
            title, subtitle, categoryBox, keywordField,
            searchButton, resultArea, backButton
    );

    stage.setScene(new Scene(root, 760, 660));
  }

  private VBox createRoot() {
    VBox root = new VBox(18);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(35));
    root.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #EEF2FF, #FDF2F8);" +
                    "-fx-font-family: 'Malgun Gothic';"
    );
    return root;
  }

  private Label createTitle(String text) {
    Label label = new Label(text);
    label.setFont(Font.font("Arial", 30));
    label.setTextFill(Color.web("#1E293B"));
    label.setAlignment(Pos.CENTER);
    return label;
  }

  private Label createSubTitle(String text) {
    Label label = new Label(text);
    label.setFont(Font.font("Arial", 15));
    label.setTextFill(Color.web("#64748B"));
    label.setAlignment(Pos.CENTER);
    return label;
  }

  private TextField createTextField(String prompt) {
    TextField field = new TextField();
    field.setPromptText(prompt);
    field.setMaxWidth(240);
    field.setPrefHeight(42);
    field.setStyle(
            "-fx-background-color: white;" +
                    "-fx-background-radius: 16;" +
                    "-fx-border-radius: 16;" +
                    "-fx-border-color: #C7D2FE;" +
                    "-fx-border-width: 1.5;" +
                    "-fx-padding: 12;" +
                    "-fx-font-size: 14px;"
    );
    return field;
  }

  private PasswordField createPasswordField(String prompt) {
    PasswordField field = new PasswordField();
    field.setPromptText(prompt);
    field.setMaxWidth(240);
    field.setPrefHeight(42);
    field.setStyle(
            "-fx-background-color: white;" +
                    "-fx-background-radius: 16;" +
                    "-fx-border-radius: 16;" +
                    "-fx-border-color: #C7D2FE;" +
                    "-fx-border-width: 1.5;" +
                    "-fx-padding: 12;" +
                    "-fx-font-size: 14px;"
    );
    return field;
  }

  private void styleButton(Button button) {
    button.setStyle(
            "-fx-background-color: #7C83FD;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 15px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 18;" +
                    "-fx-padding: 12 22;" +
                    "-fx-cursor: hand;"
    );

    button.setPrefWidth(240);
  }

  private void styleOptionButton(Button button) {
    button.setStyle(
            "-fx-background-color: white;" +
                    "-fx-text-fill: #1E293B;" +
                    "-fx-font-size: 15px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 18;" +
                    "-fx-border-radius: 18;" +
                    "-fx-border-color: #CBD5E1;" +
                    "-fx-padding: 12 22;" +
                    "-fx-cursor: hand;"
    );

    button.setPrefWidth(360);
  }

  private void styleBackButton(Button button) {
    button.setStyle(
            "-fx-background-color: #64748B;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 15px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 18;" +
                    "-fx-padding: 12 22;" +
                    "-fx-cursor: hand;"
    );

    button.setPrefWidth(240);
  }

  private void showAlert(String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void showTextDialog(String title, String text) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);

    TextArea area = new TextArea(text);
    area.setEditable(false);
    area.setPrefSize(460, 330);

    alert.getDialogPane().setContent(area);
    alert.showAndWait();
  }
}