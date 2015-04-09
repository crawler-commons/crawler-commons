# Introduction #

As Crawer-Commons relies upon Apache Maven for build lifecycle, it is really easy to create an Eclipse project which you can import in to Eclipse.

# Details #

Just do the following.

```
 % mvn eclipse:eclipse 
```

This will generate the .project file and other required files.

You can then open Eclipse and go to File > Import > Existing (Maven) Projects into Workspace > path/to/Crawler-Commons