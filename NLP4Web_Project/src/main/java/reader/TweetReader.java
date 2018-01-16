package reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.dkpro.tc.api.io.TCReaderSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * DummyReader.
 * 
 * @author Daniel Wehner
 *
 */
public class TweetReader extends JCasCollectionReader_ImplBase implements TCReaderSequence {
	public final static String PARAM_TEXT_FOLDER = "TextFolder";

	// path to folder to read
	@ConfigurationParameter(name = PARAM_TEXT_FOLDER, description = "Path to folder which contains the texts to be read", mandatory = true)
	private String pathToFolder;

	// list of documents
	List<String> docs = new ArrayList<>();

	// index of current document
	int current = 0;

	/**
	 * Read the files from the folder specified.
	 * 
	 * @param context
	 */
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		Gson gson = new Gson();

		// iterate over all files in the folder
		for (File f : new File(pathToFolder).listFiles()) {
			// check if f is a file
			if (f.isFile()) {
				// create a buffered reader for each file
				try (BufferedReader inputReader = new BufferedReader(new FileReader(f))) {
					// read each file
					String line;
					while ((line = inputReader.readLine()) != null) {
						String text = parse(line);
						System.out.println(text);
						docs.add(text);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String parse(String jsonLine) {
	    JsonElement jelement = new JsonParser().parse(jsonLine);
	    JsonObject  jobject = jelement.getAsJsonObject();
	    String result = jobject.get("text").getAsString();
	    return result;
	}
	
	/**
	 * Indicate progress while reading.
	 * 
	 * @return
	 */
	@Override
	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(current + 1, docs.size(), Progress.ENTITIES) };
	}

	/**
	 * Indicate if there are more documents to read.
	 * 
	 * @return
	 */
	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return current < docs.size();
	}

	/**
	 * Get the next document from the collection.
	 * 
	 * @param jCas
	 */
	@Override
	public void getNext(JCas jCas) throws IOException, CollectionException {
		jCas.setDocumentText(docs.get(current++));
	}

	@Override
	public String getTextClassificationOutcome(JCas arg0, TextClassificationTarget arg1) throws CollectionException {
		return null;
	}
}
