import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class GitTester {
    public static void main(String[] args) throws IOException {
        File git = new File("git");
        File objects = new File("git/objects");
        File index = new File("git/index");
        File test = new File("testerFile.txt");
        String indexLine = (Git.sha1Generator(test) + " " + test.getName());
        String i = "git/index";

        // testing initialization and repeated delete of repoMethod : passed
        Git.initGitRepoTesterMethod();
        Git.initializeGitRepoMethod();
        if (git.exists()) {
            System.out.println("git directory creation passed");
        } else {
            System.out.println("git directory creation failed");
        }
        if (objects.exists()) {
            System.out.println("objects directory creation passed");
        } else {
            System.out.println("objects directory creation failed");
        }
        if (index.exists()) {
            System.out.println("index file creation passed");
        } else {
            System.out.println("index file creation failed");
        }

        // sha1 hash tester to see if correct : passed
        if (Git.sha1Generator(test).equals("53fc2666fa98bf55fd92a312f544826645983428")) {
            System.out.println("Sha-1 hash creation passed");
        } else {
            System.out.println("Sha-1 hash creation failed");
            // System.out.println(Git.sha1Generator(test));
        }

        // blob tester to see if writes correct thing to index : passed
        Git.blobGenerator(test);
        boolean isInsideIndex = false;
        BufferedReader readIndex = new BufferedReader(new FileReader(i));
        while (readIndex.ready()) {
            if (readIndex.readLine().equals(indexLine)) {
                isInsideIndex = true;
            }
        }
        readIndex.close();
        if (isInsideIndex) {
            System.out.println("Write to index passed");
        } else {
            System.out.println("Write to index failed");
        }

        // testing contents and location of copied hash name file : passed
        BufferedReader readHashFile = new BufferedReader(new FileReader("git/objects/" + Git.sha1Generator(test)));
        BufferedReader readTestFile = new BufferedReader(new FileReader(test));
        while (readHashFile.ready()) {
            System.out.println(readHashFile.readLine());
        }
    }
}
