import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserManager {

    private final String FILE_NAME = "users.txt";

    private Map<String, String> users =
            new HashMap<>();

    public UserManager() {

        loadUsers();
    }

    // 회원가입
    public boolean register(String id, String pw) {

        // 이미 존재하는 ID면 실패
        if (users.containsKey(id)) {

            return false;
        }

        users.put(id, pw);

        saveUser(id, pw);

        return true;
    }

    // 로그인
    public boolean login(String id, String pw) {

        return users.containsKey(id)
                && users.get(id).equals(pw);
    }

    // 파일 저장
    private void saveUser(String id, String pw) {

        try (BufferedWriter bw =
                     new BufferedWriter(
                             new FileWriter(FILE_NAME, true))) {

            bw.write(id + "," + pw);

            bw.newLine();

        } catch (IOException e) {

            System.out.println("회원 저장 오류");
        }
    }

    // 파일 불러오기
    private void loadUsers() {

        File file = new File(FILE_NAME);

        if (!file.exists()) {

            return;
        }

        try (BufferedReader br =
                     new BufferedReader(
                             new FileReader(FILE_NAME))) {

            String line;

            while ((line = br.readLine()) != null) {

                String[] data = line.split(",");

                if (data.length == 2) {

                    users.put(data[0], data[1]);
                }
            }

        } catch (IOException e) {

            System.out.println("회원 불러오기 오류");
        }
    }
}