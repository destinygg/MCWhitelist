MCWhitelist
==============

##A plugin for whitelisting users.

Based originally on the https://github.com/Bitjump/SubWhitelister mod.

It calls URL's to handle whitlisting of users.
The idea behind it is to let only destiny.gg subscribers onto the server and when they join, print an announcement message.
The plugin is not at all restricted to destiny.gg, 


URLs
==============
| Name     | Method | Parameters             | Expected response                                                           | Description                                                                                           |
|----------|--------|------------------------|-----------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| check    | GET    | privatekey, uuid       | 200 status code and json object like `{"end":1234567890000}`                 | when caching of a user expires, the mod calls this url to check if the user is still whitelisted, the end timestamp is used for caching purposes    |
| register | POST   | privatekey, name, uuid | 200 status code and json object like `{"nick":"foobar","end":1234567890000}` | the `end` has to be a time in UTC, basically a unix timestamp in milliseconds, the parameters are sent in `application/x-www-form-urlencoded` form |

 * The `privatekey` parameter is purely to authenticate the server itself, it is always the same and is supposed to be kept a secret
 * The `name` is the minecraft name of the user
 * The `uuid` is the minecraft uuid connected to the user, it does not change as opposed to the name of the player
 * On error, the **status code 403** will be regarded as the error code for an expired subscription, all other 4xx status codes are treated as errors

The `nick` property of the json object is used to announce the player to the server, to disable set the announcement message to an empty string.
The announcement message uses two placeholders: `{nick}` the nick received from the API, and `{name}` the minecraft name of the player.

Installation
-------------
Simply drag the provided `MCWhitelist.jar` file into your `plugins/` directory and reboot your server. The configuration file will be automatically generated.

Commands and Permissions
------------------------
| Command    | Description                     | Permission         |
|------------|---------------------------------|--------------------|
| /sw toggle | Enables or disables the plugin. | mcwhitelist.toggle |
| /sw exempt | Exempts a user from checks.     | mcwhitelist.exempt |
