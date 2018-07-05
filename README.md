# GitHubBot

Bot, that posts all your github commits to twitter.

## Getting Started

1. Clone this project using this command:

```git
git clone https://github.com/dmitryblackwell/GitHubBot.git
```

1. Create compiler output folder

Create folder for compiled classes (`out` by default) and add it to your project structure.
```
File -> Project Structure -> Project -> Project Compiler output
```

1. Add TwitterKeys.java to the `com.blackwell` package.

You need to add file with your keys for twitter. You can do it by using next pattern:

```java
package com.blackwell;

class TwitterKeys {
    // init logger
    private static final Logger LOG = Logger.getLogger(TwitterKeys.class);               
    static { LOG.debug("starting to initialized twitter keys"); }
    
    // Consumer key
    static final String ConsumerKey = "";
    // Consumer secret
    static final String ConsumerSecret = "";
    // Access token
    static final String AccessToken = "";
    // Access token secret
    static final String AccessTokenSecret = "";
    
    static { LOG.debug("Twitter keys was initialized"); }
}
```

1. Add all libraries to your project.

Go to the 
```
File -> Project Structure -> Libraries
```
and press green plus, java. After select all jars from lib-folder and click OK.

## How it works?

Bot updating map with all your commits and after checking every hour if there are new commits.
Why only one hour? Because GitHub got there restriction on request per hour. So for now, without authorization on github, you can do it only ones per hour.

#### Example of request url

```
// getting all repos
https://api.github.com/users/dmitryblackwell/repos

// getting commits for special repo
https://api.github.com/repos/dmitryblackwell/GitHubBot/commits
```


## Version
Current version is **1.0.0**

For details see [SemVer](http://semver.org/) site.

## Author
* **Dmitry Blackwell** - *Initial work, Twitter API, GitHub API.* - [@dmitryblackwell](https://github.com/dmitryblackwell)


## License

This project is licensed under the Apache License 2.0 - see the [LICENSE.md](LICENSE.md) file for details.

Apache License 2.0 © [@dmitryblackwell](https://github.com/dmitryblackwell)
