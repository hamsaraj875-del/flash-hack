
import java.util.*;

public class WordSearch {

    static class TrieNode {
        TrieNode[] children = new TrieNode[26];
        String word;
    }

    static char[][] grid;
    static boolean[][] visited;
    static int rows, cols;
    static TrieNode root = new TrieNode();

    static List<String> path = new ArrayList<>();
    static Map<String, List<String>> result = new LinkedHashMap<>();

    static int[][] directions = {
        {-1, 0}, {1, 0}, {0, -1}, {0, 1}
    };

    static void insert(String word) {
        TrieNode node = root;

        for (char ch : word.toCharArray()) {
            int index = ch - 'a';

            if (node.children[index] == null) {
                node.children[index] = new TrieNode();
            }

            node = node.children[index];
        }

        node.word = word;
    }

    static void dfs(int r, int c, TrieNode node) {

        char ch = grid[r][c];
        TrieNode next = node.children[ch - 'a'];

        if (next == null || visited[r][c]) {
            return;
        }

        visited[r][c] = true;
        path.add("(" + (r + 1) + "," + (c + 1) + ")");

        if (next.word != null) {
            result.put(next.word, new ArrayList<>(path));
            next.word = null;
        }

        for (int[] dir : directions) {
            int nr = r + dir[0];
            int nc = c + dir[1];

            if (nr >= 0 && nr < rows &&
                nc >= 0 && nc < cols &&
                !visited[nr][nc]) {

                dfs(nr, nc, next);
            }
        }

        path.remove(path.size() - 1);
        visited[r][c] = false;
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter rows and columns: ");
        rows = sc.nextInt();
        cols = sc.nextInt();

        if (rows < 1 || rows > 10 || cols < 1 || cols > 10) {
            System.out.println("Grid size must be between 1 and 10.");
            return;
        }

        grid = new char[rows][cols];
        visited = new boolean[rows][cols];

        System.out.println("Enter grid rows without spaces:");

        for (int i = 0; i < rows; i++) {
            String line = sc.next().toLowerCase();

            if (line.length() != cols || !line.matches("[a-z]+")) {
                System.out.println("Invalid grid row.");
                return;
            }

            grid[i] = line.toCharArray();
        }

        System.out.print("Enter dictionary size: ");
        int n = sc.nextInt();

        if (n < 0 || n > 10000) {
            System.out.println("Dictionary size must be between 0 and 10000.");
            return;
        }

        System.out.println("Enter dictionary words:");

        for (int i = 0; i < n; i++) {
            String word = sc.next().toLowerCase();

            if (word.matches("[a-z]{1,10}")) {
                insert(word);
            }
        }

        long start = System.nanoTime();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                dfs(i, j, root);
            }
        }

        long end = System.nanoTime();

        System.out.println("\n========== SEARCH RESULTS ==========");

        if (result.isEmpty()) {
            System.out.println("No dictionary words found.");
        } else {
            for (Map.Entry<String, List<String>> entry : result.entrySet()) {
                System.out.println("\nWord: " + entry.getKey());
                System.out.println("Path: " + String.join(" -> ", entry.getValue()));
            }
        }

        System.out.println("\nTotal words found: " + result.size());
        System.out.printf("Execution time: %.3f ms%n",
                (end - start) / 1_000_000.0);

        sc.close();
    }
}
