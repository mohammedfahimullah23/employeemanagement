### Adding OAuth to the application

First to support this we need to add the dependency

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
    <version>4.0.1</version>
</dependency>
```

I am adding github.So you need to create clientId and clientSecret in the github under OAuth Settings 
under developer settings. Add this in your application.yml file/

You need to set github developer settings
http://localhost:8080/login/oauth2/code/github in callback url.
It is the OAuth callback endpoint that Spring Security automatically provides for GitHub login.


To test this in local using postman
http://localhost:8080/oauth2/authorization/github

This opens the authorize option.you authorize them. After the successful authorization its goes to the  OAuthSuccessHandler
![alt text](authorization.png)



OAuthSuccessHandler is where you can write custom logic where you can add the data in the DB.

I was facing an issue, by default the email is not given by the github. Even if I explicity asked in the scope to solve this issue, we need to call https://api.github.com/user/emails and get the email from this. To sovle this issue we need to write this service CustomOAuth2UserService and update the logic in SecurityConfig file.

```
    .oauth2Login(oauth -> oauth
     .userInfoEndpoint(userInfo ->
        userInfo.userService(customOAuth2UserService)
      )
    .successHandler(oAuthSuccessHandler)
                )
```