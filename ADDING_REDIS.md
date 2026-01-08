Redis needs password and username to connect.

You need to add password in your app_settings for your app service
@Microsoft.KeyVault(SecretUri=https://redis_name.vault.azure.net/secrets/redis-password/)

Please note one thing I have selected enterprise redis edition, for this u can do only password username authenthication.
This is a limitation from redis side for enterprise version.

## Note
I have noticed one thing that is we cannot automatically store the redis primary or secondary keys in the key vault.