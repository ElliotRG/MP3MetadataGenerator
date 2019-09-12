package main;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v22Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

public class MP3MetadataGenerator {
	
	private static String MP3_DIRECTORY = "." + File.separator;
	private static final String WINDOW_TITLE = "Select MP3 files folder";

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException uiException) {
			uiException.printStackTrace();
		}

		int userAnswer = JOptionPane.showConfirmDialog(null, "Use current folder?", WINDOW_TITLE, JOptionPane.YES_NO_OPTION); 
		if(userAnswer == JOptionPane.YES_OPTION) {
			MP3_DIRECTORY = ".";
		} else if (userAnswer == JOptionPane.NO_OPTION) {
			JFileChooser chooser = new JFileChooser();
		    chooser.setCurrentDirectory(new java.io.File(System.getProperty("user.home") + File.separator + "Music"));
		    chooser.setDialogTitle(WINDOW_TITLE);
		    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    chooser.setAcceptAllFileFilterUsed(false);
		    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		    	MP3_DIRECTORY = chooser.getSelectedFile().getAbsolutePath();
		    } else {
		    	System.err.println("No MP3 Folder Selected");
		    	System.err.println("Closing MP3 Metadata Generator");
		    	System.exit(1);
		    }
		} else {
			System.err.println("No MP3 Folder Selected");
	    	System.err.println("Closing MP3 Metadata Generator");
	    	System.exit(1);
		}
		
		File[] files = findFilesInDirectory(MP3_DIRECTORY);
		System.out.println("========================================================");
		System.out.println("**************** MP3 METADATA GENERATOR ****************");
		System.out.println("========================================================");
		System.out.println("Finding MP3 files in " + MP3_DIRECTORY + " ...");
		System.out.println("========================================================");
		
		for(File file : files) {
			String filePath = file.getPath();
			String fileName = file.getName();
			
			try {
				Mp3File mp3file = new Mp3File(filePath);
				ID3v2 metadata;
				if (mp3file.hasId3v2Tag()) {
					System.out.println(fileName + " has ID3V2");
					metadata =  mp3file.getId3v2Tag();
					metadata.setAlbumArtist(" ");
					metadata.setTrack("");
					metadata.setComment(" ");
					metadata.setComposer(" ");
					metadata.setItunesComment(" ");
				} else {
					System.err.println(fileName + " has not ID3V2");
					metadata = new ID3v22Tag();
				}
				mp3file.setId3v1Tag(metadata);
				fileName = fileName.substring(0, fileName.length() - 4);
				String[] fileNameSplit = fileName.split(" - ");
				System.out.println("Setting metadata for: " + fileName);
				metadata.setArtist(fileNameSplit[0]);
				metadata.setTitle(fileNameSplit[1]);
				if(fileNameSplit[1].toLowerCase().contains("remix")) {
					metadata.setAlbum(fileNameSplit[1]);
				} else {
					metadata.setAlbum(fileNameSplit[1].split("\\(")[0]);
				}
				try {
					mp3file.save(MP3_DIRECTORY + File.separator + "@" + fileName + ".mp3");
				} catch (NotSupportedException e) {
					e.printStackTrace();
				}
			} catch (UnsupportedTagException | InvalidDataException | IOException e) {
				e.printStackTrace();
			}
			file.delete();
			System.out.println("========================================================");
			// TODO: Try album cover fetch with Google Images API
			try {
				SongCoverSearcher.getImage(fileName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		files = findFilesInDirectory(MP3_DIRECTORY);
		for(File file : files) {
			file.renameTo(new File(MP3_DIRECTORY + File.separator + file.getName().substring(1)));
		}
		System.out.println("========================================================");
	}
	
	public static File[] findFilesInDirectory(String dirName) {
		File dir = new File(dirName);
        return dir.listFiles(new FilenameFilter() {
        	public boolean accept(File dir, String filename) {
        		return filename.endsWith(".mp3");
        	}
        });
	}

}
