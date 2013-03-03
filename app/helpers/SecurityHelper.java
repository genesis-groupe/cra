package helpers;

import constants.ConfigurationKey;
import models.Employee;
import models.User;
import play.Configuration;
import play.Logger;
import play.mvc.Http;
import security.Secured;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.google.common.base.Preconditions.checkNotNull;

public class SecurityHelper {


	public static Employee findEmployeeByConnectedUser() {
		final User user = getConnectedUser();
		final Employee employee = Employee.findByTrigramme(user.trigramme);

		return employee;
	}


	public static User getConnectedUser() {
		return new Secured().getConnectedUser(Http.Context.current());
	}

	public static String md5(String password) {
		checkNotNull(password, "password must be not null");
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(password.getBytes(), 0, password.length());
			return new BigInteger(1, digest.digest()).toString(16);

			/**
			 * Méthode Play Framework 1.2
			 *
			 byte[] out = digest.digest(password.getBytes());
			 return new String(Base64.encodeBase64(out));
			 */
		} catch (NoSuchAlgorithmException e) {
			Logger.error(String.format("%s.md5 : %s", User.class.getSimpleName(), e.getMessage()));
			return null;
		}
	}

	public static String getDefaultEncryptedPassword(){
        return Configuration.root().getConfig(ConfigurationKey.DEFAULT.mainKey).getString("password");
	}
}
