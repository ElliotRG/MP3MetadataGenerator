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
	
	private static String MP3_DIRECTORY = new File("." + File.separator).getAbsolutePath();
	private static final String WINDOW_TITLE = "Select MP3 files folder";

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException uiException) {
			uiException.printStackTrace();
		}

		int userAnswer = JOptionPane.showConfirmDialog(null, "Use current folder?", WINDOW_TITLE, JOptionPane.YES_NO_OPTION); 
		if(userAnswer == JOptionPane.CLOSED_OPTION) {
			closeApp();
		} else if (userAnswer == JOptionPane.NO_OPTION) {
			JFileChooser chooser = new JFileChooser();
		    chooser.setCurrentDirectory(new java.io.File(System.getProperty("user.home") + File.separator + "Music"));
		    chooser.setDialogTitle(WINDOW_TITLE);
		    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    chooser.setAcceptAllFileFilterUsed(false);
		    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		    	MP3_DIRECTORY = chooser.getSelectedFile().getAbsolutePath();
		    } else {
		    	closeApp();
		    }
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
				} else {
					System.err.println(fileName + " has not ID3V2");
					metadata = new ID3v22Tag();
				}
				cleanOriginalMetadata(metadata);
				mp3file.setId3v1Tag(metadata);
				fileName = fileName.substring(0, fileName.length() - 4);
				String[] fileNameSplit = fileName.split(" - ");
				System.out.println("Setting metadata for: " + fileName);
				String artist = fileNameSplit[0];
				metadata.setArtist(artist);
				
				String title = cleanTitle(fileNameSplit[1]);
				metadata.setTitle(title);
				metadata.setAlbum(fileNameSplit[1].split("\\(")[0]);
				
				fileName = artist + " - " + title;
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
		}

		files = findFilesInDirectory(MP3_DIRECTORY);
		for(File file : files) {
			file.renameTo(new File(MP3_DIRECTORY + File.separator + file.getName().substring(1)));
		}
		System.out.println("========================================================");
	}
	
	public static ID3v2 cleanOriginalMetadata(ID3v2 metadata) {
		metadata.clearAlbumImage();
		metadata.setAlbum("");
		metadata.setAlbumArtist("");
		metadata.setArtist("");
		metadata.setArtistUrl("");
		metadata.setAudiofileUrl("");
		metadata.setAudioSourceUrl("");
		metadata.setComment("");
		metadata.setCommercialUrl("");
		metadata.setComposer("");
		metadata.setCopyright("");
		metadata.setCopyrightUrl("");
		metadata.setDate("");
		metadata.setGenreDescription("");
		metadata.setGrouping("");
		metadata.setItunesComment("");
		metadata.setKey("");
		metadata.setLyrics("");
		metadata.setOriginalArtist("");
		metadata.setPaymentUrl("");
		metadata.setPublisher("");
		metadata.setPublisherUrl("");
		metadata.setRadiostationUrl("");
		metadata.setTitle("");
		metadata.setTrack("");
		metadata.setUrl("");
		metadata.setYear("");

		return metadata;
	}
	
	public static String cleanTitle(String rawTitle) {
		String[] patterns = { "Mix", "Remix", "Edit", "Bootleg", "Version", 
				"Acapella", "Acoustic", "Anthem", "Soundtrack" };
		String cleanTitle = rawTitle;
		for(String p : patterns) {
			if(cleanTitle.contains(p)) {
				// Adding 1 to endIndex to include ) or ] after the pattern words,
				// if there are. If there aren't, then after the word a space will be 
				// included but later removed by trim at return.
				cleanTitle = rawTitle.substring(0, rawTitle.lastIndexOf(p) + p.length() + 1);
			}
		}
		
		return cleanTitle.trim();
	}
	
	public static File[] findFilesInDirectory(String dirName) {
		File dir = new File(dirName);
        return dir.listFiles(new FilenameFilter() {
        	public boolean accept(File dir, String filename) {
        		return filename.endsWith(".mp3");
        	}
        });
	}
	
	public static void closeApp() {
		System.err.println("No MP3 Folder Selected");
    	System.err.println("Closing MP3 Metadata Generator");
    	System.exit(1);
	}

}
