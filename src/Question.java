public class Question {
    private String question;
    private String[] options;
    private int answerIndex;
    private String category;

    public Question(String question, String[] options, int answerIndex, String category) {
        this.question = question;
        this.options = options;
        this.answerIndex = answerIndex;
        this.category = category;
    }

    public String getQuestion() {
        return question;
    }

    public String[] getOptions() {
        return options;
    }

    public boolean isCorrect(int index) {
        return index == answerIndex;
    }

    public int getAnswerIndex() {
        return answerIndex;
    }

    public String getCategory() {
        return category;
    }

    public String toFileString() {
        return category + "|" + question + "|" +
                options[0] + "|" + options[1] + "|" + options[2] + "|" + options[3] + "|" +
                answerIndex;
    }

    public static Question fromFileString(String line) {
        String[] data = line.split("\\|");

        if (data.length != 7) {
            return null;
        }

        String category = data[0];
        String question = data[1];
        String[] options = {data[2], data[3], data[4], data[5]};
        int answerIndex = Integer.parseInt(data[6]);

        return new Question(question, options, answerIndex, category);
    }
}