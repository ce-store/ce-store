# Controlled English (CE)

Controlled English is a form of structured English that allows you to define models of concepts, their properities and relationships that is both Human and machine readable.

 - EXAMPLE APPLICATIONS OF CE

 - SUPER SIMPLE EXAMPLE - LINK TO DETAILED TUTORIAL - GRAB THE ATTENTION OF IT'S SIMPLICITY AND SHOW IT IN ACTION?

The CE Store contains everything you need to get started with a Controlled English environment.

We are an IBM initiated open source organisation focused on providing an experimental research environment for the ITA Controlled English language.

## Quick Guide

* [Installation and Setup](https://github.com/ce-store/ce-store#installation-and-setup)
* [Usage](https://github.com/ce-store/ce-store#usage)
* [Tutorials](https://github.com/ce-store/ce-store/wiki#tutorials)
* [API](https://github.com/ce-store/ce-store/wiki#api)
* [Cheatsheet](https://github.com/ce-store/ce-store/wiki#cheatsheet)
* [Command Syntax](https://github.com/ce-store/ce-store/wiki#command-syntax)
* [Hudson](https://github.com/ce-store/ce-store/wiki#hudson)
* [Other Projects](https://github.com/ce-store/ce-store/wiki#other-ce-projects)
* [Contributing](https://github.com/ce-store/ce-store#contributing)
* [License](https://github.com/ce-store/ce-store#license)

## Installation and Setup - Deploy to the Cloud 

[![Deploy to IBM Bluemix](https://bluemix.net/deploy/button.png)](https://bluemix.net/deploy?repository=https://github.com/ce-store/ce-store)

An easy way to get started with Controlled English is through IBM Bluemix. You can [sign up to Bluemix for free](https://console.bluemix.net/), and once you've got an account, clicking the button below will automatically deploy a Cloud-hosted instance of your very own CE Store.


## Installation and Setup - Run on your own machine

Clone the code

```
git clone https://github.com/ce-store/ce-store
```

### Using Apache Maven

Using Apache Maven, run the following command to start up the CE Store application using an embedded Tomcat server.

```
mvn install
mvn tomcat:run
```

The CE Store will be available at the following URL [http://localhost:8080/ce-store](http://localhost:8080/ce-store)

### Using Docker

```
docker pull cestore/ce-store
docker run --rm -it -p 8080:8080 cestore/ce-store
```

### Building With Docker

Use Apache Maven to build a WAR file for the project with the following command.

```
mvn package
```

This WAR file can be built into a Docker image with the command below.

```
docker build -t ce-store .
```

Then start the container from this image running locally.

```
docker run --rm -it -p 8080:8080 ce-store
```

The CE Store will be available at the following URL [http://localhost:8080/ce-store](http://localhost:8080/ce-store)


### Using Vagrant
Requires [Vagrant](https://www.vagrantup.com) to be installed.

```
vagrant up
vagrant ssh
cd /vagrant
```

Then follow the steps in Using Apache Maven.


## Usage

### Engineering Panel

The Engineering Panel provides a simple web interface to the CE Store, enabling the user view the contents of the CE Store, to load new CE sentences, run queries agains the store, and more.

To open the Engineering Panel go to `<SERVER>/ce-store/ui`. It should look like this:

![Engineering Panel](http://ce-store.github.io/i/ui.png)

The engineering panel has four main areas:

  * [Left] Model operations: CE models can be loaded and modified.
  * [Right] Object pane: Contents of a CE store can be viewed.
  * [Bottom] Messages: Errors will be reported here
  * [Centre] Working area: Interactions with the model.

The CE Store comes with a sample set of CE sentences to build a model about medical data. The wiki includes a tutorial for using the [Medicine model](https://github.com/ce-store/ce-store/wiki/Introducing-the-Medicine-Model).

## Contributing

Read about contributing [here](https://github.com/ce-store/ce-store/blob/master/CONTRIBUTE.md).

## License

Licensed under the [Apache License, Version 2.0](https://github.com/ce-store/ce-store/blob/master/LICENSE.md)
