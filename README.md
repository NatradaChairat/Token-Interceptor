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

## Usage

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
RefreshToken.initToken( token = ..., refreshToken  = ..., expiresIn : ... as Long, timestamp : ... as Long)
```

