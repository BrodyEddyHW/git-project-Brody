import java.io.*;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class Git implements GitInterface{

    public static void main(String[] args) {
        // File git = new File("git");
        // File objects = new File("git/objects");
        // File index = new File("git/index");
    }

    public interface GitInterface {

        /**
         * Stages a file for the next commit.
         *
         * @param filePath The path to the file to be staged.
         */
        public void stage(String filePath) {
            File toBeStaged = new File(filePath);

            if(toBeStaged.exists()) {
                if (toBeStaged.isDirectory()) {
                    addTree(filePath, toBeStaged.getName());
                }
                else {
                    blobGenerator(toBeStaged);
                }
            }
            else {
                throw new FileNotFoundException();
            }

        }
    
        /**
         * Creates a commit with the given author and message.
         * It should capture the current state of the repository,
         * update the HEAD, and return the commit hash.
         *
         * @param author  The name of the author making the commit.
         * @param message The commit message describing the changes.
         * @return The SHA1 hash of the new commit.
         */
        public String commit(String author, String message) {
            newCommit(author, message);

            BufferedReader br = new BufferedReader(new FileReader(new File("git/HEAD")));
            String commitHash = br.readLine();
            br.close();
            return commitHash;
        }
    
        /**
         * EXTRA CREDIT: Checks out a specific commit given its hash.
         * This should update the working directory to match the
         * state of the repository at that commit.
         *
         * @param commitHash The SHA1 hash of the commit to check out.
         */
        void checkout(String commitHash);
    }
    



    public static void initializeGitRepoMethod() throws IOException {
        // creating pointers for files/directorys (does not actually create them)
        File git = new File("git");
        File objects = new File("git/objects");
        File index = new File("git/index");
        File README = new File("git/README.md");
        // checking if they all exists
        if (git.exists() && objects.exists() && index.exists()) {
            System.out.println("Git Repository already exists");

        }
        // creating each one that deosn't exist
        else if (git.exists() == false) {
            git.mkdir();
        }
        if (objects.exists() == false) {
            objects.mkdir();
        }
        if (index.exists() == false) {
            index.createNewFile();
        }
        if(README.exists() == false) {
            README.createNewFile();
        }

        blobGenerator(README);

        newCommit("Example", "Initial commit");
    }


    // checks if files exist and then delete them to continue tester
    public static void initGitRepoTesterMethod() throws IOException {
        File git = new File("git");
        File objects = new File("git/objects");
        File index = new File("git/index");
        File head = new File("git/HEAD");
        if (git.exists() == true) {
            git.delete();
        }
        if (objects.exists() == true) {
            objects.delete();
        }
        if (index.exists() == true) {
            index.delete();
        }
        if(head.exists() == true) {
            head.delete();
        } 
    }

    // sha-1 function from https://www.geeksforgeeks.org/sha-1-hash-in-java/ and
    // then modified for type File
    public static String sha1Generator(File fileInputSha1) throws IOException {
        try {
            // getInstance() method is called with algorithm SHA-1
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte

            byte[] messageDigest = md.digest(Files.readAllBytes(fileInputSha1.toPath()/* not absolute path */))/*
                                                                                                                * fileInputSha1
                                                                                                                * .
                                                                                                                * getBytes
                                                                                                                * ()
                                                                                                                */;

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 40 digits long
            while (hashtext.length() < 40) {
                hashtext = "0" + hashtext;
            }

            // return the HashText (Sha1 file name)
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void blobGenerator(File blobFileInput) throws IOException {
        // string for index path
        String i = "git/index";
        // bool for checking if entry exists in index file
        Boolean existsInIndex = false;
        // creates hash file name
        String shaFileName = sha1Generator(blobFileInput);
        // saves original file name
        //String originalFileName = blobFileInput.getName();
        // creates line to be writen to index file
        String type = "";
        if (blobFileInput.isFile()) {
            type += "blob ";
        }
        else {
            type += "tree ";
        }
        String path = blobFileInput.getPath();
        String indexOutput = (type + shaFileName + " " + path);

        // points and creates new file with hash as name
        File hashFile = new File("git/objects/" + shaFileName);

        // copy file contents to hashNamedFile
        copyContent(blobFileInput, hashFile);
        // checks if entry already exists in index and sets boolean accordingly
        try {
            BufferedReader readIndex = new BufferedReader(new FileReader(i));
            while (readIndex.ready()) {
                if (readIndex.readLine().equals(indexOutput)) {
                    existsInIndex = true;
                }
            }
            readIndex.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // writes to index if entry is not present
        try {
            if (!existsInIndex) {
                BufferedWriter writeIndex = new BufferedWriter(new FileWriter(i, true));
                writeIndex.write(indexOutput);
                writeIndex.newLine();
                writeIndex.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String addTree(String path, String name) throws IOException{
        //creates file object
        File blob = new File(path);
        //checks if path works
        if (!blob.exists()) {
            throw new IOException("this path ain't work. litowaly");
        }
        File tree = new File ("git/objects/" + name);
        //checks to see if it is a file or folder
        if (blob.isDirectory()) {
            // //creates the new tree file
            String toTreeFile = "";
            //goes through everything in the folder
            File[] blobs = blob.listFiles();

            for (int i = 0; i < blobs.length; i++) {
                File file = blobs[i];
                String hash = "";
                if (file.isDirectory()) {
                    //adds new line for the tree file
                    hash = addTree(file.getPath(), file.getName());
                    toTreeFile += "tree " + hash + " " + file.getName() + "\n";
                }
                else {
                    //gets the new line
                    hash = sha1Generator(file);
                    String newLine = "blob " + hash + " " + file.getName();

                    //adds new line to the tree file
                    toTreeFile += newLine + "\n";

                    //saves file stuff to objects & index
                    blobGenerator(file);
                }
            }
            //adds stuff to tree file
            BufferedWriter bw = new BufferedWriter(new FileWriter(tree));
            bw.write(toTreeFile);
            bw.close();

            //adds directory stuff to index
            File index = new File ("git/index");
            String toIndex = "tree " + sha1Generator(tree) + " " + path + "\n";
            BufferedWriter bw2 = new BufferedWriter(new FileWriter(index, true));
            bw2.write(toIndex);
            bw2.close();
        }
        return sha1Generator(tree);
    }

    // reader and writer to copy files from
    // https://www.geeksforgeeks.org/different-ways-to-copy-content-from-one-file-to-another-file-in-java/
    // modified to always close input and output streams
    public static void copyContent(File inputCopy, File outputCopy) throws IOException {
        FileInputStream in = new FileInputStream(inputCopy);
        FileOutputStream out = new FileOutputStream(outputCopy);

        try {
            int n;
            // read() function to read the
            // byte of data
            while ((n = in.read()) != -1) {
                // write() function to write
                // the byte of data
                out.write(n);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    public static void newCommit(String author, String message) throws IOException{

        boolean isInitialCommit = false;
        File HEAD = new File("git/HEAD");
        File commitFile = new File("git/objects/commits");


        String[] commitArr = new String[5];

        String currHeadHash;
        if(!HEAD.createNewFile()) {
            
            currHeadHash = Files.readString(Paths.get(HEAD.getPath())).trim();
            List<String> lines = Files.readAllLines(Paths.get("git/objects/" + currHeadHash));
            for(int i = 0; i < commitArr.length; i++) {
                String[] splitty = lines.get(i).split("\\s+");
                if(splitty.length < 2) {
                    commitArr[i] = "";
                }
                else {
                    commitArr[i] = splitty[1];
                }
            }
            
        }
        else {
            currHeadHash = "";
            isInitialCommit = true;
            Arrays.fill(commitArr, "");
            
        }


        String previousTreeHash = commitArr[1];

        File wDir = new File("git/objects/workingDirectory");
        File index = new File("git/index");
        

        String indexText = Files.readString(Paths.get(index.getPath()));

        BufferedWriter bw = new BufferedWriter(new FileWriter(wDir));

        bw.append(indexText);

        bw.close();

        String wDirHash;

        if(!isInitialCommit) {
            wDirHash = fillWorkingDir(wDir.getName(), previousTreeHash);
        }
        else {
            wDirHash = sha1Generator(wDir);
        }
        Path wDirPath = Paths.get(wDir.getPath());
        
        if(new File("git/objects" + wDirHash).exists()) {
            wDir.delete();
        }
        else {
            Files.move(wDirPath, wDirPath.resolveSibling(wDirHash));
        }
        
        StringBuilder commitText = new StringBuilder();

        commitText.append("tree: " + wDirHash + "\n");
        if(isInitialCommit) {
            commitText.append("parent: \n");
        }
        else {
            commitText.append("parent: " + currHeadHash + "\n");
        }
        commitText.append("author: " + author + "\n");

        LocalDate dateUnformatted = LocalDate.now();
        DateTimeFormatter  formatter =  DateTimeFormatter.ofPattern("MMMM d, yyyy");

        commitText.append("date: " + dateUnformatted.format(formatter) + "\n");
        commitText.append("message: " + message);

        commitFile.createNewFile();
        
        BufferedWriter commitWriter = new BufferedWriter(new FileWriter(commitFile));
        commitWriter.append(commitText.toString());
        commitWriter.close();

        String currCommitHash = sha1Generator(commitFile);

        Path currCommitPath = Paths.get(commitFile.getPath());
        Files.move(currCommitPath, currCommitPath.resolveSibling(currCommitHash));

        BufferedWriter headWriter = new BufferedWriter(new FileWriter(HEAD));

        headWriter.append(currCommitHash);

        headWriter.close();
    }


    public static String fillWorkingDir(String wDirName, String prevWDirName) throws IOException {


        File wDir = new File("git/objects/" + wDirName);
        File prevWDir = new File("git/objects/" + prevWDirName);

        BufferedReader br = new BufferedReader(new FileReader(prevWDir));

        char[] prevWDirCArr = new char[(int) prevWDir.length()];
        
        br.read(prevWDirCArr);

        

        br.close();

        BufferedWriter bw = new BufferedWriter(new FileWriter(wDir));

        bw.append(prevWDirName);

        bw.close();
        
        String hashedName = sha1Generator(wDir);

        return hashedName;
    }

}
