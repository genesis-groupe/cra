package controllers;

import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Transient;
import com.google.common.collect.Lists;
import models.Employee;
import play.libs.F;
import play.mvc.Controller;
import security.RoleName;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * @author leo
 */
public abstract class AbstractController extends Controller {

	protected abstract static class UpdateForm {

		protected abstract void ensureDbField();

		public List<F.Tuple<String, Object>> fields() throws IllegalAccessException {
			ensureDbField();
			final List<F.Tuple<String, Object>> tuples = Lists.newArrayList();
			final Field[] fields = this.getClass().getFields();
			for (Field field : fields) {
				if (!field.isAnnotationPresent(Id.class) && !field.isAnnotationPresent(Transient.class)) {
					tuples.add(F.Tuple(field.getName(), field.get(this)));
				}
			}
			return tuples;
		}
	}

	protected static interface CreateForm<T> {
		public T to();
	}


	protected static List<Employee> loadEmployees(final String roleName){
		final List<Employee> employees = Lists.newArrayList();
		if (RoleName.ADMINISTRATOR.equals(roleName) || RoleName.GESTION.equals(roleName)) {
			employees.addAll(Employee.getOnlyEmployees());
			Collections.sort(employees, Employee.BY_TRIGRAMME);
		}
		return employees;
	}
}
