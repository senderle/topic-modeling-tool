
# Topic Modeling Tool

## A GUI for MALLET's implementation of LDA.

### New features:

* **Metadata integration**
* **Automatic file segmentation**
* **Custom CSV delimiters**
* **Alpha/Beta optimization**
* **Custom regex tokenization**
* **Multicore processor support**

## Getting started:

To start using some of these new features right away, consult the 
[quickstart guide](https://senderle.github.io/topic-modeling-tool/documentation/2017/01/06/quickstart.html).

## Requirements:

**UPDATE: We now have experimental support for a native Mac app. If you 
feel adventurous, don't have administrative access to your Mac, or just
don't like installing java, download and open `TopicModelingTool-1.0.dmg`.
You should be able to drag it to any folder and run it. Support for 
Windows and Linux is forthcoming.**

Otherwise, you'll need to have a fairly recent version of Java; the version 
that came with your computer may not work, especially if your computer is a Mac.

If you have a newer Mac, you probably don't have Java installed at all. 
Whatever your operating system, you can install an updated version of Java, 
by following the instructions for your operating system 
[here](https://java.com/en/download/help/download_options.xml).

## Building the Development Version:

If you feel adventurous, you might want to modify the code and compile your 
own version. To do so, you'll need to install [Apache Maven](https://maven.apache.org/) 
as well as the Java Development Kit. On Macs, [Homebrew](http://brew.sh/) 
is the best way to do so; simply install homebrew as described on the Homebrew 
site, and then type `brew install maven` at the command line.

With maven installed, simply use the terminal to navigate to the `TopicModelingTool` folder:

    $ cd topic-modeling-tool/TopicModelingTool
    
Then use maven's `package` command:

    $ mvn package

We now have experimental support for compiling the tool as a native OS X app using
the [javafx plugin](https://github.com/javafx-maven-plugin/javafx-maven-plugin) 
for maven. This will build a native `.app` package that should run on any Mac (UNTESTED):

    $ mvn jfx:native
    
The javafx plugin also supports building Windows native apps -- apparently. But 
we don't have a Windows Java hacker to hold our hands through this process. If 
you use Windows and know how to set up a Java development environment with maven, 
please feel free to add instructions here and submit a PR.
___

#### Acknowledgements:

This version of the tool was forked from the [original version](http://code.google.com/p/topic-modeling-tool) 
by [David Newman](http://www.ics.uci.edu/~newman/) and [Arun Balagopalan](https://github.com/arunbg).

Previous work on the GUI for MALLET has been supported by a National Leadership 
Grant (LG-06-08-0057-08) from the Institute of Museum and Library Services to 
Yale University, the University of Michigan, and the University of California, 
Irvine. The Institute of Museum and Library Services is the primary source of 
federal support for the nation's 123,000 libraries and 17,500 museums. The 
Institute's mission is to create strong libraries and museums that connect people to information and ideas.

Work on this version of the tool has benefited from the support of 
[Penn Libraries](http://www.library.upenn.edu/) and the the University of 
Pennsylvania's [Price Lab for Digital Humanities](https://pricelab.sas.upenn.edu/).
