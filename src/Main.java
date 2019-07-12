import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v22Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

public class Main {
	
	public static final String DOWNLOADS_DIRECTORY = "D:\\Música\\Downloads";

	public static void main(String[] args) {
		File[] files = findFilesInDirectory(DOWNLOADS_DIRECTORY);
		System.out.println("========================================================");
		System.out.println("**************** MP3 METADATA GENERATOR ****************");
		System.out.println("========================================================");
		System.out.println("Finding MP3 files in " + DOWNLOADS_DIRECTORY + " ...");
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
				mp3file.setId3v1Tag(metadata);
				fileName = fileName.substring(0, fileName.length() - 4);
				String[] fileNameSplit = fileName.split(" - ");
				System.out.println("Setting metadata for: " + fileName);
				metadata.setArtist(fileNameSplit[0]);
				metadata.setTitle(fileNameSplit[1]);
				metadata.setAlbum(fileNameSplit[1].split("\\(")[0]);
				try {
					mp3file.save(DOWNLOADS_DIRECTORY + File.separator + "@" + fileName + ".mp3");
				} catch (NotSupportedException e) {
					e.printStackTrace();
				}
			} catch (UnsupportedTagException | InvalidDataException | IOException e) {
				e.printStackTrace();
			}
			file.delete();
			System.out.println("========================================================");
		}

		files = findFilesInDirectory(DOWNLOADS_DIRECTORY);
		for(File file : files) {
			file.renameTo(new File(DOWNLOADS_DIRECTORY + File.separator + file.getName().substring(1)));
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
