package edu.fges.shorturl.repository;

import edu.fges.shorturl.domain.User;

public interface UserRepository {
	public void saveUser(User user);
	public void getUser(String email,String pwd);
	public boolean checkUserEmail(String email);
	public boolean checkUserEmailPwd(User user);
}