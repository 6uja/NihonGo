import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class StatsManager {

    private final String FILE_NAME = "stats.txt";

    public void saveResult(String userId,
                           String category,
                           int correct,
                           int total) {

        try (BufferedWriter bw =
                     new BufferedWriter(
                             new FileWriter(FILE_NAME, true))) {

            bw.write(
                    userId + ","
                            + category + ","
                            + correct + ","
                            + total
            );

            bw.newLine();

        } catch (IOException e) {

            System.out.println("통계 저장 오류");
        }
    }

    public String getUserStats(String userId) {

        Map<String, int[]> statsMap =
                getUserStatsMap(userId);

        if (statsMap.isEmpty()) {
            return "아직 학습 통계가 없습니다.";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("📊 나의 학습 통계\n\n");

        for (String category : statsMap.keySet()) {

            int correct = statsMap.get(category)[0];

            int total = statsMap.get(category)[1];

            int rate =
                    (int)((double)correct / total * 100);

            sb.append(category)
                    .append(" : ")
                    .append(correct)
                    .append("/")
                    .append(total)
                    .append(" 정답, 정답률 ")
                    .append(rate)
                    .append("%\n");
        }

        return sb.toString();
    }

    // ★ 추가된 메서드
    public Map<String, int[]> getUserStatsMap(String userId) {

        Map<String, int[]> statsMap =
                new HashMap<>();

        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return statsMap;
        }

        try (BufferedReader br =
                     new BufferedReader(
                             new FileReader(FILE_NAME))) {

            String line;

            while ((line = br.readLine()) != null) {

                String[] data = line.split(",");

                if (data.length == 4
                        && data[0].equals(userId)) {

                    String category = data[1];

                    int correct =
                            Integer.parseInt(data[2]);

                    int total =
                            Integer.parseInt(data[3]);

                    statsMap.putIfAbsent(
                            category,
                            new int[]{0, 0}
                    );

                    statsMap.get(category)[0]
                            += correct;

                    statsMap.get(category)[1]
                            += total;
                }
            }

        } catch (IOException e) {

            System.out.println("통계 불러오기 오류");
        }

        return statsMap;
    }
}