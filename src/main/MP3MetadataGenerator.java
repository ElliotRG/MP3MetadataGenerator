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
	private static final String MP3_EXTENSION = ".mp3";
	private static final String ARTIST_SONG_SEPARATOR = " - ";
	private static final String SELECT_FORMAT_WINDOW_TITLE = "Select MP3 filename format";
	private static final String SELECT_FORMAT_WINDOW_TEXT = "What MP3 filename format do you use?";
	private static final String SELECT_FOLDER_WINDOW_TITLE = "Select MP3 files folder";
	private static final String SELECT_FOLDER_WINDOW_TEXT = "Use current folder?";

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException uiException) {
			uiException.printStackTrace();
		}
		
		String[] formats = {"Artist - Song", "Song - Artist"};
		Object selected = JOptionPane.showInputDialog(null, SELECT_FORMAT_WINDOW_TEXT, SELECT_FORMAT_WINDOW_TITLE, JOptionPane.DEFAULT_OPTION, 
				null, formats, formats[0]);
		boolean firstArtistFormat = true;
		if (selected != null) {
		    if (!formats[0].equals(selected.toString())) {
		    	firstArtistFormat = false;
		    }
		} else {
			closeApp();
		}

		int userAnswer = JOptionPane.showConfirmDialog(null, SELECT_FOLDER_WINDOW_TEXT, SELECT_FOLDER_WINDOW_TITLE, JOptionPane.YES_NO_OPTION); 
		if (userAnswer == JOptionPane.CLOSED_OPTION) {
			closeApp();
		} else if (userAnswer == JOptionPane.NO_OPTION) {
			JFileChooser chooser = new JFileChooser();
		    chooser.setCurrentDirectory(new java.io.File(System.getProperty("user.home") + File.separator + "Music"));
		    chooser.setDialogTitle(SELECT_FOLDER_WINDOW_TITLE);
		    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    chooser.setAcceptAllFileFilterUsed(false);
		    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		    	MP3_DIRECTORY = chooser.getSelectedFile().getAbsolutePath();
		    } else {
		    	closeApp();
		    }
		}
		
		System.out.println("========================================================");
		System.out.println("**************** MP3 METADATA GENERATOR ****************");
		System.out.println("========================================================");
		System.out.println("Finding MP3 files in " + MP3_DIRECTORY + " ...");
		System.out.println("========================================================");
		
		for (File file : findFilesInDirectory(MP3_DIRECTORY)) {
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
				
				cleanOriginalMetadata(metadata);
				mp3file.setId3v1Tag(metadata);
				fileName = fileName.substring(0, fileName.length() - MP3_EXTENSION.length());
				String[] fileNameSplit = fileName.split(ARTIST_SONG_SEPARATOR);
				
				System.out.println("Setting metadata for: " + fileName);

				String artist = fileNameSplit[firstArtistFormat ? 0 : 1].trim();
				metadata.setArtist(artist);
				
				String title = cleanTitle(fileNameSplit[firstArtistFormat ? 1 : 0]);
				metadata.setTitle(title);
				metadata.setAlbum(title.split("\\(")[0]);
				
				if (firstArtistFormat) {
					fileName = artist + ARTIST_SONG_SEPARATOR + title;
				} else {
					fileName = title + ARTIST_SONG_SEPARATOR + artist;
				}

				try {
					mp3file.save(MP3_DIRECTORY + File.separator + "@" + fileName + MP3_EXTENSION);
				} catch (NotSupportedException e) {
					e.printStackTrace();
				}
			} catch (UnsupportedTagException | InvalidDataException | IOException e) {
				e.printStackTrace();
			}
			
			file.delete();
			System.out.println("========================================================");
		}
		
		for (File file : findFilesInDirectory(MP3_DIRECTORY)) {
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
		for (String p : patterns) {
			if (cleanTitle.contains(p)) {
				// Adding 1 to endIndex to include ) or ] after the pattern words,
				// if there are. If there aren't, then after the word a space will be 
				// included but later removed by trim at return.
				cleanTitle = rawTitle.substring(0, rawTitle.indexOf(p) + p.length() + 1);
			}
		}
		
		cleanTitle = cleanTitle.replace("my-free-mp3s.com", "");
		
		return cleanTitle.trim();
	}
	
	public static File[] findFilesInDirectory(String dirName) {
		File dir = new File(dirName);
        return dir.listFiles(new FilenameFilter() {
        	public boolean accept(File dir, String filename) {
        		return filename.endsWith(MP3_EXTENSION);
        	}
        });
	}
	
	public static void closeApp() {
		System.err.println("No MP3 Folder Selected");
    	System.err.println("Closing MP3 Metadata Generator");
    	System.exit(1);
	}

}
