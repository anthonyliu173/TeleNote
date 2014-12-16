package com.example.telenote;

import java.util.Locale;

/*
 * Provide phone status / default language to other class 
 */

public class CallStatus {
	public static String callStatus = "out";
	static String UserDefinedLanguage = "!@#(";

	public static boolean shouldDisable = false;

	public String detectLanguage() {
		if (UserDefinedLanguage.contains("!@#(")) {
			String Language = String.valueOf(Locale.getDefault());
			if (Language.contains("zh_CN")) {
				Language = "cn_MA";
			}
			return Language;
		}else{
			return UserDefinedLanguage;
		}
	}

}
