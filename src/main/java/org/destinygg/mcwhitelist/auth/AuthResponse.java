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
	public enum AuthResponseType{
		USER_NOT_FOUND ("User was not found.", false), 
		USER_NOT_SUB ("User is not subscribed.", false),
		BAD_REQUEST ("Auth request failed.", false),
		BAD_RESPONSE ("Auth response invalid.", false),
		VALID_AUTH ("Valid subscriber authenticated.", true);
		
		private String message;
		private boolean validResponse;
		private AuthResponseType(String message, boolean validResponse){
			this.message = message;
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
