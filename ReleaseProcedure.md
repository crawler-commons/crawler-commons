# Introduction #

This page describes how to do a release of crawler-commons. As we are using
pure Maven, it is drop dead easy :0). So hold on to your hats, here we go!

# Details #

First, checkout a fresh version of the **ENTIRE** Crawler-Commons SVN repository

It is pretty small (< 1/2 GB) and this saves time later on. The following command can be used:

> % `svn checkout https://crawler-commons.googlecode.com/svn/ crawler-commons --username ${user.name}`

You may also be prompted for your password here.

**Non-core Documentation is Accurate**

  * `% cd trunk`

Please ensure that the details in `CHANGES.txt` are accurate and that
licensing etc. is all accurate.

**Generate Release Javadoc**

In all of the steps below, replace `X.Y` with the actual release number.

  * `% gedit pom.xml`
  * change project version from `X.Y-SNAPSHOT` to `X.Y`
  * `% mvn javadoc:javadoc`
  * You may need to create the new `javadoc/X.Y` directory
  * % `cp -r target/site/apidocs/ ../wiki/javadoc/X.Y/`
  * % `svn add ../wiki/javadoc/X.Y/`
  * % `svn revert pom.xml`
  * % `mvn clean`

**Prepare the Release Artifacts (Dry-Run)**

  * % `rm -rf ~/.m2/repository/com/google/code/crawler-commons/`
  * % `mvn release:clean release:prepare -DdryRun=true`

Executing the above makes life a breeze for us. It does the following:

  * cleans out all previous release-related module directories
  * prepares us for a release, stating that this is a dryRun of the actual release. This gives us the opportunity to review the artifacts.

At this stage is it imperative to check the resulting artifacts as we wish to iron out any discrepancies at this stage.

**Commit to SVN & Tag**

Before we commit the tag and set ourselves up for the next development drive, we want to commit the new Javadocs to Subversion.

  * `% cd ../`
  * You may wish to execute `svn status` to ensure that all the files you wish are being committed
  * `% find wiki/javadoc -name "*.html" | xargs -I filename svn propset svn:mime-type text/html filename`
  * `% find wiki/javadoc -name "*.gif" | xargs -I filename svn propset svn:mime-type image/gif filename`
  * `% find wiki/javadoc -name "*.css" | xargs -I filename svn propset svn:mime-type text/css filename`
  * `% svn ci -m "X.Y Release Javadoc"`
  * `% cd trunk`
  * `% mvn release:clean release:prepare`

N.B. If the final command fails this may be due to non-interactive mode being activated in your local SVN client.
This can easily be overcome by explicitely passing in the -Dusername=${username} -Dpassword=${password} arguments
to the command. Your username and password can be located within your GoogleCode profile.

This will create and commit the release tag and bump the development version in the pom.xml file.

**Build & Deploy to Sonatype**

This will build the pom, jar, javadoc, signatures and sources, and push them to the Sonatype staging repository

  * `% mvn release:perform`

If this command fails, ensure that you have the sonatype server configuration within your ~/.m2/settings.xml as follows

```
<settings>
  <servers>
    ...
    <server>
      <id>sonatype-nexus-staging</id>
      <username>${nexus_username}</username>
      <password>${nexus_password}</password>
    </server>
    ...
```



**Close the Sonatype Staging Repository**

  * % Browse to https://oss.sonatype.org/index.html and log in.
  * % Navigate to the Staging Repositories side tab and locate the stating release.
  * % Close the repository so that others can view and review it.

**Hold a Community VOTE**

  * % Head over to the Crawer Commons mailing list and create a thread which details the tag and staging release.
  * % Collect votes, give it time to bake.
  * % If all is good head back to the staging repository and Release the artifacts.

**Update the Javadoc link on the project main page**

  * Click on the Administer link
  * Update the `*`User Documentation`*` section to link to the new Javadoc index.html file, e.g. `* [http://crawler-commons.googlecode.com/svn/wiki/javadoc/0.2/index.html Javadoc]`

**Publicize**

Post to the crawler-commons list, as well as Nutch, Bixo, LinkedIn, Twitter etc.

**Additional Information**

It is common for developers unfamiliar with staging, snapshot and release repositories to encounter difficulties when attempting to release artifacts. Much more information on the OSS Sonatype platform can be found [here](https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide)