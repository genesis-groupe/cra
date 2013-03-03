package helpers.binder;

import play.mvc.PathBindable;

import java.math.BigDecimal;

/**
 * @author f.patin
 * @link http://stackoverflow.com/questions/10286725/how-to-bind-double-parameter-with-play-2-0-routing/10307927#10307927
 */
public class BigDecimalW implements PathBindable<BigDecimalW> {
	public BigDecimal value;

	@Override
	public BigDecimalW bind(String key, String txt) {
		this.value = new BigDecimal(txt);
		return this;
	}

	@Override
	public String unbind(String key) {
		return value.toPlainString();
	}

	@Override
	public String javascriptUnbind() {
		return value.toPlainString();
	}
}
