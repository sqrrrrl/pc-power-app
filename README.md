# What is this?
This is the mobile app component of a three-parts project where an [API](https://github.com/sqrrrrl/pc-power-api), an [arduino](https://github.com/sqrrrrl/pc-power-arduino) and a mobile app communicate together in order to enable users to remotely control the power state of their desktop computer(s).

# Installation
1. Clone the repository:
```
git clone https://github.com/sqrrrrl/pc-power-app.git
```

# Usage
## Environment variables
Create a file at the root of the project named local.properties and add the base url of the api to it:
```
apiUrl="https://pcpower.example.com"
``` 

## Installing the app
The easiest way to build the app is by using Android Studio:
1. Open the project in Android Studio
2. Change the build variant to "release": ```Build > Select Build Variant```
3. Select ```Generate Signed Bundle / APK``` in the ```Build``` menu and follow the instructions
4. The apk should appear at ```./app/release/app-release.apk```


# License
Distributed under the [GPL-3.0 license](https://github.com/sqrrrrl/pc-power-app#GPL-3.0-1-ov-file)
