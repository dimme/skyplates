import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Converter {

	public void run() throws IOException, ParserConfigurationException, SAXException {

		// Get the Windows path were SkyDemon saves it's PDFs.
		String path = System.getProperty("user.home") + "\\AppData\\Roaming\\Divelements Limited\\SkyDemon Plan\\Plates";

		// Check of the index file exists
		File indexFile = new File(path + "\\index.xml");
		if (!indexFile.exists()) {
			throw new IOException("index.xml does not exist");
		}

		// Read the index file
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(indexFile);
		
		// Extract all publishers and add them in a hashmap
		NodeList publishers = document.getElementsByTagName("Publisher");
		HashMap<Integer, String> publishersMap = new HashMap<Integer, String>();
		for (int i = 0; i < publishers.getLength(); i++) {
			NamedNodeMap atts = publishers.item(i).getAttributes();
			int id = Integer.valueOf(atts.getNamedItem("ID").getTextContent());
			String name = atts.getNamedItem("Name").getTextContent();
			publishersMap.put(id, name);

			// Create directories for publishers if they do not exist
			File publisherDir = new File("PDFs\\" + name);
			if (!publisherDir.exists())
				publisherDir.mkdirs();
		}
		
		// Get a list with all files available
		String[] filesInDir = new File(path).list();

		// Get a list with all plates from the xml
		NodeList plates = document.getElementsByTagName("Plate");

		for (int i = 0; i < plates.getLength(); i++) {

			// Get the plate ID
			int id = Integer.valueOf(plates.item(i).getAttributes().getNamedItem("ID").getTextContent());

			// If the file in index.xml does not exist locally, skip it and go to the next one. 
			if (Arrays.binarySearch(filesInDir, id + ".pdf") < 0)
				continue;

			// Get the new filename from index.xml
			int publisherId = Integer.valueOf(plates.item(i).getAttributes().getNamedItem("Publisher").getTextContent());
			String newFilename = "PDFs\\" + publishersMap.get(publisherId) + "\\" + plates.item(i).getAttributes().getNamedItem("OriginalFilename").getTextContent();

			// Rename and move the file to the new directory
			File oldFile = new File(path + "\\" + id + ".pdf");
			File newFile = new File(newFilename);

			if (Files.copy(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING) != null) {
				System.out.println("Done: " + id + ".pdf -> " + newFilename);
			} else {
				System.out.println("Error: " + id + ".pdf -> " + newFilename);
			}
			
		}
		
	}

	public static void main(String[] args) throws Exception {
		new Converter().run();
	}

}
