### ReadyCI
A no-fuss CI/CD tool for iOS and Android apps
    
## Why use ReadyCI?

### Builds iOS, Android, and maven apps
ReadyCI comes with build scripts so that you spend less time setting up your automated CI/CD infrastructure, and get to making automated builds faster. ReadyCI scripts currently support:
* iOS apps
* Android apps
* Maven projects
* Sonarqube

### Runs as command-line or web-service
You can run ReadyCI on the command-line within another CI environment like Jenkins, or run ReadyCI as a service on it's own and accept web-hook calls from GIT services like Github and Bitbucket.

### Supports web-hooks
ReadyCI supports GIT commit web-hooks when you run it in server mode so that your automated builds start as soon as you push to your git repository. ReadyCI supports web-hooks from:
* GitHub
* BitBucket

### Parses iOS provisioning profiles
Configuring iOS builds is tricky. ReadyCI uses the .mobileprovision file generated by Apple Developer Portal to automatically configure your build and remove some of the guess-work behind making your iOS app build successful. ReadyCI also keeps the provisioning profiles on CI agent up-to-date. All you need to do is commit updated provisioning profiles to your GIT repository and ReadyCI will take it from there.

### Deploys to App Store Connect, Google Play and Hockeyapp
ReadyCI can automatically deploy your app to Hockeyapp or the respective app stores. Just add the appropriate task and credentials and ReadyCI will get your upload your build for you.

### Keeps credentials safe
ReadyCI allows you to split configuration accross multiple configuration files so that you can keep your credentials safely on your CI server and out of your GIT repository.

## Quick Start
### Getting ReadyCI
Download ReadyCI From https://readyci.org 

### Running a command-line build
Run a once off command-line build by specifying the `yml` configuration file and the `pipeline=` parameter. It's only fitting that ReadyCI be able to build itself!   

Try this out with the example found below using the configuration `readyConfigExample.yml` to run a ReadyCI build named `readyci`. 
```bash
$ java -jar target/readyci-0.3.jar readyConfigExample.yml pipeline=readyci

Loaded configuration readyci_config_example.yml with 2 pipelines
...
ReadyCI is in command-line mode
Building pipline readyci
...
FINISHED BUILD 74e404d8-6bae-41fa-8aa1-4d786c797c58 
```  
A successful build will deploy `readyci.jar` to your `/tmp/` directory. You can check that it's there like this:
```bash
$ ls -la /tmp/readyci-0.3.jar 
-rw-r--r--  1 bradley  wheel  16612035 Jun 13 12:30 /tmp/readyci.jar
```


## Wiki
Head over to the wiki to learn more: https://github.com/DeloitteDigitalAPAC/readyci/wiki
