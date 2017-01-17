
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

**UPDATE**: We now have experimental support for native Windows and Mac apps. 
If you feel adventurous, don't have administrative access to your computer, 
or just don't like installing java, follow the instructions for your 
operating system:

**For Macs**: 
* Download `TopicModelingTool.dmg`.
* Open it by double-clicking.
* Drag the app into your `Applications` folder -- or into any folder at all.
* Run the app by double-clicking.

**For Windows PCs**:
* Download `TopicModelingTool.zip`.
* Extract the files into any folder and open it.
* Double-click on the file called `TopicModelingTool.exe` to run it.

Otherwise, you'll need to have a fairly recent version of Java; the version 
that came with your computer may not work, especially if your computer is a Mac.

If you have a newer Mac, you probably don't have Java installed at all. 
Whatever your operating system, you can install an updated version of Java, 
by following the instructions for your operating system 
[here](https://java.com/en/download/help/download_options.xml).

## Reporting and Replicating Bugs and Other Issues:

If you hadn't already guessed, most testing for this tool happens on a Mac. 
There are bound to be errors happening on other platforms that have slipped
through the cracks. We need you to report them!

For example, we know that there are some problems with Windows support for
unicode text; if you see problems, please post *detailed* information under
the [main issue](https://github.com/senderle/topic-modeling-tool/issues/48)
so that we can start isolating and fixing these bugs. 

We love getting new issues because it means the tool is improving!
**When posting a bug report, please include vast amounts of detail**. Copy
and paste everything from the tool's console output if you can, tell us
your operating system and version, and let us know the other tools you're
using to view the output. It also helps if you verify that the bug still
exists in the most recent build of the tool (i.e. the one contained in 
the `.jar` or `.dmg` files in the root directory).

## Building the Development Version:

If you feel adventurous, you might want to modify the code and compile your 
own version. To do so, you'll need to install [Apache Maven](https://maven.apache.org/) 
as well as the Java Development Kit. On Macs, [Homebrew](http://brew.sh/) 
is the best way to do so; simply install homebrew as described on the Homebrew 
site, and then type `brew install maven` at the command line. On Windows PCs -- 
you're on your own! But we did it and it wasn't terribly hard. You just need an
up-to-date JDK and maven package, with their `bin` folders in your `PATH`. 

With maven installed, simply use the terminal to navigate to the `TopicModelingTool` folder:

    $ cd topic-modeling-tool/TopicModelingTool
    
Then use maven's `package` command:

    $ mvn package

We now have experimental support for compiling the tool as a native app using
the [javafx plugin](https://github.com/javafx-maven-plugin/javafx-maven-plugin) 
for maven. This will build a native package able to run on your operating system.
This has been tested on both Macs and Windows PCs.

    $ mvn jfx:native
    
___

#### Acknowledgements:

This version of the tool was forked from the 
[original version](http://code.google.com/p/topic-modeling-tool) 
by [David Newman](http://www.ics.uci.edu/~newman/) and 
[Arun Balagopalan](https://github.com/arunbg).

Previous work on the GUI for MALLET has been supported by a National Leadership 
Grant (LG-06-08-0057-08) from the Institute of Museum and Library Services to 
Yale University, the University of Michigan, and the University of California, 
Irvine. The Institute of Museum and Library Services is the primary source of 
federal support for the nation's 123,000 libraries and 17,500 museums. The 
Institute's mission is to create strong libraries and museums that connect 
people to information and ideas.

Work on this version of the tool has benefited from the support of 
[Penn Libraries](http://www.library.upenn.edu/) and the the University of 
Pennsylvania's [Price Lab for Digital Humanities](https://pricelab.sas.upenn.edu/).
