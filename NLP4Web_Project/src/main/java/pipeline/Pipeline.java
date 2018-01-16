package pipeline;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.features.length.NrOfChars;
import org.dkpro.tc.ml.ExperimentCrossValidation;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.crfsuite.CRFSuiteAdapter;
import org.dkpro.tc.ml.report.BatchCrossValidationReport;
import org.dkpro.tc.ml.report.BatchTrainTestReport;

// TODO: classifiers
enum Classifier {
	CRFSuite,
	DeepRNN
}

public class Pipeline {
	
	//TODO: path to data folders
	private static final String corpusFilePathTrain = "src/main/resources/data/train";
	private static final String corpusFilePathDev = "src/main/resources/data/test";
	private static final String corpusFilePathTest = "TODO";
	
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
		}

	}
	
	// TODO: readers and features
	public static ParameterSpace getParameterSpace() throws ResourceInitializationException {
		
//		CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(NERDemoReader.class,
//				NERDemoReader.PARAM_LANGUAGE, "en", NERDemoReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
//				NERDemoReader.PARAM_PATTERNS, INCLUDE_PREFIX + "*.txt");

//		Map<String, Object> dimReaders = new HashMap<String, Object>();
//		dimReaders.put(DIM_READER_TRAIN, readerTrain);
//		dimReaders.put(DIM_READER_TEST, readerDev);
//		dimReaders.put(DIM_READER_TEST, readerTest);

//		Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET, new TcFeatureSet(
//				TcFeatureFactory.create(NrOfChars.class)));

//		ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
//				Dimension.create(DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL),
//				Dimension.create(DIM_FEATURE_MODE, Constants.FM_SEQUENCE), dimFeatureSets);

//		return pSpace;
		
		return null;
	}

	protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException {
		return createEngineDescription(NoOpAnnotator.class); //TODO: preprocessing
	}
	
}
