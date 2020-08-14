# TokenInterceptor

is library which providing interceptor for check current token is expired or not? If it's expired, The system will refresh token.

## Installation

build.gradle (project)
```bash
buildscript {
  repositories {
       jcenter()
  }
}
allprojects {
  repositories {
       jcenter()
   }
}
```

build.gradle (app)
```bash
implementation 'com.natradac.android:token-interceptor:0.2.2'
```

## Usage V0.2.0

Add Interceptor to OkHttp
```kotlin
import com.natradac.android.tokeninterceptor.ExpiredTokenInterceptor

  ...
  OkHttpClient.Builder().apply {
    addInterceptor(ExpiredTokenInterceptor(context))
  }.build()
  ...

```

Init Refresh token endpoint
```kotlin
RefreshToken.initEndpoint(endpoint = ..., context = ...)
```

Default of request is
```
method : POST
body : {
    tokenId:
    refreshToken:
}
```
Can change request by using
```kotlin
RefreshToken.initRequest(requestMethod = ... as String, requestBody = ... as RequestBody?)
```

Default of response key is
```
"access_token"
"refresh_token"
"expires_in"
```
Can change response key by using
```kotlin
RefreshToken.updateResponseKey(accessToken = ... as String, refreshToken = ... as String, expiresIn = ... as String)
```

Init token
```kotlin
RefreshToken.updateToken( token = ... as String, refreshToken  = ... as String, expiresIn : ... as Long)
```

