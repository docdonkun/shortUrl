package edu.fges.shorturl.repository;

import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import edu.fges.shorturl.domain.User;

@Repository
public class UserRepositoryInpl implements UserRepository {

	private static final Logger logger = LogManager.getLogger(UserRepositoryInpl.class);

	@Autowired
	private DataSource dataSource;

	/**
	 * Save user in BDD
	 * 
	 * @param user
	 */
	public void saveUser(User user) {
		PreparedStatement preparedStatementInsert = null;

		try {
			if (user != null) {
				preparedStatementInsert = dataSource.getConnection()
						.prepareStatement("INSERT INTO user (email, pwd, ip) VALUES(?,?,?)");
				preparedStatementInsert.setString(1, user.getEmail());
				preparedStatementInsert.setString(2, sha256(user.getPwd()));
				preparedStatementInsert.setString(3, user.getIp());
				preparedStatementInsert.executeUpdate();
				ResultSet rs = preparedStatementInsert.getGeneratedKeys();
				if (rs.next()) {
					user.setId(rs.getInt(1));
				}
			}
		} catch (SQLException e) {
			logger.error("[UserRepositoryServiceInpl].[saveUser] SQLException - Erreur dans la requete");
			e.printStackTrace();
		} finally {
			try {
				preparedStatementInsert.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Check if the user exist in the bdd
	 * 
	 * @param user
	 * @return
	 */
	public boolean checkUserEmail(String email) {
		PreparedStatement preparedStatement = null;

		try {
			preparedStatement = dataSource.getConnection()
					.prepareStatement("SELECT id FROM user WHERE email = ? LIMIT 1");
			preparedStatement.setString(1, email);
			ResultSet result = preparedStatement.executeQuery();
			result.last();
			if (result.getRow() > 0) {
				return true;
			}
		} catch (SQLException e) {
			logger.error("[UserRepositoryServiceInpl].[checkUserEmail] SQLException - Erreur dans la requete");
			e.printStackTrace();
		} finally {
			try {
				preparedStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Check if the user with pwd is correct
	 * 
	 * @param user
	 * @return
	 */
	public boolean checkUserEmailPwd(User user) {
		PreparedStatement preparedStatement = null;

		try {
			preparedStatement = dataSource.getConnection()
					.prepareStatement("SELECT id,ip FROM user WHERE email = ? AND pwd = ? LIMIT 1");
			preparedStatement.setString(1, user.getEmail());
			preparedStatement.setString(2, sha256(user.getPwd()));
			ResultSet result = preparedStatement.executeQuery();
			boolean flag = false;
			while (result.next()) {
				user.setId(result.getInt("id"));
				user.setIp(result.getString("ip"));
				flag = true;
			}
			return flag;
		} catch (SQLException e) {
			logger.error("[UserRepositoryServiceInpl].[checkUserEmail] SQLException - Erreur dans la requete");
			e.printStackTrace();
		} finally {
			try {
				preparedStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * For encoding the password in sha256
	 * 
	 * @param base
	 * @return
	 */
	public static String sha256(String base) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(base.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();

			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}

			return hexString.toString();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
