package constants;

import com.google.common.base.Predicate;
import models.Customer;

import javax.annotation.Nullable;

/**
 * User: f.patin
 * Date: 11/08/12
 * Time: 19:23
 */
public class Genesis {

	public static final String NAME = "genesis";

	public static final Predicate<Customer> WITHOUT_GENESIS = new Predicate<Customer>() {
		@Override
		public boolean apply(@Nullable Customer customer) {
			return !Genesis.NAME.equals(customer.name);
		}
	};
}
