package com.diana.parser;

import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;

public class FileMover {
    private File targetFolder;
    private File sourceFolder;
    private XMLParser parser;
    private File customLabelsFile;
    private ArrayList<String> filesPath;

    public FileMover(String sourceFolderPath, String targetFolderPath, String customLabelsFilePath, XMLParser parser) throws Exception {
        this.sourceFolder = new File(sourceFolderPath);
        this.targetFolder = new File(targetFolderPath);
        this.customLabelsFile = new File(customLabelsFilePath);
        if (!this.sourceFolder.exists()) {
            throw new Exception(String.format("Folder %s doesn't exist.", this.sourceFolder.getAbsolutePath()));
        }
        if (!this.targetFolder.exists()) {
            createFolder(this.targetFolder.getPath());
        }
        this.parser = parser;
    }

    public void invoke() throws IOException, SAXException, ParserConfigurationException {
        this.filesPath = parser.getFoldersMapping();
        if (this.filesPath.isEmpty()) {
            System.out.println("No files to copy");
            return;
        }

        System.out.println("\nStarted copying ...");
        cleanFolder(this.targetFolder);

        for (File sourceFile : this.sourceFolder.listFiles()) {
            if (!sourceFile.isDirectory()) {
                continue;
            }
            copyFiles(sourceFile.listFiles(), sourceFile.getName());
        }

        copyFile(parser.getXMLFIle(), this.targetFolder );
        copyCustomLablesFile();

        System.out.println("Completed copying ...\n");
    }
    
    private void copyCustomLablesFile(){
    	try {
	        String labelsFolderPath = this.targetFolder.getPath() + "\\" +  "labels";
	        File labelsFolder = new File(labelsFolderPath);
	        if (!labelsFolder.exists()){
	        	createFolder(labelsFolderPath);
	        }
	        copyFile(this.customLabelsFile, labelsFolder);
    	} catch (Exception e) {
    		System.out.println(e.getMessage());
    	}
    }

    @Override
    public String toString() {
        String str = "";
        for (String filePath : this.filesPath) {
            str += filePath + "\n";
        }
        return str;
    }

    private void cleanFolder(File folder) throws IOException {
        if (folder.listFiles() == null) {
            return;
        }
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                cleanFolder(file);
            }
            file.delete();
            //System.out.println(String.format("%s is deleted.", file.getAbsolutePath()));
        }
    }

    private void createFolder(String newFolderPath) {
        File newFolder = new File(newFolderPath);
        if (!newFolder.exists() && newFolder.mkdirs()) {
            System.out.println(String.format("Folder created: %s.", newFolderPath));
        }
    }

    private String getFileNameWithoutExtension(String nameWithExtension) {
        String pathToTargetFile = nameWithExtension;
        if (pathToTargetFile.endsWith("-meta.xml")) {
            pathToTargetFile = pathToTargetFile.substring(0, pathToTargetFile.lastIndexOf("-meta.xml"));
        }

        if (pathToTargetFile.contains(".")) {
            pathToTargetFile = pathToTargetFile.substring(0, pathToTargetFile.lastIndexOf("."));
        }
        return pathToTargetFile;
    }

    private void copyFiles(File[] sourceFiles, String targetFolderName) throws IOException {
        for (File fileFrom : sourceFiles) {
            if (fileFrom.isDirectory()) {
                String newFolderName = fileFrom.getParentFile().getName() + "/" + fileFrom.getName();
                copyFiles(fileFrom.listFiles(), newFolderName);
            } else {
                String nameWithExtension = targetFolderName + "/" + fileFrom.getName();
                if (!this.filesPath.contains(getFileNameWithoutExtension(nameWithExtension))) {
                    continue;
                }

                createFolder(this.targetFolder.getAbsolutePath() + "/" + targetFolderName);
                copyFile(nameWithExtension);
            }
        }
    }
    
    private void copyFile(String sourceFileName, String targetFileName) throws IOException {
        try (
			InputStream inputStream = new FileInputStream(sourceFileName);
			OutputStream outputStream = new FileOutputStream(targetFileName);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)
		) {
        	boolean eof = false;
        	while (!eof){
        		byte currentByte = (byte) bufferedInputStream.read();
        		if( currentByte != -1) {
        			bufferedOutputStream.write(currentByte);
        		} else {
        			eof = true;
        		}
        	}        	
        } catch (IOException ex) {
            throw new IOException(String.format("Failed to copy %s to %s.", sourceFileName, targetFileName));
        } catch (Exception e) {
        	System.out.println(e.getMessage());
        }    	
    }
    
    private void copyFile(File fileToCopy, File folderToCopy) throws IOException {
    	String sourceFileName = fileToCopy.getPath();
    	String targetFileName = folderToCopy.getPath() + "/" + fileToCopy.getName();

        copyFile(sourceFileName, targetFileName);
    }
    
    private void copyFile(String fileName) throws IOException {
        String sourceFileName = this.sourceFolder.getAbsolutePath() + "/" + fileName;
        String targetFileName = this.targetFolder.getAbsolutePath() + "/" + fileName;

        copyFile(sourceFileName, targetFileName);
    }    
}
