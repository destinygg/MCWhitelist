/**
 * 
 */
package org.destinygg.mcwhitelist.auth;

/**
 * Provides Authentication response status and data
 * 
 * @author xtphty
 *
 */
public class AuthResponse {
	/**
	 * Enumerates possible errors and appropriate user messages
	 */
	public enum AuthResponseType{
		USER_NOT_FOUND ("Your minecraft account is not registered on destiny.gg.", false),
		USER_NOT_SUB ("This minecraft account is registered to a destiny.gg plebian (non-subscriber).", false),
		USER_BANNED("This minecraft account is registered to a banned destiny.gg user.", false),
		UUID_ALREADY_TAKEN("Minecraft account change detected on your destiny.gg profile, account changes are not allowed.", false),
		BAD_REQUEST ("Destiny.gg authentication request failed, message an admin with this error.", false),
		BAD_RESPONSE ("Auth response invalid.", false),
		VALID_AUTH ("Valid subscriber authenticated.", true);
		
		private String message;
		private boolean validResponse;
		private AuthResponseType(String message, boolean validResponse){
			this.message = message;
			this.validResponse = validResponse;
		}
		public String getMessage(){
			return this.message;
		}
		public boolean isValidResponse(){
			return validResponse;
		}
	}
	
	public AuthUser authUser;
	public AuthResponseType authResponseType;

	public AuthResponse(CachedAuthUser authUser, AuthResponseType authResponseType) {
		this.authUser = authUser;
		this.authResponseType = authResponseType;
	}
}
