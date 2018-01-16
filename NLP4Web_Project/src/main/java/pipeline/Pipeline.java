package pipeline;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.text.BreakIterator;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.features.length.NrOfChars;
import org.dkpro.tc.ml.ExperimentCrossValidation;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.crfsuite.CRFSuiteAdapter;
import org.dkpro.tc.ml.report.BatchCrossValidationReport;
import org.dkpro.tc.ml.report.BatchTrainTestReport;

import de.tudarmstadt.ukp.dkpro.core.arktools.ArktweetPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import reader.TweetReader;

// TODO: classifiers
enum Classifier {
	CRFSuite,
	DeepRNN
}

public class Pipeline implements Constants {
	
	//TODO: path to data folders
	private static final String PATH_TO_TWEETS = new File(
		Pipeline.class.getResource("/tweets_raw/").getFile()
	).getAbsolutePath();
	
	public static void main(String[] args) {
		System.setProperty("java.util.logging.config.file", "src/main/resources/logging.properties");
		
		// ensure DKPRO_HOME environment variable is set
		DemoUtils.setDkproHome(Pipeline.class.getSimpleName());
		
		Pipeline pipeline = new Pipeline();
		
		try {
			pipeline.runTrainTest(getParameterSpace(), Classifier.CRFSuite);
			// demo.runCrossValidation(getParameterSpace(), Classifiers.CRFSuite);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// Cross validation
	protected void runCrossValidation(ParameterSpace pSpace, int num_folds, Classifier clf) throws Exception {
		if(clf == Classifier.CRFSuite) {
			// TODO
			ExperimentCrossValidation batch = new ExperimentCrossValidation("TODO", CRFSuiteAdapter.class, num_folds);
			batch.setPreprocessing(getPreprocessing());
			batch.setParameterSpace(pSpace);
			batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
			batch.addReport(BatchCrossValidationReport.class);
			
			Lab.getInstance().run(batch);
			
		}else if (clf == Classifier.DeepRNN) {
			// TODO
		}
		
	}

	// Train/Test evaluation
	protected void runTrainTest(ParameterSpace pSpace, Classifier clf) throws Exception {
		if(clf == Classifier.CRFSuite) {
			// TODO
			ExperimentTrainTest batch = new ExperimentTrainTest("TODO", CRFSuiteAdapter.class);
			batch.setPreprocessing(getPreprocessing());
			batch.setParameterSpace(pSpace);
			batch.addReport(BatchTrainTestReport.class);
			batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
			
			Lab.getInstance().run(batch);
			
		}else if (clf == Classifier.DeepRNN) {
			// TODO
//			DemoUtils.setDkproHome(DeepLearning4jDocumentTrainTest.class.getSimpleName());
//
//	        DeepLearningExperimentTrainTest batch = new DeepLearningExperimentTrainTest("DeepLearning", Deeplearning4jAdapter.class);
//	        batch.setPreprocessing(getPreprocessing());
//	        batch.setParameterSpace(pSpace);
//	        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
//
//	        // Run
//	        Lab.getInstance().run(batch);
		}

	}
	
	// TODO: readers and features
	public static ParameterSpace getParameterSpace() throws ResourceInitializationException {
		
		CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
				TweetReader.class, TweetReader.PARAM_TEXT_FOLDER, PATH_TO_TWEETS);

		Map<String, Object> dimReaders = new HashMap<String, Object>();
		dimReaders.put(DIM_READER_TRAIN, readerTrain);

		Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET, new TcFeatureSet(
				TcFeatureFactory.create(NrOfChars.class)));

		ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
				Dimension.create(DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL),
				Dimension.create(DIM_FEATURE_MODE, Constants.FM_SEQUENCE), dimFeatureSets);

		return pSpace;
	}

	protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException {
		//return createEngineDescription(NoOpAnnotator.class);
		
		return createEngineDescription(
				createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(ArktweetPosTagger.class, ArktweetPosTagger.PARAM_LANGUAGE,
                        "en", ArktweetPosTagger.PARAM_VARIANT, "default"));
	}
	
}
