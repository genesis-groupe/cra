package helpers.formatter;

import constants.FeeType;
import org.apache.commons.lang3.StringUtils;
import play.data.format.Formatters;

import java.text.ParseException;
import java.util.Locale;

/**
 * @author f.patin
 */
public class FeesTypeFormatter extends Formatters.SimpleFormatter<FeeType> {
	@Override
	public FeeType parse(String text, Locale locale) throws ParseException {
		if (StringUtils.isBlank(text)) {
			return null;
		}
		return FeeType.byName(text);
	}

	@Override
	public String print(FeeType feesType, Locale locale) {
		return feesType.label;
	}
}
