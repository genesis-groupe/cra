package constants;

import org.apache.commons.lang3.StringUtils;

/**
 * @author leo
 *         Date: 08/12/12
 *         Time: 17:08
 */
public enum ExportFormat {
	PDF,
	HTML,
	NOT_SUPPORTED;

	public static ExportFormat byName(final String name) {
		try {
			return ExportFormat.valueOf(StringUtils.upperCase(name));
		} catch (IllegalArgumentException e) {
			return NOT_SUPPORTED;
		}
	}
}
