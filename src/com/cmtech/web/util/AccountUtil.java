package com.cmtech.web.util;

import static com.cmtech.web.util.DbUtil.INVALID_ID;

import com.cmtech.web.btdevice.Account;

public class AccountUtil {
	public static boolean signUp(Account account) {
		return (account.getId() == INVALID_ID && account.insert());
	}
	
	public static boolean login(Account account) {
		return (account.getId() != INVALID_ID);
	}
}
