# Citrus and Barista

Citrus is a programming language a programming language that supports one way constraints, events, value restrictions, and object ownership. I used Citrus to implement the Barista user interface toolkit, which supports the construction of text-editable structured editors.

To see a demo of both, check out the [Citrus](https://www.youtube.com/watch?v=YIlYJCwIXLs) and [Barista](https://www.youtube.com/watch?v=gAxjUh9d2YI) YouTube videos.

## History

During my first year in graduate school I spent a lot of time working with the [Alice](http://www.alice.org/) code base, building a debugger called the Whyline. The Whyline work was a lot of fun, and I would eventually would return to it for my dissertation, but I spent a year enamored with structured editors and user interface toolkits. This led to both Citrus and Barista, which were both aimed at trying to solve two fundamental problems with structured editors: they're really hard to build and, at the time, they were really hard to use.

The basic idea in Citrus is that state can and should be much more sophisticated. Variables shouldn't be just containers for values, but have all kinds of metadata, like constraints and restrictions on their values, error checking, units, and backreferences to things that refer to them. Features like these make it very easy to build user interfaces to structured data, which require many of these features to represent the complex interdependencies in their user interfaces. Citrus was my best attempt at expressing these ideas; they started off as a basic library, and then I realized that giving them a syntax would make them much more powerful. The result was kind of a mess, but it was good enough at expressing the ideas.

I built Citrus to ultimately make it easier to build code editors, and so I built Barista using the Citrus language. As it turns out, building a research prototype on top of a research prototype with one user is not easy! But it was sufficient to explore the basic ideas in Barista, which was incremental parsing combined with mixed-mode editing, allowing for both block-based drag and drop and text-based editing interactions.

After I finished these two projects, my little hiatus into editor land was done. I didn't see many further research opportunities in the space that I was excited about, and so I moved onto other tasks in software development, returning to debugging, but also investigating navigation and program understanding tools. But the projects remain quite popular, and eventually informed broader efforts to improve the accessibility and flexibility of code editors such as [Scratch](http://scratch.mit.edu) and [Blockly](https://github.com/google/blockly). Now that I've begun to do work in computing education research, structured editors have been resurrected with great success and some of the lessons in these projects continue to inform new efforts.

## Architecture

The best way to learn about the two projects is to read the two corresponding publications:

* [Citrus: A Language and Toolkit for Simplifying the Creation of Structured Editors for Code and Data](http://faculty.washington.edu/ajko/publications?id=citrus). Ko, A.J. and Myers, B.A. (2005). ACM Symposium on User Interface Software and Technology (UIST). A programming language and library with language-level support for constraints, restrictions and change notifications on primitive and aggregate data.
* [Barista: An Implementation Framework for Enabling New Tools, Interaction Techniques and Views for Code Editors](http://faculty.washington.edu/ajko/publications?id=barista). Ko, A.J. and Myers, B.A. (2006). ACM Conference on Human Factors in Computing Systems (CHI), 387-396. Abstractions that make it easy to build rich multimedia interfaces in a code editor without sacrificing the ability to write code as text.

The implementation is (unsurprisingly for a hastily-written, unmaintained research prototype) a bit of a mess. There are no tests, no build scripts, and not a lot of comments. Sorry :(

## Support

Unfortunately, because I've long since moved on to other projects, I cannot support this code or develop it further. Fork it, patch it, extend it: do whatever you like with it. It's here for the public good as an archive for future generations of developer tool developers. I'd love to see what you do with it! I love to hear stories about how people are building upon the work.

That said, if you find that things are critically broken and can be fixed with some simple changes, submit a pull request. I'll review all requests eventually and merge them, so that others can continue to play with the code.

## Running the Examples

I've included several examples that demonstrate the various features of the language and UI toolkit. 

There's a folder named "Citrus" with this release that has several folders in it named "images", "languages", and "styles". For now, the Citrus interpreter expects a folder named "Citrus" with these subfolders in it, relative to the execution path, so don't move them. A proper design would allow these "libraries" to be in an arbitrary location (like the Java SDKs on a Windows machine), or a default system location (like the Java SDKs on an OS X machine).

Anyway, the "languages" folder has several folders that represent different applications and libraries. For example, the "ToDo" folder has the two .citrus files that have the source code for a simple to do editor. To run this ToDo app, from the directory that has the Citrus.jar file in it, you would type:

	java -jar Citrus.jar Citrus/languages/ToDo/ToDoViews.citrus

The ToDoViews.citrus file has an "init" at the top, which is like Java "main" method. Other folders, like HTML and Widgets, are just libraries, and don't have files with an init.

## Understanding the Source

If you're trying to understand all of the source code included here, here are a few major points:

* The source is generally divided into two major parts: the package edu.cmu.hcii.citrus, which is the Citrus virtual machine, parser, etc., and edu.cmu.hcii.citrus.views, which is the Citrus user interface toolkit.

* The major base classes amongst all of the source code include Element (the Citrus equivalent of Java's Object), Property (the Citrus equivalent of a Java Object's field, but with so much more), View (the Citrus equivalent of Swing's JComponent), and App (the Citrus equivalent of Swing's JFrame).

## Writing Citrus code

The best way to learn the syntax is either to read the parser code (edu.cmu.hcii.citrus.CitrusParser) or to look at the examples included in the Citrus/languages subdirectory. I haven't very well formalized the syntax, because it has been in a state of flux as I've been iterating on the language design.
