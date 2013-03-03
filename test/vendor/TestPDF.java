package vendor;

import models.Cra;
import models.Mission;
import org.joda.time.DateTimeConstants;
import org.junit.Test;
import testUtils.AbstractTest;

import static play.mvc.Results.ok;
import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;
import static play.mvc.Http.Status.OK;
import static testUtils.TestFactory.*;

/**
 * @author f.patin
 */
public class TestPDF extends AbstractTest {

	private Cra cra;

	@Override
	protected String getConnexionRole() {
		return null;
	}

	@Override
	protected void postBeforeEachTest() {
		final Mission mission = newMission("M1", "ML1", newCustomer("PdfTestCustomer").id);
		cra = newCra(collaborator, 2012, DateTimeConstants.JANUARY);
		addDay(cra, 2, mission);
		addDay(cra, 3, mission);
		addDay(cra, 4, mission);
		addDay(cra, 7, mission);
		addDay(cra, 14, mission);
	}

	@Override
	protected void preAfterEachTest() {
	}

	@Test
	public void testExportCra() {
		PDFData pdfData = new PDFCra(cra);
		byte[] pdf = PDFv2.apply(pdfData);
		assertThat(pdf).isNotEmpty();
	}

}
