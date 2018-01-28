package pipeline;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import org.dkpro.tc.features.length.NrOfTokensPerSentence;
import org.dkpro.tc.features.style.ContextualityMeasureFeatureExtractor;
import org.dkpro.tc.features.style.ExclamationFeatureExtractor;
import org.dkpro.tc.features.style.ModalVerbsFeatureExtractor;
import org.dkpro.tc.features.style.TypeTokenRatioFeatureExtractor;
import org.dkpro.tc.features.twitter.EmoticonRatio;
import org.dkpro.tc.features.twitter.NumberOfHashTags;
import org.dkpro.tc.ml.ExperimentCrossValidation;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.report.BatchCrossValidationReport;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
import org.dkpro.tc.ml.weka.WekaClassificationAdapter;

import de.tudarmstadt.ukp.dkpro.core.arktools.ArktweetPosTagger;
import de.tudarmstadt.ukp.dkpro.core.arktools.ArktweetTokenizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import reader.TweetReader;
import weka.classifiers.bayes.NaiveBayes;

// TODO: classifiers
enum Classifier {
	WekaNaiveBayes,
	Deeplearning4j
}

public class Pipeline implements Constants {
	
	private static final String PATH_TO_TWEETS_TRAIN = new File(
		Pipeline.class.getResource("/tweets_train/").getFile()
	).getAbsolutePath().replace("target\\classes", "src\\main\\resources").replaceAll("%20", " ");
	
	private static final String PATH_TO_TWEETS_TEST = new File(
		Pipeline.class.getResource("/tweets_test/").getFile()
	).getAbsolutePath().replace("target\\classes", "src\\main\\resources").replaceAll("%20", " ");
	
	public static void main(String[] args) {
		System.setProperty("java.util.logging.config.file", "src/main/resources/logging.properties");
		
		// ensure DKPRO_HOME environment variable is set
		DemoUtils.setDkproHome(Pipeline.class.getSimpleName());
		
		Pipeline pipeline = new Pipeline();
		
		try {
			pipeline.runTrainTest(getParameterSpace(), Classifier.WekaNaiveBayes);
			// demo.runCrossValidation(getParameterSpace(), Classifiers.CRFSuite);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// Cross validation
	protected void runCrossValidation(ParameterSpace pSpace, int num_folds, Classifier clf) throws Exception {
		if(clf == Classifier.WekaNaiveBayes) {
			// TODO
			ExperimentCrossValidation batch = new ExperimentCrossValidation("TwitterSherlockWekaNB", WekaClassificationAdapter.class, num_folds);
			batch.setPreprocessing(getPreprocessing());
			batch.setParameterSpace(pSpace);
			batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
			batch.addReport(BatchCrossValidationReport.class);
			
			Lab.getInstance().run(batch);
			
		}else if (clf == Classifier.Deeplearning4j) {
			// TODO
		}
		
	}

	// Train/Test evaluation
	protected void runTrainTest(ParameterSpace pSpace, Classifier clf) throws Exception {
		if(clf == Classifier.WekaNaiveBayes) {
			// TODO
			ExperimentTrainTest batch = new ExperimentTrainTest("TwitterSherlockWekaNB", WekaClassificationAdapter.class);
			batch.setPreprocessing(getPreprocessing());
			batch.setParameterSpace(pSpace);
			batch.addReport(BatchTrainTestReport.class);
			batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
			
			Lab.getInstance().run(batch);
			
		}else if (clf == Classifier.Deeplearning4j) {
			// TODO
//			DemoUtils.setDkproHome(DeepLearning4jDocumentTrainTest.class.getSimpleName());
//
//	        DeepLearningExperimentTrainTest batch = new DeepLearningExperimentTrainTest("TwitterSherlockDeeplearning4j", Deeplearning4jAdapter.class);
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
				TweetReader.class, TweetReader.PARAM_TEXT_FOLDER, PATH_TO_TWEETS_TRAIN);
		
		CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
				TweetReader.class, TweetReader.PARAM_TEXT_FOLDER, PATH_TO_TWEETS_TEST);

		Map<String, Object> dimReaders = new HashMap<String, Object>();
		dimReaders.put(DIM_READER_TRAIN, readerTrain);
		dimReaders.put(DIM_READER_TEST, readerTest);

		Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(
                DIM_FEATURE_SET,
                new TcFeatureSet(TcFeatureFactory.create(NrOfTokensPerSentence.class),
                		TcFeatureFactory.create(TypeTokenRatioFeatureExtractor.class),
                		TcFeatureFactory.create(ContextualityMeasureFeatureExtractor.class),
                		//TcFeatureFactory.create(ModalVerbsFeatureExtractor.class),
                		TcFeatureFactory.create(ExclamationFeatureExtractor.class),
                		//TcFeatureFactory.create(SuperlativeRatioFeatureExtractor.class),
                		//TcFeatureFactory.create(PastVsFutureFeatureExtractor.class), //Penn Treebank Tagset only for this one!!!
                        TcFeatureFactory.create(EmoticonRatio.class),
                        TcFeatureFactory.create(NumberOfHashTags.class)));
		
		@SuppressWarnings("unchecked")
		Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new String[] { NaiveBayes.class.getName() }));

		ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
				Dimension.create(DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL),
				Dimension.create(DIM_FEATURE_MODE, Constants.FM_DOCUMENT), dimFeatureSets, dimClassificationArgs);

		return pSpace;
	}

	protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException {
//		return createEngineDescription(NoOpAnnotator.class);
		
		return createEngineDescription(
				createEngineDescription(ArktweetTokenizer.class),
                createEngineDescription(ArktweetPosTagger.class, ArktweetPosTagger.PARAM_LANGUAGE,
                        "en", ArktweetPosTagger.PARAM_VARIANT, "default"),
                createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE, "en"));
	}
	
}
