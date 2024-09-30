import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Git {
    public static void main(String[] args) {
        // File git = new File("git");
        // File objects = new File("git/objects");
        // File index = new File("git/index");
    }

    public static void initializeGitRepoMethod() throws IOException {
        // creating pointers for files/directorys (does not actually create them)
        File git = new File("git");
        File objects = new File("git/objects");
        File index = new File("git/index");
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
    }

    // checks if files exist and then delete them to continue tester
    public static void initGitRepoTesterMethod() throws IOException {
        File git = new File("git");
        File objects = new File("git/objects");
        File index = new File("git/index");
        if (git.exists() == true) {
            git.delete();
        }
        if (objects.exists() == true) {
            objects.delete();
        }
        if (index.exists() == true) {
            index.delete();
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
        hashFile.createNewFile();

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
}
