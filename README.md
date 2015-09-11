MCWhitelist
==============

##A plugin for whitelisting users.

###Nothing is done in this readme, these are just the plans

Based originally on the https://github.com/Bitjump/SubWhitelister mod.

It calls URL's to handle whitlisting of users.
For new users it requires the user to enter a shared secret that will be sent to the api endpoint specified in the configuration file.
For users that have been whitelisted it calls a configurable URL to check if the user is still whitelisted.

URLs
==============
| Name     | Method | Parameters                     | Expected response                                                           | Description                                                                                           |
|----------|--------|--------------------------------|-----------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| check    | GET    | privatekey, uuid               | 2xx status code and json object like `{"end":123456789000}`                 | when caching of a user expires, the mod calls this url to check if the user is still whitelisted, the end timestamp is used for caching purposes    |
| register | POST   | privatekey, authtoken, uuid    | 2xx status code and json object like `{"nick":"foobar","end":123456789000}` | the `end` has to be a time in UTC, basically a unix timestamp with milliseconds, the parameters are sent in `application/x-www-form-urlencoded` form |

 * The privatekey parameter is purely to authenticate the server itself
 * The authtoken can be any string in the range `[a-zA-Z0-9_-]+`, the mod sends a message to the unkown player to enter it, and disconnects the player after 1 minute or on a non-successfull response
 * The uuid is the minecraft uuid connected to the user, it does not change as opposed to the name of the player

The `nick` property of the json object is used to announce the player to the server, to disable set the announcement message to an empty string

Installation
-------------
Simply drag the provided `SubWhitelister.jar` file into your `plugins/` directory and reboot your server. The configuration file will be automatically generated. 

Commands and Permissions
------------------------
| Command    | Description                                  | Permission                     |
|------------|----------------------------------------------|--------------------------------|
| /sw purge  | Purges the user cache.                       | subwhitelister.*               |
| /sw toggle | Enables or disables the plugin.              | subwhitelister.toggle          |
| /sw exempt | Exempts a user from the whitelist check.     | subwhitelister.exempt          |
