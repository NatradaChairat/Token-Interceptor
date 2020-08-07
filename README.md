# TokenInterceptor

is library which providing interceptor for check current token is expired or not? If it's expired, The system will refresh token.

## Installation

build.gradle (project)
```bash
buildscript {
  repositories {
      maven {
        url 'https://dl.bintray.com/natradachairat/maven'
      }
  }
}
allprojects {
  repositories {
      maven {
        url 'https://dl.bintray.com/natradachairat/maven'
      }
   }
}
```

build.gradle (app)
```bash
implementation 'com.natradac.android:token-interceptor:0.1.0'
```

## Usage V0.1.0

Add Interceptor to OkHttp
```kotlin
import com.natradac.android.tokeninterceptor.ExpiredTokenInterceptor

class OkHttpBuilder {
  ...
  OkHttpClient.Builder().apply {
    addInterceptor(ExpiredTokenInterceptor(context))
  }.build()
  ...
}
```

Init Refresh token endpoint
```kotlin
RefreshToken.initEndpoint(endpoint = ..., context = ...)
```

Init token
```kotlin
RefreshToken.updateToken( token = ..., refreshToken  = ..., expiresIn : ... as Long)
```

