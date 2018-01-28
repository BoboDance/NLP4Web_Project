package featureExtractor;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

public class CharacterFeatureExtractor extends FeatureExtractorResource_ImplBase implements FeatureExtractor {

	@Override
	public Set<Feature> extract(JCas jCas, TextClassificationTarget target) throws TextClassificationException {

        Set<Feature> featSet = new HashSet<Feature>();
		
		String text = jCas.getDocumentText();
		char[] chars = text.toCharArray();

		int upperCaseCtr = 0;
		int alpabeticCtr = 0;
		int digitCtr = 0;
		int specialCharCtr = 0;
		int whiteSpaceCtr = 0;
		int tabSpaceCtr = 0;

		for (char c : chars) {
			if (Character.isUpperCase(c)) upperCaseCtr++;
			if (Character.isAlphabetic(c)) alpabeticCtr++;
			if (Character.isDigit(c)) digitCtr++;
			if (!Character.isDigit(c) && !Character.isAlphabetic(c)) specialCharCtr++;
			if (Character.isWhitespace(c)) whiteSpaceCtr++;
			if (c == '\t') tabSpaceCtr++;
		}

        featSet.add(new Feature("Amount_Upper_Case", upperCaseCtr));
        featSet.add(new Feature("Amount_Alphabetic", alpabeticCtr));
        featSet.add(new Feature("Amount_Digits", digitCtr));
        featSet.add(new Feature("Amount_Special", specialCharCtr));
        featSet.add(new Feature("Amount_White_Space", whiteSpaceCtr));
        featSet.add(new Feature("Special_Freq", specialCharCtr/chars.length));
        featSet.add(new Feature("Amount_Tab_Space", tabSpaceCtr));

		
		return featSet;
	}

}
